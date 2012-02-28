package edu.mcmaster.maplelab.si;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.common.datamodel.FileType;
import edu.mcmaster.maplelab.common.datamodel.IntegerResponse;
import edu.mcmaster.maplelab.common.datamodel.MultiResponse;
import edu.mcmaster.maplelab.si.datamodel.SIBlock;
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

public class SITrialLogger extends FileTrialLogger<SISession, SIBlock, SITrial> {

	public enum Keys {
        audioFile,
        vidFile,
        visFile,
        audioOffset, 
        numDots, 
        animationStart,
        aniStrikeDelay,
        audioStart,
        audioToneDelay,
        subjDurationResponse,
        subjDurationValue,
        subjAgreementResponse,
        subjAgreementValue
    }

	public SITrialLogger(SISession session, File workingDirectory) throws IOException {
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
			SIBlock block, SITrial trial) {
		return null;
	}

	@Override
	protected Set<? extends Enum<?>> getTrialDataHeaders() {
		return EnumSet.allOf(Keys.class);
	}

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(SIBlock block, 
    		SITrial trial) {
    	
        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);
        
        // Calculate trial parameters
        boolean vid = trial.isVideo();
        Playable media = vid ? trial.getVideoPlayable() : trial.getAudioPlayable();
        AnimationSequence as = trial.getAnimationSequence();
        fields.put(Keys.audioFile, !vid && media != null ? media.name() : "N/A");
        fields.put(Keys.vidFile, vid && media != null ? media.name() : "N/A");
        fields.put(Keys.visFile, as != null ? as.getSourceFileName() : "N/A");
        long millisVal = TimeUnit.MILLISECONDS.convert(
        		trial.getOffsetNanos(), TimeUnit.NANOSECONDS);
        fields.put(Keys.audioOffset, String.valueOf(millisVal));
        fields.put(Keys.numDots, String.valueOf(trial.getNumPoints()));
        if (!vid) {
            Long start = trial.getLastAnimationStartNanos();
            millisVal = start != null ? 
            		TimeUnit.MILLISECONDS.convert(start, TimeUnit.NANOSECONDS) : null;
            fields.put(Keys.animationStart, start != null ? String.valueOf(millisVal) : "N/A");
            millisVal = TimeUnit.MILLISECONDS.convert(
            		trial.getAnimationStrikeTimeNanos(), TimeUnit.NANOSECONDS);
            fields.put(Keys.aniStrikeDelay, String.valueOf(millisVal));
            start = trial.getLastMediaStartNanos();
            millisVal = start != null ? 
            		TimeUnit.MILLISECONDS.convert(start, TimeUnit.NANOSECONDS) : null;
            fields.put(Keys.audioStart, start != null ? String.valueOf(millisVal) : "N/A");
            millisVal = TimeUnit.MILLISECONDS.convert(
            		trial.getAudioToneOnsetNanos(), TimeUnit.NANOSECONDS);
            fields.put(Keys.audioToneDelay, String.valueOf(millisVal));
        }
        else {
            fields.put(Keys.animationStart, "N/A");
            fields.put(Keys.aniStrikeDelay, "N/A");
            fields.put(Keys.audioStart, "N/A");
            fields.put(Keys.audioToneDelay, "N/A");
        }
        
        // Output subject response information
        MultiResponse response = trial.getResponse();
        IntegerResponse dr = (IntegerResponse) response.getResponse(0);
        IntegerResponse ir = (IntegerResponse) response.getResponse(1);
        fields.put(Keys.subjDurationResponse, dr.getAnswer().toString());
        fields.put(Keys.subjDurationValue, String.valueOf(dr.getValue()));
        fields.put(Keys.subjAgreementResponse, ir.getAnswer().toString());
        fields.put(Keys.subjAgreementValue, String.valueOf(ir.getValue()));

        return fields;
    }

}
