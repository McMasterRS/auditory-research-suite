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

import javax.sound.midi.MidiEvent;

import edu.mcmaster.maplelab.common.LogContext;


/**
 * Class responsible for logging trial data to a file.
 * @version  $Revision:$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Nov 22, 2006
 */
public abstract class FileTrialLogger<T extends Session<?,?,?>, Q extends 
    Block<?, ?>, R extends Trial<?>> implements TrialLogger<Q, R> {
    
    private final T _session;
    private final File _file;
    private final boolean _deleteTempFile;
    private static File _outputDir;

    public FileTrialLogger(T session, File workingDirectory) throws IOException {
        this(session, workingDirectory, true, false);
    }
    
    public FileTrialLogger(T session, File workingDirectory, boolean separateOutput, boolean deleteTempFile) throws IOException {
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
    protected T getSession() {
        return _session;
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
     * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#submit(edu.mcmaster.maplelab.common.datamodel.Block, edu.mcmaster.maplelab.common.datamodel.Trial)
     */
    public void submit(Q block, R trial) throws IOException {
        
        EnumMap<? extends Enum<?>,String> map = marshalToMap(block, trial, 0, null);
        
        File file = getFile();
        
        boolean addHeader = !file.exists();
        
        FileWriter out = null;
        try {
            out = new FileWriter(file, true);
            if(addHeader) {
                writeHeader(map.keySet(), out);
            }
            
            writeRow(map, out);
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
    protected void writeRow(EnumMap<? extends Enum<?>, String> map, Writer out) throws IOException {
        Set<? extends Enum<?>> keys = map.keySet();
        
        for(Enum<?> e : keys) {
            out.write(String.format("%s\t", map.get(e)));
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
    protected void writeHeader(Set<? extends Enum<?>> keys, FileWriter out) throws IOException {
        for(Enum<?> e : keys) {
            out.write(String.format("%s\t", e.name()));
        }
        out.write(String.format("%n"));
    }
 
    
    protected abstract EnumMap<? extends Enum<?>, String> marshalToMap(Q block, R trial, 
    		int responseNum, MidiEvent event); 

}
