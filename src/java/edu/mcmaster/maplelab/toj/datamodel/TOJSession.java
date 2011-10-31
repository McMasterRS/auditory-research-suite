package edu.mcmaster.maplelab.toj.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.toj.TOJDemoGUIPanel;
import edu.mcmaster.maplelab.toj.TOJExperiment;
import edu.mcmaster.maplelab.toj.TOJTrialLogger;

public class TOJSession extends AVSession<TOJBlock, TOJTrial, TOJTrialLogger> {
	
	public TOJSession(Properties props) {
		super(props);
	}

	@Override
	public String getExperimentBaseName() {
		return TOJExperiment.EXPERIMENT_BASENAME;
	}
	
	/**
     * Generate the experiment blocks. The number of blocks is determined by 
     * the types included from the properties.
     */
    public List<TOJBlock> generateBlocks() {

        List<TOJBlock> retval = new ArrayList<TOJBlock>();
        
        if (includeAudioBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_ONLY, getVisualDurations(), getPitches(), 
        			getFrequencies(), getSpectra(), getEnvelopeDurations(), getAudioDurations(), 
        			getVideoFileExtensions(), getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeVideoBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.VIDEO_ONLY, getVisualDurations(), getPitches(), 
        			getFrequencies(), getSpectra(), getEnvelopeDurations(), getAudioDurations(), 
        			getVideoFileExtensions(), getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeAudioAnimationBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_ANIMATION, getVisualDurations(), getPitches(), 
        			getFrequencies(), getSpectra(), getEnvelopeDurations(), getAudioDurations(), 
        			getVideoFileExtensions(), getSoundOffsets(), getNumAnimationPoints()));
        }
        
        // Shuffle and renumber blocks
        if (randomizeBlocks()) Collections.shuffle(retval);
        for (int i = 0; i < retval.size(); i++) {
       	 	retval.get(i).setNum(i+1);
        }
        
        return retval;
    }

    /**
     * Create a single-trial, generic audio-video block for warmup.
     */
	public TOJBlock generateWarmup() {
		return new TOJBlock(this, 0, AVBlockType.AUDIO_ANIMATION, 
				Arrays.asList(DurationEnum.NORMAL), 
				Arrays.asList(NotesEnum.D), 
				Arrays.asList("330Hz"),
				Arrays.asList("Puretone"),
				Arrays.asList("Flat-360ms"),
				Arrays.asList(DurationEnum.NORMAL),
				Arrays.asList("avi"),
				Arrays.asList((long) 0), 
				Arrays.asList(5));
	}

	@Override
	public TOJDemoGUIPanel getExperimentDemoPanel() {
		return new TOJDemoGUIPanel(this);
	}
}
