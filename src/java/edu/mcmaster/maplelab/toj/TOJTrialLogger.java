/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import javax.sound.midi.MidiEvent;

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;
import edu.mcmaster.maplelab.toj.datamodel.TOJBlock;
import edu.mcmaster.maplelab.toj.datamodel.TOJResponseParameters;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;
/**
 * TOJ specific extension of FileTrialLogger.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class TOJTrialLogger extends FileTrialLogger<TOJSession, TOJBlock, TOJTrial> {

	public enum Keys {
    	exp_id,
		sub_exp_id,
		exp_build,
    	exp_build_date,
    	ra_id,
    	subject, 
        session, 
        trial_num,
        repetition_num,
        trial_in_repetition,
        block_num, 
        trial_in_block,
        time_stamp, 
        audioFile,
        visFile,
        audioOffset, 
        numDots, 
        confidence, 
        subjResponse,
        responseCorrect
    }

    public enum FileType {
    	RESPONSE,
    	DEBUG {
    		@Override
    		public boolean needsBlockTrialData() {
    			return true;
    		}
    	},
    	RESPONSE_ALL;
    	
    	/**
    	 * Indicate if this file type needs block and trial data
    	 * in order to calculate a file name.
    	 */
    	public boolean needsBlockTrialData() {
    		return false;
    	}
    }

    public TOJTrialLogger(TOJSession session, File workingDirectory) throws IOException {
		super(session, workingDirectory);
	}

	@Override
    protected File getCollectedOutputFile() {
    	return getOutputFile(FileType.RESPONSE_ALL);
    }
    
    @Override
    protected File createFile() {
    	return getOutputFile(FileType.RESPONSE);
    }

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalToMap(TOJBlock block, 
    		TOJTrial trial, int responseNum, MidiEvent event) {
    	
    	TOJSession session = getSession();
    	
        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);

        // Meta information
        fields.put(Keys.exp_id, session.getExperimentID());
        fields.put(Keys.sub_exp_id, session.getSubExperimentID());
        fields.put(Keys.exp_build, TOJExperiment.getBuildVersion());
        fields.put(Keys.exp_build_date, TOJExperiment.getBuildDate());
        fields.put(Keys.ra_id, session.getRAID());
        fields.put(Keys.subject, String.valueOf(session.getSubject()));
        fields.put(Keys.session, String.valueOf(session.getSession()));
        
        // Calculate trial numbers and parameters
        int trial_in_rep = (block.getNum()-1)*block.getNumTrials() + trial.getNum();
        int overall_trial = (session.getCurrentRepetition()-1)*session.getNumBlocks()*block.getNumTrials() + trial_in_rep;
    	fields.put(Keys.trial_num, String.valueOf(overall_trial));
        fields.put(Keys.repetition_num, String.valueOf(session.getCurrentRepetition()));
        fields.put(Keys.trial_in_repetition, String.valueOf(trial_in_rep));
        fields.put(Keys.block_num, String.valueOf(block.getNum()));
        fields.put(Keys.trial_in_block, String.valueOf(trial.getNum()));
        fields.put(Keys.time_stamp, trial.getTimeStamp());
        Playable p = trial.getPlayable();
        AnimationSequence as = trial.getAnimationSequence();
        fields.put(Keys.audioFile, p != null ? p.name() : "N/A");
        fields.put(Keys.visFile, as != null ? as.getSourceFileName() : "N/A");
        fields.put(Keys.audioOffset, String.valueOf(trial.getOffset()));
        fields.put(Keys.numDots, String.valueOf(trial.getNumPoints()));
        
        // Output subject response information
        Response response = trial.getResponse();
        fields.put(Keys.confidence, String.valueOf(response.getConfidence().ordinal()));
        fields.put(Keys.subjResponse, TOJResponseParameters.isDotFirst(response) ? "Dot" : "Tone");
        fields.put(Keys.responseCorrect, trial.isResponseCorrect() ? "Correct" : "Incorrect");

        return fields;
    }
    
    /**
     * Get the output file for the given file type.  Not valid for file types
     * needing block and trial information to calculate a file name.
     */
    public File getOutputFile(FileType type) throws UnsupportedOperationException {
    	return getOutputFile(getSession(), type, null, null);
    }
    
    /**
     * Get the output file for the given file type.  Not valid for file types
     * needing block and trial information to calculate a file name.
     */
    public static File getOutputFile(TOJSession ts, FileType type) throws UnsupportedOperationException {
    	if (type.needsBlockTrialData()) {
    		throw new UnsupportedOperationException(
    				String.format("File type %s needs block and trial information " +
    						"to calculate a file name.", type));
    	}
    	return getOutputFile(ts, type, null, null);
    }
    
    public File getOutputFile(FileType type, TOJBlock block, TOJTrial trial) {
    	return getOutputFile(getSession(), type, block, trial);
    }

    /**
     * Get the output file for the given file type.  Creates directories as
     * needed.
     */
    public static File getOutputFile(TOJSession ts, FileType type, TOJBlock block, TOJTrial trial) {
    	String fName = null;
    	switch(type) {
	    	case RESPONSE_ALL:
	    		fName = String.format("ex%s-responses.txt", ts.getExperimentID());
	    		break;
	    	case RESPONSE:
	    		fName = String.format("sub%s-sess%s-ex%s-subex%s-%s-responses.txt", ts.getSubject(), ts.getSession(),
	    				ts.getExperimentID(), ts.getSubExperimentID(), getTimeStamp());
	    		break;
	    	case DEBUG: 
	    		fName = String.format("sub%s-sess%s-ex%s-subex%s-%s-debug.log", ts.getSubject(), ts.getSession(),
	    				ts.getExperimentID(), ts.getSubExperimentID(), getTimeStamp());
	    		break;
    	}
    	return new File(getOutputDirectory(ts, type), fName);
    }
    
    /**
     * Get the output sub-directory for the given file type, starting
     * from the given base output directory.  Creates directories as
     * needed.
     */
    private static File getOutputDirectory(TOJSession ts, FileType type) {
    	switch(type) {
	    	case RESPONSE_ALL:
	    		return getCombinedFileDirectory(ts);
	    	case RESPONSE:
	    	case DEBUG: 
	    	default:
	    		return getSingleFileDirectory(ts);
    	}
    }
    
    /**
     * Get the directory in which single output files should be placed.
     */
    private static File getSingleFileDirectory(TOJSession ts) {
    	String dirName = String.format("%s-Individual" + File.separator + 
    			"Experiment %s" + File.separator + "Subject %s", ts.getExperimentBaseName(),
    			ts.getExperimentID(), ts.getSubject());
    	File dir = new File(getBaseOutputDirectory(), dirName);
    	if (!dir.exists()) dir.mkdirs();
    	return dir;
    }
    
    /**
     * Get the directory in which combined output files should be placed.
     */
    private static File getCombinedFileDirectory(TOJSession ts) {
    	File dir = new File(getBaseOutputDirectory(), 
    			String.format("%s-Composite", ts.getExperimentBaseName()));
    	if (!dir.exists()) dir.mkdirs();
    	return dir;
    }

}
