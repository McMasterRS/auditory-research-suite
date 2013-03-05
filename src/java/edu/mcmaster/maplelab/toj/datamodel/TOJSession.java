package edu.mcmaster.maplelab.toj.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVBlock.AVBlockType;
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
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_ONLY, getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeVideoBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.VIDEO_ONLY, getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeAnimationBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.ANIMATION_ONLY, getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeAudioAnimationBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_ANIMATION, getSoundOffsets(), getNumAnimationPoints()));
        }
        
        // Shuffle and renumber blocks
        if (randomizeBlocks()) Collections.shuffle(retval);
        for (int i = 0; i < retval.size(); i++) {
       	 	retval.get(i).setNum(i+1);
        }
        
        return retval;
    }

	@Override
	public TOJDemoGUIPanel getExperimentDemoPanel() {
		return new TOJDemoGUIPanel(this);
	}
}
