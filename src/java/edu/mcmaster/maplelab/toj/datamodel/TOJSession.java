package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static edu.mcmaster.maplelab.common.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;

import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.toj.TOJDemoGUIPanel;
import edu.mcmaster.maplelab.toj.TOJTrialLogger;

public class TOJSession extends Session<TOJBlock, TOJTrial, TOJTrialLogger> {
	
	public enum ConfigKeys {
		screenWidth,
		screenHeight,
		dataFileName,
		pitches,
		toneDurations, // DurationEnum[]
		strikeDurations,
		soundOffsets,
		numAnimationPoints,
		includeAudioBlock,
		includeVideoBlock,
		includeAudioVideoBlock,
		connectDots,
		speedMode
	}
	
	public TOJSession(Properties props) {
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
	 * Set the screen height.
	 */
	public void setScreenHeight(int height) {
		setProperty(ConfigKeys.screenHeight, height);
	}

	/**
	 * Set the data file name.
	 */
	public void setDataFileName(String name) {
		setProperty(ConfigKeys.dataFileName, name);
	}
	
	/**
	 * Get the screen width.
	 */
	public int getScreenWidth() {
		return getInteger(ConfigKeys.screenWidth, 720);
	}
	
	/**
	 * Get the screen height.
	 */
	public int getScreenHeight() {
		return getInteger(ConfigKeys.screenHeight, 480);
	}
	
	public String getDataFileName() {
		return getString(ConfigKeys.dataFileName, null);
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
	
	public boolean connectDots() {
		return getBoolean(ConfigKeys.connectDots, true);
	}
	
	public List<NotesEnum> getPitches() {
		List<String> pitches = getStringList(ConfigKeys.pitches, "C");
		List<NotesEnum> retval = new ArrayList<NotesEnum>();
		for (String s: pitches) {
			try {
				NotesEnum nEnum = NotesEnum.valueOf(s);
				retval.add(nEnum);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retval;
	}
	
	public List<String> getToneDurations() {
		return getStringList(ConfigKeys.strikeDurations, DurationEnum.NORMAL.codeString());
	}
	
	public List<String> getStrikeDurations() {
		return getStringList(ConfigKeys.strikeDurations, DurationEnum.NORMAL.codeString());
	}
	
	public List<Float> getSoundOffsets() {
		List<Float> offsets = getFloatList(ConfigKeys.soundOffsets, new Float[]{0f});
		return offsets;
	}
	
	public List<Integer> getNumAnimationPointsArray() {
		List<Integer> intList= getIntegerList(ConfigKeys.numAnimationPoints);
		return intList;
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
    
    /**
    * Flag to indicate dividing IOIs by 10 to speed through stimulus for
    * testing.
    */
   public boolean isSpeedMode() {
       boolean retval = false;
       Object val = getProperty(ConfigKeys.speedMode);
       if(val instanceof String) {
           retval = Boolean.parseBoolean((String)val);
       }
       return retval;
   }

	@Override
	public TOJDemoGUIPanel getExperimentDemoPanel() {
		return new TOJDemoGUIPanel(this);
	}
}
