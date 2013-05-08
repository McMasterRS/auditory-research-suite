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
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

public class SITrialLogger extends FileTrialLogger<SISession, SITrial> {

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
	protected EnumMap<? extends Enum<?>, String> marshalGeneralDataToMap(SITrial trial) {
		return null;
	}

	@Override
	protected Set<? extends Enum<?>> getTrialDataHeaders() {
		return EnumSet.allOf(Keys.class);
	}

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(SITrial trial) {
    	
        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);
        
        // Calculate trial parameters
        boolean vid = trial.isVideo();
        Playable media = vid ? trial.getVideoPlayable() : trial.getAudioPlayable();
        AnimationSequence as = trial.getAnimationSequence();
        fields.put(Keys.audioFile, !vid && media != null ? media.name() : NA);
        fields.put(Keys.vidFile, vid && media != null ? media.name() : NA);
        fields.put(Keys.visFile, as != null ? as.getSourceFileName() : NA);
        Long millisVal = TimeUnit.MILLISECONDS.convert(
        		trial.getOffsetNanos(), TimeUnit.NANOSECONDS);
        fields.put(Keys.audioOffset, String.valueOf(millisVal));
        fields.put(Keys.numDots, String.valueOf(trial.getNumPoints()));
        if (!vid) {
            Long start = trial.getLastAnimationStartNanos();
            if (start != null) {
                millisVal = TimeUnit.MILLISECONDS.convert(start, TimeUnit.NANOSECONDS);
                fields.put(Keys.animationStart, String.valueOf(millisVal));
                millisVal = TimeUnit.MILLISECONDS.convert(
                		trial.getAnimationStrikeTimeNanos(), TimeUnit.NANOSECONDS);
                fields.put(Keys.aniStrikeDelay, String.valueOf(millisVal));
            }
            else {
                fields.put(Keys.animationStart, NA);
                fields.put(Keys.aniStrikeDelay, NA);
            }
            start = trial.getLastMediaStartNanos();
            millisVal = start != null ? 
            		TimeUnit.MILLISECONDS.convert(start, TimeUnit.NANOSECONDS) : null;
            fields.put(Keys.audioStart, start != null ? String.valueOf(millisVal) : NA);
            millisVal = TimeUnit.MILLISECONDS.convert(
            		trial.getAudioToneOnsetNanos(), TimeUnit.NANOSECONDS);
            fields.put(Keys.audioToneDelay, String.valueOf(millisVal));
        }
        else {
            fields.put(Keys.animationStart, NA);
            fields.put(Keys.aniStrikeDelay, NA);
            fields.put(Keys.audioStart, NA);
            fields.put(Keys.audioToneDelay, NA);
        }
        
        // Output subject response information
        MultiResponse response = trial.getResponse();
        IntegerResponse dr = (IntegerResponse) response.getResponse(0);
        if (dr != null) {
            fields.put(Keys.subjDurationResponse, dr.getAnswer().toString());
            fields.put(Keys.subjDurationValue, String.valueOf(dr.getValue()));
        }
        else {
            fields.put(Keys.subjDurationResponse, NA);
            fields.put(Keys.subjDurationValue, NA);
        }
        IntegerResponse ir = (IntegerResponse) response.getResponse(1);
        if (ir != null) {
            fields.put(Keys.subjAgreementResponse, ir.getAnswer().toString());
            fields.put(Keys.subjAgreementValue, String.valueOf(ir.getValue()));
        }
        else {
            fields.put(Keys.subjAgreementResponse, NA);
            fields.put(Keys.subjAgreementValue, NA);
        }

        return fields;
    }

}
