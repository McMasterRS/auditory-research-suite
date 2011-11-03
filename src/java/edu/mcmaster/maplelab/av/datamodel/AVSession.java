package edu.mcmaster.maplelab.av.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.datamodel.Block;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

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
		envelopeDurations,
		visualDurations,
		audioDurations,
		soundOffsets,
		numAnimationPoints,
		includeAudioBlock,
		includeVideoBlock,
		includeAudioAnimationBlock,
		connectDots,
		videoFileExtensions
	}
	
	private Properties _audioFileMetaData = null;

	protected AVSession(Properties props) {
		super(props);
		
		int count = 0;
		count += includeAudioBlock() ? 1 : 0;
		count += includeVideoBlock() ? 1 : 0;
		count += includeAudioAnimationBlock() ? 1 : 0;
		setNumBlocks(count);
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
			File dir = getExpectedAudioSubDir();
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
	
	public List<String> getVideoFileExtensions() {
		return getStringList(ConfigKeys.videoFileExtensions, "avi");
	}
	
	public List<String> getAudioFileExtensions() {
		return Arrays.asList("wav");
	}
	
	public File getExpectedAudioSubDir() {
		return new File(getDataDir(), "aud");
	}
	
	public File getExpectedAnimationSubDir() {
		return new File(getDataDir(), "vis");
	}
	
	public File getExpectedVideoSubDir() {
		return new File(getDataDir(), "video");
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
	
	public List<String> getFrequencies() {
		return getStringList(ConfigKeys.frequencies, "330Hz");
	}
	
	public List<String> getSpectra() {
		return getStringList(ConfigKeys.spectra, "Puretone");
	}
	
	public List<EnvelopeDuration> getEnvelopeDurations() {
		List<EnvelopeDuration> retval = new ArrayList<EnvelopeDuration>();
		for (String s : getStringList(ConfigKeys.envelopeDurations, "Flat-360ms")) {
			retval.add(new EnvelopeDuration(s));
		}
		return retval;
	}
	
	public List<DurationEnum> getVisualDurations() {
		return getDurations(getStringList(ConfigKeys.visualDurations, 
				DurationEnum.NORMAL.codeString()));
	}
	
	public List<DurationEnum> getAudioDurations() {
		return getDurations(getStringList(ConfigKeys.audioDurations, 
				DurationEnum.NORMAL.codeString()));
	}
	
	private List<DurationEnum> getDurations(List<String> strVals) {
		List<DurationEnum> retval = new ArrayList<DurationEnum>();
		for (String s: strVals) {
			DurationEnum dur = null;
			try {
				dur = DurationEnum.valueOf(s);
			}
			catch (Exception e) {
				dur = DurationEnum.fromCodeString(s);
			}
			if (dur != null) retval.add(dur);
		}
		return retval;
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
		List<DurationEnum> visDur = getVisualDurations();
		List<NotesEnum> pitches = getPitches();
		List<String> freq = getFrequencies();
		List<String> spec = getSpectra();
		List<EnvelopeDuration> envDur = getEnvelopeDurations();
		List<DurationEnum> audDur = getAudioDurations();
		List<String> vidExt = getVideoFileExtensions();
		List<Long> offsets = getSoundOffsets();
		List<Integer> points = getNumAnimationPoints();
		
		// trial counts
		int audioFiles = !isUsingLegacyAudioFiles() ? 
				freq.size() * spec.size() * envDur.size() : pitches.size() * audDur.size();
		int audioOnly = audioFiles * offsets.size();
		int animation = visDur.size() * pitches.size() * points.size() * audioOnly;
		int video = pitches.size() * visDur.size() * audDur.size();
		if (!includeAudioBlock()) audioOnly = 0;
		if (!includeAudioAnimationBlock()) animation = 0;
		if (!includeVideoBlock()) video = 0;
		
		String pitchString = listString(pitches);
		String visDurString = listString(visDur);
		String audDurString = listString(audDur);
		
		String audioFormat = isUsingLegacyAudioFiles() ? 
				"\tAudio data:\n\t\tPitches: %s\n\t\tDurations: %s\n" +
						"\tAuditory offsets: %s\n": 
				"\tAudio data:\n\t\tFrequencies: %s\n\t\tSpectra: %s\n" +
						"\t\tEnvelope/Durations: %s\n\t\tAuditory offsets: %s\n";
		String audioString = isUsingLegacyAudioFiles() ? 
				String.format(audioFormat, pitchString, audDurString, listString(offsets)) :
				String.format(audioFormat, listString(freq), listString(spec), listString(envDur, 3, 2), 
						listString(offsets));
		if (!includeAudioAnimationBlock() && !includeAudioBlock()) audioString = "";
				
		String aniFormat = "\tAnimation data:\n\t\tPitches: %s\n\t\tVisual durations: %s\n" +
				"\t\tAnimation points: %s\n";
		String aniString = String.format(aniFormat, pitchString, visDurString, listString(points));
		if (!includeAudioAnimationBlock()) aniString = "";
		
		String vidFormat = "\tVideo data:\n\t\tPitches: %s\n\t\tVisual durations: %s\n" +
				"\t\tAudio durations: %s\n\t\tVideo extensions: %s\n";
		String vidString = String.format(vidFormat, pitchString, visDurString, audDurString,
				listString(vidExt));
		if (!includeVideoBlock()) vidString = "";
		
		return String.format("\n********** Experiment Session Trial Details **********\n%s\n" +
				(includeAudioBlock() ? 
						String.format("\tAudio-only trial count: %d\n", audioOnly) : "") +
				(includeAudioAnimationBlock() ? 
						String.format("\tAudio and animation trial count: %d\n", animation) : "") +
				(includeVideoBlock() ? 
						String.format("\tVideo trial count: %d\n", video) : "") +
				String.format("\tTotal trials: %d\n\n", audioOnly + animation + video) + 
				audioString + aniString + vidString +
				"**************************************************\n\n", blockTypes);
	}

	@Override
	public abstract String getExperimentBaseName();
	
	@Override
    public abstract List<B> generateBlocks();

    @Override
 	public abstract B generateWarmup();

	@Override
    public abstract DemoGUIPanel<?, T> getExperimentDemoPanel(); 
}
