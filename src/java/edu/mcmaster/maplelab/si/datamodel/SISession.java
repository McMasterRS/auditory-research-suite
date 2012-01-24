package edu.mcmaster.maplelab.si.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.si.SIDemoGUIPanel;
import edu.mcmaster.maplelab.si.SIExperiment;
import edu.mcmaster.maplelab.si.SITrialLogger;

public class SISession extends AVSession<SIBlock, SITrial, SITrialLogger> {

	public SISession(Properties props) {
		super(props);
	}

	@Override
	public String getExperimentBaseName() {
		return SIExperiment.EXPERIMENT_BASENAME;
	}

	@Override
	public List<SIBlock> generateBlocks() {
        List<SIBlock> retval = new ArrayList<SIBlock>();
        
        if (includeAudioBlock()) {
        	retval.add(new SIBlock(this, 0, AVBlockType.AUDIO_ONLY, getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeVideoBlock()) {
        	retval.add(new SIBlock(this, 0, AVBlockType.VIDEO_ONLY, getSoundOffsets(), getNumAnimationPoints()));
        }
        if (includeAudioAnimationBlock()) {
        	retval.add(new SIBlock(this, 0, AVBlockType.AUDIO_ANIMATION, getSoundOffsets(), getNumAnimationPoints()));
        }
        
        // Shuffle and renumber blocks
        if (randomizeBlocks()) Collections.shuffle(retval);
        for (int i = 0; i < retval.size(); i++) {
       	 	retval.get(i).setNum(i+1);
        }
        
        return retval;
	}

	@Override
	public DemoGUIPanel<?, SITrial> getExperimentDemoPanel() {
		return new SIDemoGUIPanel(this);
	}

}
