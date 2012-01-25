package edu.mcmaster.maplelab.av.datamodel;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.av.media.MediaParams;
import edu.mcmaster.maplelab.av.media.MediaType;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;

public abstract class AVSession<B extends AVBlock<?,?>, T extends AVTrial<?>, 
										L extends TrialLogger<B, T>> extends Session<B, T, L> {
	
	private static final String AUDIO_META_FILE = "sound-metadata.properties";
	
	public enum SoundMeta {
		toneOnset
	}
	
	public enum ConfigKeys {
		screenWidth,
		screenHeight,
		legacyAudioFiles,
		animationPointAspect,
		animationPointSize,
		pitches,
		frequencies,
		spectra,
		spectrums,
		envelopeDurations,
		visualDurations,
		audioDurations,
		soundOffsets,
		numAnimationPoints,
		includeAudioBlock,
		includeVideoBlock,
		includeAudioAnimationBlock,
		connectDots,
		videoFileSubDirectory,
		animationFileSubDirectory,
		audioFileSubDirectory,
		videoFileExtensions,
		animationFileExtensions,
		audioFileExtensions,
		synchronizeParameters,
		oscilloscopeSensorMode
	}
	
	private Properties _audioFileMetaData = null;

	protected AVSession(Properties props) {
		super(props);
		
		int count = 0;
		count += includeAudioBlock() ? 1 : 0;
		count += includeVideoBlock() ? 1 : 0;
		count += includeAudioAnimationBlock() ? 1 : 0;
		setNumBlocks(count);
		
		MediaParams.loadMediaParams(this);
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
            catch(NumberFormatException ex) {
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
	
	public boolean includeVideoBlock() {
		return getBoolean(ConfigKeys.includeVideoBlock, true);
	}
	
	public boolean includeAudioAnimationBlock() {
		return getBoolean(ConfigKeys.includeAudioAnimationBlock, true);
	}
	
	public boolean connectDots() {
		return getBoolean(ConfigKeys.connectDots, true);
	}
	
	public boolean isUsingLegacyAudioFiles() {
		return getBoolean(ConfigKeys.legacyAudioFiles, false);
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
	
	/**
	 * Get a description of all of the parameters that will contribute to block and
	 * trial combinatorial generation.
	 */
	@Override
	public String getCombinatorialDescription(List<B> blocks) {
		
		// blocks
		String blockTypes = "";
		blockTypes += includeAudioBlock() ? 
				"\t\t\t" + AVBlockType.AUDIO_ONLY.getUIName() + " block\n" : "";
		blockTypes += includeVideoBlock() ? 
				"\t\t\t" + AVBlockType.VIDEO_ONLY.getUIName() + " block\n" : "";
		blockTypes += includeAudioAnimationBlock() ? 
				"\t\t\t" + AVBlockType.AUDIO_ANIMATION.getUIName() + " block\n" : "";
		blockTypes = String.format("\t%d block(s), repeated %d time(s), includes:\n", 
						getNumBlocks(), getBlockSetRepetitions()) + blockTypes;
		
		// parameters
		List<String> audioParams = MediaType.AUDIO.getParams(this);
		List<String> animationParams = MediaType.ANIMATION.getParams(this);
		List<String> videoParams = MediaType.VIDEO.getParams(this);
		
		// additional experiment parameters
		List<Long> offsets = getSoundOffsets();
		List<Integer> points = getNumAnimationPoints();
		
		// trial counts - init to 1 for loop purposes, correct later
		int audioOnlyCount = 1;
		int audAniCount = 1;
		int videoCount = 1;
		
		// file location info
		File audDir = getAudioDirectory();
		File aniDir = getAnimationDirectory();
		File vidDir = getVideoDirectory();
		List<String> audExt = getAudioFileExtensions();
		List<String> aniExt = getAnimationFileExtensions();
		List<String> vidExt = getVideoFileExtensions();
		
		// inclusion checks and description construction
		String audioDesc = "";
		if (!includeAudioBlock() && !includeAudioAnimationBlock()) {
			audioOnlyCount = 0;
		}
		else {
			audioDesc = "\tAudio data:\n";
			for (String s : audioParams) {
				List<String> labels = getStringList(s + ".labels", (String[]) null);
				if (labels == null) {
					labels = getStringList(s, (String[]) null);
				}
				audioOnlyCount *= labels.size();
				String list = listString(labels, 3, 2);
				audioDesc += "\t\t" + getString(s + ".label", s) + " " + list + "\n";
			}
			audioOnlyCount *= offsets.size();
			audioDesc += String.format("\t\tAuditory offsets: %s\n", listString(offsets));
			audioDesc += String.format("\t\tAudio subdirectory: %s\n", audDir.getName());
			audioDesc += String.format("\t\tAudio extensions: %s\n", listString(audExt));
		}
		
		String aniDesc = "";
		if (!includeAudioAnimationBlock()) {
			audAniCount = 0;
		}
		else {
			audAniCount *= audioOnlyCount * points.size();
			boolean shared = synchronizeParameters();
			aniDesc = "\tAnimation data:\n";
			for (String s : animationParams) {
				List<String> labels = getStringList(s + ".labels", (String[]) null);
				if (labels == null) {
					labels = getStringList(s, (String[]) null);
				}
				// check to see if this item already counted
				if (!(shared && audioParams.contains(s))) {
					audAniCount *= labels.size();
				}
				String list = listString(labels, 3, 2);
				aniDesc += "\t\t" + getString(s + ".label", s) + " " + list + "\n";
			}
			aniDesc += String.format("\t\tAnimation points: %s\n", listString(points));
			aniDesc += String.format("\t\tAnimation subdirectory: %s\n", aniDir.getName());
			aniDesc += String.format("\t\tAnimation extensions: %s\n", listString(aniExt));
		}
		
		// audio correction, after used by animation
		if (!includeAudioBlock()) audioOnlyCount = 0;
		
		String videoDesc = "";
		if (!includeVideoBlock()) {
			videoCount = 0;
		}
		else {
			videoDesc = "\tVideo data:\n";
			for (String s : videoParams) {
				List<String> labels = getStringList(s + ".labels", (String[]) null);
				if (labels == null) {
					labels = getStringList(s, (String[]) null);
				}
				videoCount *= labels.size();
				String list = listString(labels, 3, 2);
				videoDesc += "\t\t" + getString(s + ".label", s) + " " + list + "\n";
			}
			videoDesc += String.format("\t\tVideo subdirectory: %s\n", vidDir.getName());
			videoDesc += String.format("\t\tVideo extensions: %s\n", listString(vidExt));
		}
		
		return String.format("\n********** Experiment Session Trial Details **********\n%s\n" +
				(includeAudioBlock() ? 
						String.format("\tAudio-only trial count: %d\n", audioOnlyCount) : "") +
				(includeAudioAnimationBlock() ? 
						String.format("\tAudio and animation trial count: %d\n", audAniCount) : "") +
				(includeVideoBlock() ? 
						String.format("\tVideo trial count: %d\n", videoCount) : "") +
				String.format("\tTotal trials: %d\n\n", audioOnlyCount + audAniCount + videoCount) + 
				audioDesc + aniDesc + videoDesc +
				"**************************************************\n\n", blockTypes);
	}

	@Override
	public abstract String getExperimentBaseName();
	
	@Override
    public abstract List<B> generateBlocks();

	/**
	 * Create warmup blocks.
	 */
    @Override
 	public List<B> generateWarmup() {

		List<B> retval = generateBlocks();
		int warmupCount = getNumWarmupTrials();
		int initialCount = 0;
		for (B block : retval) {
			initialCount += block.getNumTrials();
		}
		
		if (initialCount > warmupCount) {
			int remaining = warmupCount;
			for (int i = retval.size()-1; i > 0; i--) {
				B block = retval.get(i);
				int localCount = block.getNumTrials();
				if (localCount > 1) {
					float percent = (float) localCount / (float) initialCount;
					localCount = (int) (warmupCount * percent);
					block.clipTrials(localCount);
					remaining -= localCount;
				}
			}
			retval.get(0).clipTrials(remaining);
		}
		
		return retval;
    }

	@Override
    public abstract DemoGUIPanel<?, T> getExperimentDemoPanel(); 
}
