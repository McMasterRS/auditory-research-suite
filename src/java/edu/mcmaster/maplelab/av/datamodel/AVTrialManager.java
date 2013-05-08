package edu.mcmaster.maplelab.av.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mcmaster.maplelab.av.media.MediaType;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaParams.MediaParamValue;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.datamodel.PredeterminedTrialManager;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.RelativeTrialPosition;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy;

/**
 * @author bguseman
 *
 */
public abstract class AVTrialManager<S extends AVSession<?, T, ?>, T extends AVTrial<?>> extends PredeterminedTrialManager<S, T> {
	
	private static boolean _initAudioCount = false;

	public AVTrialManager(S session, boolean warmup) {
		super(session, TrialHierarchy.BLOCK, warmup);
	}
	
	protected abstract T createTrial(AVBlockType type, AnimationSequence animationSequence, MediaWrapper<Playable> media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots, Long mediaDelay);

	@Override
	protected List<List<T>> generateMetaBlock(int metablock) {
        List<List<T>> retval = new ArrayList<List<T>>();
        
        // initialize common parameters
		S session = getSession();
		List<Long> offsets = session.getSoundOffsets();
		List<Integer> aniPoints = session.getNumAnimationPoints();
		float pointSize = session.getBaseAnimationPointSize();
		boolean connect = session.connectDots();
		
		// get all total combinations - init counts if we need all
		List<Map<String, MediaParamValue>> audioCombinations = MediaType.AUDIO.buildParameterMaps(session);
		List<Map<String, MediaParamValue>> animationCombinations = MediaType.ANIMATION.buildParameterMaps(session);
		if (session.includeAudioBlock() && !_initAudioCount) {
			MediaType.AUDIO.initializeCount(audioCombinations.size());
			MediaType.AUDIO.initializeWait(session.getAudioPollWait());
			_initAudioCount = true;
		}
		
		// iterate over number of repetitions
		int trialCount = 0;
		int reps = session.getBlockSetRepetitions();
		for (int i = 0; i < reps; i++) {
			
			// AUDIO
			if (session.includeAudioBlock()) {
				List<T> trials = new ArrayList<T>();
				
				boolean singleBlock = session.singleAudioBlock();
				boolean innerRandom = session.randomizeTrials() && singleBlock && 
						!session.singleAudioFullRandom();
				int innerReps = singleBlock ? (i == 0 ? reps : 0) : 1;
				for (int j = 0; j < innerReps; j++) {
					List<T> innerTrials = new ArrayList<T>();
					for (Map<String, MediaParamValue> map : audioCombinations) {
						MediaWrapper<Playable> audio = MediaType.AUDIO.createMedia(session, map.values());
						if (audio == null) continue;
						Long delay = session.getToneOnsetTime(audio.getName());
						for (Long so : offsets) {
							T trial = createTrial(AVBlockType.AUDIO_ONLY, null, audio, so, 0, 
									pointSize, connect, delay);
							trial.setNumber(RelativeTrialPosition.REPETITION, i + j + 1); // i or j must be 0
							trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
							innerTrials.add(trial);
							++trialCount;
						}
					}
					
					// randomize and add trials
					if (innerRandom) {
						Collections.shuffle(innerTrials);
					}
					trials.addAll(innerTrials);
				}
				if (session.randomizeTrials() && !innerRandom) {
					Collections.shuffle(trials);
				}
				
				// add block
				retval.add(trials);
			}
			
			// VIDEO
			if (session.includeVideoBlock()) {
				List<T> trials = new ArrayList<T>();
				
				boolean singleBlock = session.singleVideoBlock();
				boolean innerRandom = session.randomizeTrials() && singleBlock && 
						!session.singleVideoFullRandom();
				int innerReps = singleBlock ? (i == 0 ? reps : 0) : 1;
				List<Map<String, MediaParamValue>> videoCombinations = MediaType.VIDEO.buildParameterMaps(session);
				for (int j = 0; j < innerReps; j++) {
					List<T> innerTrials = new ArrayList<T>();
					for (Map<String, MediaParamValue> map : videoCombinations) {
						MediaWrapper<Playable> video = MediaType.VIDEO.createMedia(session, map.values());
						if (video == null) continue;
						T trial = createTrial(AVBlockType.VIDEO_ONLY, null, video, 0l, 0, 
								pointSize, connect, 0l);
						trial.setNumber(RelativeTrialPosition.REPETITION, i + j + 1); // i or j must be 0
						trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
						innerTrials.add(trial);
						++trialCount;
					}
					
					// randomize and add trials
					if (innerRandom) {
						Collections.shuffle(innerTrials);
					}
					trials.addAll(innerTrials);
				}
				if (session.randomizeTrials() && !innerRandom) {
					Collections.shuffle(trials);
				}
				
				// add block
				retval.add(trials);
			}
			
			// ANIMATION
			if (session.includeAnimationBlock()) {
				List<T> trials = new ArrayList<T>();
				
				boolean singleBlock = session.singleAnimationBlock();
				boolean innerRandom = session.randomizeTrials() && singleBlock && 
						!session.singleAnimationFullRandom();
				int innerReps = singleBlock ? (i == 0 ? reps : 0) : 1;
				for (int j = 0; j < innerReps; j++) {
					List<T> innerTrials = new ArrayList<T>();
					for (Map<String, MediaParamValue> animationMap : animationCombinations) {
						MediaWrapper<AnimationSequence> ani = 
								MediaType.ANIMATION.createMedia(session, animationMap.values());
						if (ani == null) continue;
						for (int pts : aniPoints) {
							T trial = createTrial(AVBlockType.ANIMATION_ONLY, ani.getMediaObject(), 
									null, 0l, pts, pointSize, connect, 0l);
							trial.setNumber(RelativeTrialPosition.REPETITION, i + j + 1); // i or j must be 0
							trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
							innerTrials.add(trial);
							++trialCount;
						}
					}
					
					// randomize and add trials
					if (innerRandom) {
						Collections.shuffle(innerTrials);
					}
					trials.addAll(innerTrials);
				}
				if (session.randomizeTrials() && !innerRandom) {
					Collections.shuffle(trials);
				}
				
				// add block
				retval.add(trials);
			}
			
			// AUDIO & ANIMATION
			if (session.includeAudioAnimationBlock()) {
				List<T> trials = new ArrayList<T>();
				
				boolean singleBlock = session.singleAudioAnimationBlock();
				boolean innerRandom = session.randomizeTrials() && singleBlock && 
						!session.singleAudioAnimationFullRandom();
				int innerReps = singleBlock ? (i == 0 ? reps : 0) : 1;
				
				if (session.synchronizeParameters()) {
					// process synchronization between audio and animation
					Set<String> sharedParams = new HashSet<String>();
					sharedParams.addAll(MediaType.AUDIO.getParams(session));
					sharedParams.retainAll(MediaType.ANIMATION.getParams(session));
					
					// have to do this in two steps to figure out total counts
					List<Map<String, MediaParamValue>> audioRemovals = new ArrayList<Map<String, MediaParamValue>>();
					List<Map<String, MediaParamValue>> animationRemovals = new ArrayList<Map<String, MediaParamValue>>();
					for (Map<String, MediaParamValue> audioMap : audioCombinations) {
						for (Map<String, MediaParamValue> animationMap : animationCombinations) {
							for (String param : sharedParams) {
								MediaParamValue audioVal = audioMap.get(param);
								MediaParamValue animationVal = animationMap.get(param);
								if (audioVal != animationVal) {
									audioRemovals.add(audioMap);
									animationRemovals.add(animationMap);
								}
							}
						}
					}
					audioCombinations.removeAll(audioRemovals);
					animationCombinations.removeAll(animationRemovals);
					
					// init audio counts if not done
					if (!_initAudioCount) {
						MediaType.AUDIO.initializeCount(audioCombinations.size());
						MediaType.AUDIO.initializeWait(session.getAudioPollWait());
						_initAudioCount = true;
					}
					
					// build trial list
					for (int j = 0; j < innerReps; j++) {
						List<T> innerTrials = new ArrayList<T>();
						// use reduced number of maps for actual trial construction
						for (Map<String, MediaParamValue> audioMap : audioCombinations) {
							for (Map<String, MediaParamValue> animationMap : animationCombinations) {
								
								MediaWrapper<Playable> audio = MediaType.AUDIO.createMedia(session, audioMap.values());
								MediaWrapper<AnimationSequence> ani = 
										MediaType.ANIMATION.createMedia(session, animationMap.values());
								if (audio == null || ani == null) continue;
								Long delay = session.getToneOnsetTime(audio.getName());
								for (Long so : offsets) {
									for (int pts : aniPoints) {
										T trial = createTrial(AVBlockType.AUDIO_ANIMATION, 
												ani.getMediaObject(), audio, so, 
												pts, pointSize, connect, delay);
										trial.setNumber(RelativeTrialPosition.REPETITION, i + j + 1); // i or j must be 0
										trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
										innerTrials.add(trial);
										++trialCount;
									}
								}
							}
						}
						
						// randomize and add trials
						if (innerRandom) {
							Collections.shuffle(innerTrials);
						}
						trials.addAll(innerTrials);
					}
				}
				else {
					if (!_initAudioCount) {
						MediaType.AUDIO.initializeCount(audioCombinations.size());
						MediaType.AUDIO.initializeWait(session.getAudioPollWait());
						_initAudioCount = true;
					}

					// build trial list
					for (int j = 0; j < innerReps; j++) {
						List<T> innerTrials = new ArrayList<T>();
						for (Map<String, MediaParamValue> audioMap : audioCombinations) {
							for (Map<String, MediaParamValue> animationMap : animationCombinations) {
								MediaWrapper<Playable> audio = MediaType.AUDIO.createMedia(session, audioMap.values());
								MediaWrapper<AnimationSequence> ani = 
										MediaType.ANIMATION.createMedia(session, animationMap.values());
								if (audio == null || ani == null) continue;
								Long delay = session.getToneOnsetTime(audio.getName());
								for (Long so : offsets) {
									for (int pts : aniPoints) {
										T trial = createTrial(AVBlockType.AUDIO_ANIMATION, 
												ani.getMediaObject(), audio, so, 
												pts, pointSize, connect, delay);
										trial.setNumber(RelativeTrialPosition.REPETITION, i + j + 1); // i or j must be 0
										trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
										innerTrials.add(trial);
										++trialCount;
									}
								}
							}
						}
						
						// randomize and add trials
						if (innerRandom) {
							Collections.shuffle(innerTrials);
						}
						trials.addAll(innerTrials);
					}
				}
				
				// randomize full trials, if 
				if (session.randomizeTrials() && !innerRandom) {
					Collections.shuffle(trials);
				}
				
				// add block
				retval.add(trials);
			}
		}
        
        // shuffle blocks
        if (session.randomizeBlocks()) Collections.shuffle(retval);
        
		// numbering
    	// XXX: we assume that metablocks are all the same size!
		int blockNum = ((metablock - 1) * retval.size()) + 1;
		int blockInMetablock = 1;
		int trialNum = ((metablock - 1) * trialCount) + 1;
		int trialInMetablock = 1;
		
		for (List<T> block : retval) {
			int trialInBlock = 1;
			for (T trial : block) {
				trial.setNumber(TrialHierarchy.METABLOCK, metablock);
				trial.setNumber(TrialHierarchy.BLOCK, blockNum);
				trial.setNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK, blockInMetablock);
				trial.setNumber(TrialHierarchy.TRIAL, trialNum);
				trial.setNumber(RelativeTrialPosition.TRIAL_IN_BLOCK, trialInBlock);
				trial.setNumber(RelativeTrialPosition.TRIAL_IN_METABLOCK, trialInMetablock);
				++trialNum;
				++trialInBlock;
				++trialInMetablock;
			}
			++blockNum;
			++blockInMetablock;
		}
        
        return retval;
	}
	
	@Override
	protected String generateDescription(TrialHierarchy level) {
		if (level == TrialHierarchy.BLOCK) {
			return getStandardBlockDescription();
		}
		else if (level == TrialHierarchy.METABLOCK) {
			S session = getSession();
			
			// blocks
			String blockTypes = "";
			int numBlockTypes = 0;
			if (session.includeAudioBlock()) {
				blockTypes += "\t\t\t" + AVBlockType.AUDIO_ONLY.getUIName() + " block\n";
				++numBlockTypes;
			}
			if (session.includeVideoBlock()) {
				blockTypes += "\t\t\t" + AVBlockType.VIDEO_ONLY.getUIName() + " block\n";
				++numBlockTypes;
			}
			if (session.includeAnimationBlock()) {
				blockTypes += "\t\t\t" + AVBlockType.ANIMATION_ONLY.getUIName() + " block\n";
				++numBlockTypes;
			}
			if (session.includeAudioAnimationBlock()) {
				blockTypes += "\t\t\t" + AVBlockType.AUDIO_ANIMATION.getUIName() + " block\n";
				++numBlockTypes;
			}
			blockTypes = String.format("\t%d metablock(s), %d block type(s), stimuli combinations\n " +
					"\t\trepeated %d time(s), includes:\n", session.getMetaBlocks(), numBlockTypes, 
					session.getBlockSetRepetitions()) + blockTypes;
			
			// parameters
			List<String> audioParams = MediaType.AUDIO.getParams(session);
			List<String> animationParams = MediaType.ANIMATION.getParams(session);
			List<String> videoParams = MediaType.VIDEO.getParams(session);
			
			// additional experiment parameters
			List<Long> offsets = session.getSoundOffsets();
			List<Integer> points = session.getNumAnimationPoints();
			
			// trial counts - init to 1 for loop purposes, correct later
			int audioOnlyCount = 1;
			int aniOnlyCount = 1;
			int audAniCount = 1;
			int videoCount = 1;
			
			// file location info
			File audDir = session.getAudioDirectory();
			File aniDir = session.getAnimationDirectory();
			File vidDir = session.getVideoDirectory();
			List<String> audExt = session.getAudioFileExtensions();
			List<String> aniExt = session.getAnimationFileExtensions();
			List<String> vidExt = session.getVideoFileExtensions();
			
			// inclusion checks and description construction
			String audioDesc = "";
			if (!session.includeAudioBlock() && !session.includeAudioAnimationBlock()) {
				audioOnlyCount = 0;
			}
			else {
				audioDesc = "\tAudio data:\n";
				for (String s : audioParams) {
					List<String> labels = session.getStringList(s + ".labels", (String[]) null);
					if (labels == null) {
						labels = session.getStringList(s, (String[]) null);
					}
					audioOnlyCount *= labels.size();
					String list = listString(labels, 3, 2);
					audioDesc += "\t\t" + session.getString(s + ".label", s) + " " + list + "\n";
				}
				audioOnlyCount *= offsets.size();
				audioDesc += String.format("\t\tAuditory offsets: %s\n", listString(offsets));
				audioDesc += String.format("\t\tAudio subdirectory: %s\n", audDir.getName());
				audioDesc += String.format("\t\tAudio extensions: %s\n", listString(audExt));
			}
			
			String aniDesc = "";
			if (!session.includeAnimationBlock() && !session.includeAudioAnimationBlock()) {
				audAniCount = 0;
				aniOnlyCount = 0;
			}
			else {
				aniOnlyCount *= points.size();
				audAniCount *= audioOnlyCount * aniOnlyCount;
				boolean shared = session.synchronizeParameters();
				aniDesc = "\tAnimation data:\n";
				for (String s : animationParams) {
					List<String> labels = session.getStringList(s + ".labels", (String[]) null);
					if (labels == null) {
						labels = session.getStringList(s, (String[]) null);
					}
					// check to see if this item already counted
					if (!(shared && audioParams.contains(s))) {
						audAniCount *= labels.size();
					}
					String list = listString(labels, 3, 2);
					aniDesc += "\t\t" + session.getString(s + ".label", s) + " " + list + "\n";
				}
				aniDesc += String.format("\t\tAnimation points: %s\n", listString(points));
				aniDesc += String.format("\t\tAnimation subdirectory: %s\n", aniDir.getName());
				aniDesc += String.format("\t\tAnimation extensions: %s\n", listString(aniExt));
			}
			
			// corrections, after use by combined block
			if (!session.includeAudioBlock()) audioOnlyCount = 0;
			if (!session.includeAnimationBlock()) aniOnlyCount = 0;
			
			String videoDesc = "";
			if (!session.includeVideoBlock()) {
				videoCount = 0;
			}
			else {
				videoDesc = "\tVideo data:\n";
				for (String s : videoParams) {
					List<String> labels = session.getStringList(s + ".labels", (String[]) null);
					if (labels == null) {
						labels = session.getStringList(s, (String[]) null);
					}
					videoCount *= labels.size();
					String list = listString(labels, 3, 2);
					videoDesc += "\t\t" + session.getString(s + ".label", s) + " " + list + "\n";
				}
				videoDesc += String.format("\t\tVideo subdirectory: %s\n", vidDir.getName());
				videoDesc += String.format("\t\tVideo extensions: %s\n", listString(vidExt));
			}
			
			return String.format("\n********** Experiment Session Trial Details **********\n%s\n" +
					(session.includeAudioBlock() ? 
							String.format("\tAudio-only trial count: %d\n", audioOnlyCount) : "") +
					(session.includeAnimationBlock() ? 
							String.format("\tAnimation-only trial count: %d\n", aniOnlyCount) : "") +
					(session.includeAudioAnimationBlock() ? 
							String.format("\tAudio and animation trial count: %d\n", audAniCount) : "") +
					(session.includeVideoBlock() ? 
							String.format("\tVideo trial count: %d\n", videoCount) : "") +
					String.format("\tTotal trials: %d\n\n", audioOnlyCount + audAniCount + videoCount + 
							aniOnlyCount) + audioDesc + aniDesc + videoDesc +
					"**************************************************\n\n", blockTypes);
			
		}
		
		return null;
	}

}
