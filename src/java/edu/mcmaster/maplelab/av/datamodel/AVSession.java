package edu.mcmaster.maplelab.av.datamodel;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.av.media.MediaParams;
import edu.mcmaster.maplelab.av.media.MediaType;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.datamodel.Trial;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;

public abstract class AVSession<TM extends AVTrialManager<?, T>, T extends AVTrial<?>, 
										L extends TrialLogger<T>> extends Session<TM, T, L> {
	
	private static final String AUDIO_META_FILE = "sound-metadata.properties";
	
	public enum SoundMeta {
		toneOnset
	}
	
	public enum ConfigKeys {
		screenWidth,
		screenHeight,
		animationPointAspect,
		animationPointSize,
		soundOffsets,
		numAnimationPoints,
		includeAudioBlock,
		singleAudioBlock,
		singleAudioFullRandom,
		includeVideoBlock,
		singleVideoBlock,
		singleVideoFullRandom,
		includeAnimationBlock,
		singleAnimationBlock,
		singleAnimationFullRandom,
		includeAudioAnimationBlock,
		singleAudioAnimationBlock,
		singleAudioAnimationFullRandom,
		connectDots,
		oscilloscopeSensorMode,
		animationFrameAdvance,
		audioCallAhead,
		renderCallAhead,
		videoFileSubDirectory,
		animationFileSubDirectory,
		audioFileSubDirectory,
		videoFileExtensions,
		animationFileExtensions,
		audioFileExtensions,
		synchronizeParameters,
		audioPollWait
	}
	
	private Properties _audioFileMetaData = null;

	protected AVSession(Properties props) {
		super(props);
		
		// must load the media parameter specification
		MediaParams.loadMediaParams(this);
	}
	
	/**
	 * Get the animation frame look ahead time.
	 */
	public Long getAnimationFrameAdvance() {
		return getLong(ConfigKeys.animationFrameAdvance, 0);
	}
	
	/**
	 * Get the audio call ahead time.
	 */
	public Long getAudioCallAhead() {
		return getLong(ConfigKeys.audioCallAhead, 0);
	}
	
	/**
	 * Get the rendering call ahead time.
	 */
	public Long getRenderCallAhead() {
		return getLong(ConfigKeys.renderCallAhead, 0);
	}
	
	/**
	 * Get the poll wait time for audio playback completion.
	 */
	public int getAudioPollWait() {
		return getInteger(ConfigKeys.audioPollWait, 0);
	}
	
	/**
	 * Get the tone onset time ('introductory silence') associated with the 
	 * given audio file.  If no introductory silence is associated with the 
	 * given file, the return value will be zero.
	 */
	public Long getToneOnsetTime(File file) {
		if (file == null || !file.exists()) return null;
		return getToneOnsetTime(file.getName());
	}
	
	/**
	 * Get the tone onset time ('introductory silence') associated with the 
	 * given audio file.  If no introductory silence is associated with the 
	 * given file, the return value will be zero.
	 */
	public Long getToneOnsetTime(String fileName) {
		if (_audioFileMetaData == null) {
			File dir = getAudioDirectory();
			File propFile = new File(dir, AUDIO_META_FILE);
			_audioFileMetaData = loadSecondaryProps(propFile);
		}
		
		if (fileName.contains(File.separator)) {
			fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1);
		}
		
		long retval = 0;
		String s = fileName + "." + SoundMeta.toneOnset.name();
        Object val = _audioFileMetaData.getProperty(s);
        if (val instanceof String) {
            try {
                retval = Long.parseLong((String)val);
            }
            catch (NumberFormatException ex) {
            }
        }
        else if (val instanceof Long) {
            retval = ((Long)val).longValue();
        }
        return retval;
	}

	@Override
	public String toPropertiesString() {
		String retval = toPropertiesStringWithEnum(EnumSet.allOf(Session.ConfigKeys.class));
        retval += toPropertiesStringWithEnum(EnumSet.allOf(ConfigKeys.class));
        retval += toPropertiesStringWithEnum(EnumSet.allOf(MediaType.ParameterKeys.class));
        for (MediaType<?> mt : MediaType.values()) {
        	for (String param : mt.getParams(this)) {
        		retval += toPropertiesStringWithStrings(false, param, 
        				MediaParams.LabelKeys.LABEL.getKey(param), 
        				MediaParams.LabelKeys.VAL_LABELS.getKey(param));
        	}
        }
        
        return retval;
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
	
	/**
	 * Get the aspect ratio multiplier, if set.
	 */
	public float getAnimationPointAspect() {
		return getFloat(ConfigKeys.animationPointAspect, 1.0f);
	}
	
	/**
	 * Get the base animation point size, if set.
	 */
	public float getBaseAnimationPointSize() {
		return getFloat(ConfigKeys.animationPointSize, 0.1f);
	}
	
	public boolean includeAudioBlock() {
		return getBoolean(ConfigKeys.includeAudioBlock, true);
	}
	
	public boolean singleAudioBlock() {
		return getBoolean(ConfigKeys.singleAudioBlock, false);
	}
	
	public boolean singleAudioFullRandom() {
		return getBoolean(ConfigKeys.singleAudioFullRandom, true);
	}
	
	public boolean includeVideoBlock() {
		return getBoolean(ConfigKeys.includeVideoBlock, true);
	}
	
	public boolean singleVideoBlock() {
		return getBoolean(ConfigKeys.singleVideoBlock, false);
	}
	
	public boolean singleVideoFullRandom() {
		return getBoolean(ConfigKeys.singleVideoFullRandom, true);
	}
	
	public boolean includeAnimationBlock() {
		return getBoolean(ConfigKeys.includeAnimationBlock, true);
	}
	
	public boolean singleAnimationBlock() {
		return getBoolean(ConfigKeys.singleAnimationBlock, false);
	}
	
	public boolean singleAnimationFullRandom() {
		return getBoolean(ConfigKeys.singleAnimationFullRandom, true);
	}
	
	public boolean includeAudioAnimationBlock() {
		return getBoolean(ConfigKeys.includeAudioAnimationBlock, true);
	}
	
	public boolean singleAudioAnimationBlock() {
		return getBoolean(ConfigKeys.singleAudioAnimationBlock, false);
	}
	
	public boolean singleAudioAnimationFullRandom() {
		return getBoolean(ConfigKeys.singleAudioAnimationFullRandom, true);
	}
	
	public boolean connectDots() {
		return getBoolean(ConfigKeys.connectDots, true);
	}
	
	public boolean synchronizeParameters() {
		return getBoolean(ConfigKeys.synchronizeParameters, true);
	}
	
	public List<String> getVideoFileExtensions() {
		return getStringList(ConfigKeys.videoFileExtensions, "avi");
	}
	
	public List<String> getAnimationFileExtensions() {
		return getStringList(ConfigKeys.animationFileExtensions, "txt");
	}
	
	public List<String> getAudioFileExtensions() {
		return getStringList(ConfigKeys.audioFileExtensions, "wav");
	}
	
	private File getSubDirectory(String subDir) {
		File retval = new File(getDataDir(), subDir);
		// Allow non-existent directory, but not an existing non-directory file.
		return retval.isDirectory() || !retval.exists() ? retval : null;
	}
	
	public File getVideoDirectory() {
		return getSubDirectory(getString(ConfigKeys.videoFileSubDirectory, "video"));
	}
	
	public File getAnimationDirectory() {
		return getSubDirectory(getString(ConfigKeys.animationFileSubDirectory, "vis"));
	}
	
	public File getAudioDirectory() {
		return getSubDirectory(getString(ConfigKeys.audioFileSubDirectory, "aud"));
	}
	
	public List<Long> getSoundOffsets() {
		List<Long> offsets = getLongList(ConfigKeys.soundOffsets, new Long[]{(long) 0});
		return offsets;
	}
	
	public List<Integer> getNumAnimationPoints() {
		List<Integer> intList= getIntegerList(ConfigKeys.numAnimationPoints);
		return intList;
	}
	
	/**
	 * Determine if we are testing A/V synchronization with oscilloscope, for demo code to
	 * position windows appropriately.
	 */
	public boolean isOscilloscopeSensorMode() {
	    return getBoolean(ConfigKeys.oscilloscopeSensorMode, false);
	}

	@Override
	public abstract String getExperimentBaseName();
	
	@Override
	public abstract TM initializeTrialManager(boolean warmup);

	@Override
    public abstract DemoGUIPanel<?, T> getExperimentDemoPanel(); 
}
