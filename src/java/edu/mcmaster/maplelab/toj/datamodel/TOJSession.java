package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import edu.mcmaster.maplelab.common.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.toj.TOJTrialLogger.FileType;
import edu.mcmaster.maplelab.toj.TOJDemoGUIPanel;
import edu.mcmaster.maplelab.toj.TOJExperiment;
import edu.mcmaster.maplelab.toj.TOJTrialLogger;

public class TOJSession extends Session<TOJBlock, TOJTrial, TOJTrialLogger> {
	private static final String AUDIO_META_FILE = "sound-metadata.properties";
	
	public enum SoundMeta {
		toneOnset
	}
	
	public enum ConfigKeys {
		screenWidth,
		screenHeight,
		animationPointAspect,
		dataFileName,
		pitches,
		frequencies,
		spectrums,
		envelopeDurations,
		strikeDurations,
		soundOffsets,
		numAnimationPoints,
		includeAudioBlock,
		includeVideoBlock,
		includeAudioVideoBlock,
		connectDots,
		speedMode
	}
	
	private Properties _audioFileMetaData = null;
	
	public TOJSession(Properties props) {
		super(props);
		
		int count = 0;
		count += includeAudioBlock() ? 1 : 0;
		count += includeVideoBlock() ? 1 : 0;
		count += includeAudioVideoBlock() ? 1 : 0;
		setNumBlocks(count);
	}

	@Override
	public File getDebugLogFile() {
		return getTrialLogger().getOutputFile(FileType.DEBUG);
	}

	@Override
	public String getExperimentBaseName() {
		return TOJExperiment.EXPERIMENT_BASENAME;
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
	
	/**
	 * Get the aspect ratio multiplier, if set.
	 */
	public float getAnimationPointAspect() {
		return getFloat(ConfigKeys.animationPointAspect, 1.0f);
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
	
	public File getExpectedAudioSubDir() {
		return new File(getDataDir(), "aud");
	}
	
	public File getExpectedVisualSubDir() {
		return new File(getDataDir(), "vis");
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
	
	public List<String> getSpectrums() {
		return getStringList(ConfigKeys.spectrums, "Puretone");
	}
	
	public List<String> getEnvelopeDurations() {
		return getStringList(ConfigKeys.envelopeDurations, "Flat-360ms");
	}
	
	public List<String> getStrikeDurations() {
		return getStringList(ConfigKeys.strikeDurations, DurationEnum.NORMAL.codeString());
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
	
	/**
	 * Get a description of all of the parameters that will contribute to block and
	 * trial combinatorial generation.
	 */
	public String getCombinatorialDescription() {
		// blocks
		String blockTypes = "";
		blockTypes += includeAudioBlock() ? 
				"\t\t\t" + AVBlockType.AUDIO_ONLY.getUIName() + " block\n" : "";
		blockTypes += includeVideoBlock() ? 
				"\t\t\t" + AVBlockType.VIDEO_ONLY.getUIName() + " block\n" : "";
		blockTypes += includeAudioVideoBlock() ? 
				"\t\t\t" + AVBlockType.AUDIO_VIDEO.getUIName() + " block\n" : "";
		blockTypes = String.format("\t%d block(s), repeated %d time(s), includes:\n", 
						getNumBlocks(), getBlockSetRepetitions()) + blockTypes;
		
		// parameters
		List<String> strikes = getStrikeDurations();
		List<NotesEnum> pitches = getPitches();
		List<String> freq = getFrequencies();
		List<String> spec = getSpectrums();
		List<String> envDur = getEnvelopeDurations();
		List<Long> offsets = getSoundOffsets();
		List<Integer> points = getNumAnimationPoints();
		
		// trial counts
		int audioOnly = freq.size() * spec.size() * envDur.size() * offsets.size();
		int animation = strikes.size() * pitches.size() * points.size() * audioOnly;
		int video = 0; //TODO: video trials?
		if (!includeAudioBlock()) audioOnly = 0;
		if (!includeAudioVideoBlock()) animation = 0;
		
		return String.format("\n********** Experiment Session Trial Details **********\n%s\n" +
				(includeAudioBlock() ? 
						String.format("\tAudio-only trial count: %d\n", audioOnly) : "") +
				(includeAudioVideoBlock() ? 
						String.format("\tAudio and animation trial count: %d\n", animation) : "") +
				(includeVideoBlock() ? 
						String.format("\tVideo trial count: %d\n", video) : "") +
				String.format("\tTotal trials: %d\n\n", audioOnly + animation + video) +
				"\tAuditory offsets: %s\n\tAudio data:\n" +
				"\t\tFrequencies: %s\n\t\tSpectra: %s\n" +
				"\t\tEnvelope/Durations: %s\n\tAnimation data:\n" +
				"\t\tDurations: %s\n\t\tPitches: %s\n" +
				"\t\tAnimation points: %s\n" +
				"**************************************************\n\n", blockTypes,
				listString(offsets), listString(freq), listString(spec),
				listString(envDur, 3, 2), listString(strikes), listString(pitches), 
				listString(points));
	}
	
	private static String listString(List<?> list) {
		return listString(list, -1, 0);
	}
	
	private static String listString(List<?> list, int breakAfter, int tabs) {
		String retval = "[";
		for (int i = 0; i < list.size(); i++) {
			if (breakAfter > 0 && i > 0 && i % breakAfter == 0) {
				retval += "\n";
				for (int j = 0; j <= tabs; j++) {
					retval += "\t";
				}
			}
			retval += list.get(i).toString() + ", ";
		}
		return retval.substring(0, retval.length()-2) + "]";
	}
	
	/**
     * Generate the experiment blocks. The number of blocks is determined by 
     * the types included from the properties.
     */
    public List<TOJBlock> generateBlocks() {

        List<TOJBlock> retval = new ArrayList<TOJBlock>();
        
        if (includeAudioBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_ONLY, getStrikeDurations(), getPitches(), 
        			getFrequencies(), getSpectrums(), getEnvelopeDurations(), getSoundOffsets(), 
        			getNumAnimationPoints()));
        }
        if (includeVideoBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.VIDEO_ONLY, getStrikeDurations(), getPitches(), 
        			getFrequencies(), getSpectrums(), getEnvelopeDurations(), getSoundOffsets(), 
        			getNumAnimationPoints()));
        }
        if (includeAudioVideoBlock()) {
        	retval.add(new TOJBlock(this, 0, AVBlockType.AUDIO_VIDEO, getStrikeDurations(), getPitches(), 
        			getFrequencies(), getSpectrums(), getEnvelopeDurations(), getSoundOffsets(), 
        			getNumAnimationPoints()));
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
		return new TOJBlock(this, 0, AVBlockType.AUDIO_VIDEO, 
				Arrays.asList(DurationEnum.NORMAL.codeString()), 
				Arrays.asList(NotesEnum.D), 
				Arrays.asList("330Hz"),
				Arrays.asList("Puretone"),
				Arrays.asList("Flat-360ms"),
				Arrays.asList((long) 0), 
				Arrays.asList(5));
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
