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
import java.util.EnumSet;
import java.util.Set;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.common.datamodel.FileType;
import edu.mcmaster.maplelab.common.datamodel.Response;
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
        audioFile,
        visFile,
        audioOffset, 
        numDots, 
        animationStart,
        aniStrikeDelay,
        audioStart,
        audioToneDelay,
        confidence, 
        subjResponse,
        responseCorrect
    }

    public TOJTrialLogger(TOJSession session, File workingDirectory) throws IOException {
		super(session, workingDirectory);
	}

	@Override
	protected void loadAdditionalFileTypes() {
		// none needed
	}

	@Override
    protected File getCollectedOutputFile() {
    	return getOutputFile(FileType.get(RESPONSE_ALL_FILE));
    }
    
    @Override
    protected File createFile() {
    	return getOutputFile(FileType.get(RESPONSE_FILE));
    }

	@Override
	protected Set<? extends Enum<?>> getGeneralDataHeaders() {
		return null;
	}

	@Override
	protected EnumMap<? extends Enum<?>, String> marshalGeneralDataToMap(
			TOJBlock block, TOJTrial trial) {
		return null;
	}

	@Override
	protected Set<? extends Enum<?>> getTrialDataHeaders() {
		return EnumSet.allOf(Keys.class);
	}

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(TOJBlock block, 
    		TOJTrial trial) {
    	
        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);
        
        // Calculate trial parameters
        Playable p = trial.getAudioPlayable();
        AnimationSequence as = trial.getAnimationSequence();
        fields.put(Keys.audioFile, p != null ? p.name() : "N/A");
        fields.put(Keys.visFile, as != null ? as.getSourceFileName() : "N/A");
        fields.put(Keys.audioOffset, String.valueOf(trial.getOffset()));
        fields.put(Keys.numDots, String.valueOf(trial.getNumPoints()));
        Long start = trial.getLastAnimationStart();
        fields.put(Keys.animationStart, start != null ? String.valueOf(start) : "N/A");
        fields.put(Keys.aniStrikeDelay, String.valueOf(trial.getAnimationStrikeTime()));
        start = trial.getLastMediaStart();
        fields.put(Keys.audioStart, start != null ? String.valueOf(start) : "N/A");
        fields.put(Keys.audioToneDelay, String.valueOf(trial.getAudioToneOnset()));
        
        // Output subject response information
        Response response = trial.getResponse();
        fields.put(Keys.confidence, String.valueOf(response.getConfidence().ordinal()));
        fields.put(Keys.subjResponse, TOJResponseParameters.isDotFirst(response) ? "Dot" : "Tone");
        fields.put(Keys.responseCorrect, trial.isResponseCorrect() ? "Correct" : "Incorrect");

        return fields;
    }

}
