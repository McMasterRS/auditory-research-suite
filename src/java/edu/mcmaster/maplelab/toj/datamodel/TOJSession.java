package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static edu.mcmaster.maplelab.common.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.toj.TOJTrialLogger;

public class TOJSession extends Session<TOJBlock, TOJTrial, TOJTrialLogger> {
	
	public enum ConfigKeys {
		screenWidth,
		screenHeight,
		dataFileName,
		includeVisualStimuli,
		pitches,
		toneDurations,
		strikeDurations,
		soundOffsets,
		numAnimationPointsArray,
		includeAudioBlock,
		includeVideoBlock,
		includeAudioVideoBlock
	}
	
	protected TOJSession(Properties props) {
		super(props);
		// TODO Auto-generated constructor stub
	}

	@Override
	public File getDebugLogFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExperimentBaseName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toPropertiesString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Set the screen width.
	 */
	public void setScreenWidth(int width) {
		setProperty(ConfigKeys.screenWidth, width);
	}
	
	/**
	 * Set the screen width.
	 */
	public void setScreenHeight(int height) {
		setProperty(ConfigKeys.screenHeight, height);
	}

	/**
	 * Set the screen width.
	 */
	public void setDataFileName(String name) {
		setProperty(ConfigKeys.dataFileName, name);
	}
	
	/**
	 * Set the screen width.
	 */
	public void setIncludeVisualStimuli(boolean include) {
		setProperty(ConfigKeys.includeVisualStimuli, include);
	}
	
	/**
	 * Get the screen width.
	 */
	public int getScreenWidth() {
		return getInteger(ConfigKeys.screenWidth, 720);
	}
	
	/**
	 * Get the screen width.
	 */
	public int getScreenHeight() {
		return getInteger(ConfigKeys.screenHeight, 480);
	}
	
	/**
	 * Get the screen width.
	 */
	public String getDataFileName() {
		return getString(ConfigKeys.dataFileName, null);
	}
	
	/**
	 * Get the screen width.
	 */
	public boolean includeVisualStimuli() {
		return getBoolean(ConfigKeys.includeVisualStimuli, true);
	}
	
	public boolean includeAudioBlock() {
		return getBoolean(ConfigKeys.includeAudioBlock, true);
	}
	
	public boolean includeVideoBlock() {
		return getBoolean(ConfigKeys.includeVideoBlock, true);
	}
	
	public boolean includeAudioVideoBlock() {
		return getBoolean(ConfigKeys.includeAudioVideoBlock, true);
	}

	/**
     * Generate the experiment blocks. The number of blocks is determined by 
     * the types included from the properties.
     */
    public List<TOJBlock> generateBlocks() {

        List<TOJBlock> retval = new ArrayList<TOJBlock>();
        
        if (includeAudioBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_ONLY));
        }
        if (includeVideoBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.VIDEO_ONLY));
        }
        if (includeAudioVideoBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_VIDEO));
        }
        
        // Shuffle and renumber blocks
        if (randomizeBlocks()) Collections.shuffle(retval);
        for (int i = 0; i < retval.size(); i++) {
       	 	retval.get(i).setNum(i+1);
        }
        
        return retval;
    }

	public TOJBlock generateWarmup() {
		// TODO Auto-generated method stub
		return null;
	}
}
