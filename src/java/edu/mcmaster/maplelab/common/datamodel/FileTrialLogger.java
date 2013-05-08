/*
* Copyright (C) 2006 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/

package edu.mcmaster.maplelab.common.datamodel;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.RelativeTrialPosition;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy;


/**
 * Class responsible for logging trial data to a file.
 * @version  $Revision:$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Nov 22, 2006
 */
public abstract class FileTrialLogger<S extends Session<?, ?, ?>, T extends Trial<?>> implements TrialLogger<T> {
	
	protected static final String DEBUG_FILE = "debug";
	protected static final String RESPONSE_FILE = "response";
	protected static final String RESPONSE_ALL_FILE = "response_all";
	
	private static void loadFileTypes() {
		FileType.create(DEBUG_FILE, "log", "debug", false, false);
		FileType.create(RESPONSE_FILE, "txt", "responses", false, false);
		FileType.create(RESPONSE_ALL_FILE, "txt", "responses", true, false);
	}
	
	private enum SessionKeys {
		exp_id,
		sub_exp_id,
		exp_build,
    	exp_build_date,
    	ra_id,
    	subject, 
        session
	}
    
	private enum CountKeys {
		trial_num,
		block_num,
		metablock_num,
		block_instance,
        repetition_num,
        trial_in_metablock,
        block_in_metablock, 
        trial_in_block,
        time_stamp
	}
	
    private final S _session;
    private final File _file;
    private final boolean _deleteTempFile;
    private static File _outputDir;

    public FileTrialLogger(S session, File workingDirectory) throws IOException {
        this(session, workingDirectory, true, false);
    }
    
    public FileTrialLogger(S session, File workingDirectory, boolean separateOutput, boolean deleteTempFile) throws IOException {
        loadFileTypes();
        loadAdditionalFileTypes();
    	
    	_session = session;
        _deleteTempFile = deleteTempFile;
        
        if(!workingDirectory.isDirectory() || !workingDirectory.canWrite()) {
            throw new IOException(String.format("'%s' is not a writable directory", workingDirectory.getAbsoluteFile()));
        }
        
        _outputDir = separateOutput ? new File(workingDirectory, "output") : workingDirectory;
        if(!_outputDir.exists()) {
            _outputDir.mkdir();
        }
        
        if(!_outputDir.canWrite()) {
            throw new IOException(String.format("'%s' is not a writable directory", _outputDir.getAbsoluteFile()));
        }

        _file = createFile();
        
        LogContext.getLogger().fine("Output file: " + _file);
        
    }
    
    /**
     * Get the timestamp in a standard format.
     */
    public static String getTimeStamp() {
    	return String.format("%1$ty%1$tm%1$td%1$tH%1$tM", new Date());
    }
    
    /**
     * Load any additional file types needed.
     */
    protected abstract void loadAdditionalFileTypes();
    
    /**
     * Get the individual file file to which data should be written.
     */
    protected abstract File createFile();
    
    /**
     * Get the overall file to which data from individual
     * files should be written.
     */
    protected abstract File getCollectedOutputFile();
    
    /**
     * Get the directory where data output is written. May be workingDirectory,
     * or a sub directory depending on how instantiated.
     */
    protected static File getBaseOutputDirectory() {
        return _outputDir;
    }
    
    /**
     * Get the output file where data is logged.
     */
    protected File getFile() {
        return _file;
    }
    
    /**
     * Get session context registered at creation.
     * @uml.property  name="session"
     */
    protected S getSession() {
        return _session;
    }
    
    /**
     * Get the output file for the given file type.  Not valid for file types
     * needing block and trial information to calculate a file name.
     */
    public File getOutputFile(FileType type) throws UnsupportedOperationException {
    	return getOutputFile(getSession(), type, null);
    }
    
    /**
     * Get the output file for the given file type.  Not valid for file types
     * needing block and trial information to calculate a file name.
     */
    public static File getOutputFile(Session<?, ?, ?> s, FileType type) throws UnsupportedOperationException {
    	if (type.includesBlockTrialNums()) {
    		throw new UnsupportedOperationException(
    				String.format("File type %s needs block and trial information " +
    						"to calculate a file name.", type));
    	}
    	return getOutputFile(s, type, null);
    }
    
    public File getOutputFile(FileType type, T trial) {
    	return getOutputFile(getSession(), type, trial);
    }

    /**
     * Get the output file for the given file type.  Creates directories as
     * needed.
     */
    public static File getOutputFile(Session<?, ?, ?> s, FileType type, Trial<?> trial) {
    	String fName = null;
    	if (type.isAggregateType()) {
    		fName = String.format("ex%s-%s.%s", s.getExperimentID(), type.getSuffix(), 
    				type.getExtension());
    	}
    	else if (type.includesBlockTrialNums()) {
    		fName = String.format("sub%s-sess%s-ex%s-subex%s-b%s-t%s-%s-%s.%s", 
    				s.getSubject(), s.getSession(), s.getExperimentID(), s.getSubExperimentID(), 
    				trial.getBlockNumber(), trial.getTrialNumber(), getTimeStamp(), type.getSuffix(), type.getExtension());
    	}
    	else {
    		fName = String.format("sub%s-sess%s-ex%s-subex%s-%s-%s.%s", 
    				s.getSubject(), s.getSession(), s.getExperimentID(), s.getSubExperimentID(), 
    				getTimeStamp(), type.getSuffix(), type.getExtension());
    	}
    	return new File(getOutputDirectory(s, type), fName);
    }
    
    /**
     * Get the output sub-directory for the given file type, starting
     * from the given base output directory.  Creates directories as
     * needed.
     */
    private static File getOutputDirectory(Session<?, ?, ?> s, FileType type) {
    	return type.isAggregateType() ? getCombinedFileDirectory(s) : getSingleFileDirectory(s);
    }
    
    /**
     * Get the directory in which single output files should be placed.
     */
    private static File getSingleFileDirectory(Session<?, ?, ?> s) {
    	String dirName = String.format("%s-Individual" + File.separator + 
    			"Experiment %s" + File.separator + "Subject %s", s.getExperimentBaseName(),
    			s.getExperimentID(), s.getSubject());
    	File dir = new File(getBaseOutputDirectory(), dirName);
    	if (!dir.exists()) dir.mkdirs();
    	return dir;
    }
    
    /**
     * Get the directory in which combined output files should be placed.
     */
    private static File getCombinedFileDirectory(Session<?, ?, ?> s) {
    	File dir = new File(getBaseOutputDirectory(), 
    			String.format("%s-Composite", s.getExperimentBaseName()));
    	if (!dir.exists()) dir.mkdirs();
    	return dir;
    }

    /**
     * Called when the experiment completes. On successful completion we
     * merge local data file into collected one.
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#shutdown()
     */
    public void shutdown() {
        File collected = getCollectedOutputFile();
        boolean cExists = collected.exists();
        LineNumberReader input = null;
        PrintWriter output = null;
        try {
            input = new LineNumberReader(new FileReader(getFile()));
            output = new PrintWriter(new FileWriter(collected, true));
            
            if(cExists) {
                // The first line will have the header, so we skip it.
                input.readLine();
            }
            
            String line;
            while((line = input.readLine()) != null) {
                output.println(line);
            }
            
            if(_deleteTempFile) {
                getFile().delete();
            }
        }
        catch(EOFException ex) {
        }
        catch (IOException ex) {
            LogContext.getLogger().log(Level.WARNING, "Collected results error", ex);
        }
        finally {
            if(input != null) try { input.close(); } catch(Exception ex) {}
            if(output!= null) try { output.close(); } catch(Exception ex) {}
        }
    }       
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#saveSessionConfig()
     */
    public void saveSessionConfig() throws IOException {
    }

    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#submit(edu.mcmaster.maplelab.common.datamodel.Trial)
     */
    public void submit(T trial) throws IOException {
        
    	List<Set<? extends Enum<?>>> hList = buildHeaderSets();
        List<EnumMap<? extends Enum<?>,String>> list = buildDataSets(trial);
        
        File file = getFile();
        
        boolean addHeader = !file.exists();
        
        FileWriter out = null;
        try {
            out = new FileWriter(file, true);
            if(addHeader) {
                writeHeader(hList, out);
            }
            
            writeRow(list, out);
        }
        finally {
            if(out != null) out.close();
        }
    }

    /**
     * Write out a row of data contained in the given map.
     * 
     * @param map Map containing data.
     * @param out output to write to.
     * @throws IOException on output error.
     */
    protected void writeRow(List<EnumMap<? extends Enum<?>, String>> maps, Writer out) throws IOException {
    	for (EnumMap<? extends Enum<?>, String> map : maps) {
        	if (map == null || map.isEmpty()) continue;
        	for (Enum<?> e : map.keySet()) {
        		out.write(String.format("%s\t", map.get(e)));
            }
        }
        out.write(String.format("%n"));
    }

    /**
     * Write out the header for the given set of keys.
     * 
     * @param keys data set keys.
     * @param out output to write to.
     * @throws IOException on output error.
     */
    protected void writeHeader(List<Set<? extends Enum<?>>> list, FileWriter out) throws IOException {
        for (Set<? extends Enum<?>> set : list) {
        	if (set == null || set.isEmpty()) continue;
        	for (Enum<?> e : set) {
                out.write(String.format("%s\t", e.name()));
            }
        }
        out.write(String.format("%n"));
    }
    
    /**
     * Build the list of enum sets that will label each column.  Exposed for subclasses
     * that need to override.
     */
    protected List<Set<? extends Enum<?>>> buildHeaderSets() {
    	List<Set<? extends Enum<?>>> retval = new ArrayList<Set<? extends Enum<?>>>();
    	
    	retval.add(EnumSet.allOf(SessionKeys.class));
    	retval.add(getGeneralDataHeaders());
    	retval.add(EnumSet.allOf(CountKeys.class));
    	retval.add(getTrialDataHeaders());
    	
    	return retval;
    }
    
    /**
     * Build the list of data sets that will combine for each row.  Exposed for subclasses
     * that need to override.
     */
    protected List<EnumMap<? extends Enum<?>, String>> buildDataSets(T trial) {
    	List<EnumMap<? extends Enum<?>, String>> retval = 
    		new ArrayList<EnumMap<? extends Enum<?>, String>>();
    	
    	retval.add(marshalSessionDataToMap(trial));
    	retval.add(marshalGeneralDataToMap(trial));
    	retval.add(marshalTrialCountsToMap(trial));
    	retval.add(marshalTrialDataToMap(trial));
    	
    	return retval;
    }
 
    /**
     * Gather session data.
     */
    protected EnumMap<? extends Enum<?>, String> marshalSessionDataToMap(T trial) {
    	Session<?, ?, ?> session = getSession();
    	
        EnumMap<SessionKeys, String> fields = new EnumMap<SessionKeys, String>(SessionKeys.class);

        // Meta information
        fields.put(SessionKeys.exp_id, session.getExperimentID());
        fields.put(SessionKeys.sub_exp_id, session.getSubExperimentID());
        fields.put(SessionKeys.exp_build, Experiment.getBuildVersion());
        fields.put(SessionKeys.exp_build_date, Experiment.getBuildDate());
        fields.put(SessionKeys.ra_id, session.getRAID());
        fields.put(SessionKeys.subject, String.valueOf(session.getSubject()));
        fields.put(SessionKeys.session, String.valueOf(session.getSession()));
        
        return fields;
    }
 
    /**
     * Gather trial count data.
     */
    protected EnumMap<? extends Enum<?>, String> marshalTrialCountsToMap(T trial) {
        EnumMap<CountKeys, String> fields = new EnumMap<CountKeys, String>(CountKeys.class);
        
        fields.put(CountKeys.trial_num, String.valueOf(trial.getNumber(TrialHierarchy.TRIAL)));
    	fields.put(CountKeys.block_num, String.valueOf(trial.getNumber(TrialHierarchy.BLOCK)));
    	fields.put(CountKeys.metablock_num, String.valueOf(trial.getNumber(TrialHierarchy.METABLOCK)));
    	fields.put(CountKeys.block_instance, String.valueOf(trial.getNumber(RelativeTrialPosition.BLOCK_INSTANCE)));
        fields.put(CountKeys.repetition_num, String.valueOf(trial.getNumber(RelativeTrialPosition.REPETITION)));
        fields.put(CountKeys.trial_in_metablock, String.valueOf(trial.getNumber(RelativeTrialPosition.TRIAL_IN_METABLOCK)));
        fields.put(CountKeys.block_in_metablock, String.valueOf(trial.getNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK)));
        fields.put(CountKeys.trial_in_block, String.valueOf(trial.getNumber(RelativeTrialPosition.TRIAL_IN_BLOCK)));
        fields.put(CountKeys.time_stamp, trial.getTimeStamp());
        
        return fields;
    }
 
    /**
     * Get a set of items responsible for enumerating the types of general data.
     */
    protected abstract Set<? extends Enum<?>> getGeneralDataHeaders();
 
    /**
     * Gather general data - intended for data specific to the experiment that is still
     * general in the same way as the session data.
     */
    protected abstract EnumMap<? extends Enum<?>, String> marshalGeneralDataToMap(T trial);
 
    /**
     * Get a set of items responsible for enumerating the types of trial data.
     */
    protected abstract Set<? extends Enum<?>> getTrialDataHeaders();
 
    /**
     * Gather trial data.
     */
    protected abstract EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(T trial);

}
