package edu.mcmaster.maplelab.av.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.mcmaster.maplelab.av.animation.AnimationParser;
import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.media.MediaParams;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.av.media.PlayableMedia.MediaType;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Block;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

public abstract class AVBlock<S extends AVSession<?,?,?>, T extends AVTrial<?>> extends Block<S, T> {
	
	public enum AVBlockType {
		AUDIO_ONLY("Audio only"),
		VIDEO_ONLY("Video"),
		AUDIO_ANIMATION("Audio and animation");
		
		private final String _uiName;
		
		private AVBlockType(String uiName) {
			_uiName = uiName;
		}
		
		/**
		 * Get the human-readable version of this block type.
		 */
		public String getUIName() {
			return _uiName;
		}
	}
	
	/** AV type of this block. */
	private final AVBlockType _type;
	/** Generated trials. */
	List<T> _trials = null;

	protected AVBlock(S session, int blockNum, AVBlockType type, List<DurationEnum> visDurations, 
			List<NotesEnum> pitches, List<Long> offsets, List<Integer> numPoints) {
		
		super(session, blockNum);
		
		_type = type;
		_trials = new ArrayList<T>();
		
		float animationAspect = session.getAnimationPointAspect();
		float pointSize = session.getBaseAnimationPointSize();
		boolean connect = session.connectDots();
		
		MediaType audioMT = session.isUsingLegacyAudioFiles() ? MediaType.LEGACY_AUDIO : MediaType.AUDIO;

		// create trials from session
		if (type == AVBlockType.AUDIO_ANIMATION) {
			for (PlayableMedia audio : audioMT.getUniqueMedia(session)) {
				// pitch is shared if legacy audio!
				List<NotesEnum> pitchList = audioMT == MediaType.LEGACY_AUDIO ? 
						Arrays.asList(audio.getValue(MediaParams.pitch)) : pitches;
				
				for (NotesEnum pitch : pitchList) {
					for (DurationEnum duration : visDurations) {
						String filename = String.format("%s%s_.txt", pitch.toString().toLowerCase(), 
								duration.codeString());
						
						File dir = session.getExpectedAnimationSubDir();
						AnimationSequence aniSeq = null;
						try {
							aniSeq = AnimationParser.parseFile(new File(dir, filename), animationAspect);
							aniSeq.setVisualDuration(duration);
						}
						catch (FileNotFoundException fne) {
							LogContext.getLogger().warning(String.format("Animation file %s not found.", filename));
						}
						
						for (Long so : offsets) {
							for (int pts : numPoints) {
								T trial = createTrial(aniSeq, false, audio, so, pts, pointSize, connect);
								_trials.add(trial);
							}
						}
					}
				}
			}
		}
		else if (type == AVBlockType.AUDIO_ONLY) {
			for (PlayableMedia audio : audioMT.getUniqueMedia(session)) {
				for (Long so : offsets) {
					T trial = createTrial(null, false, audio, so, 0, pointSize, connect);
					_trials.add(trial);
				}
			}
		}
		else if (type == AVBlockType.VIDEO_ONLY) { 
			for (PlayableMedia video : MediaType.VIDEO.getUniqueMedia(session)) {
				T trial = createTrial(null, true, video, 0l, 0, pointSize, connect);
				_trials.add(trial);
			}
		}
		
		// randomize and number trials
		if (session.randomizeTrials()) Collections.shuffle(_trials);
		for (int i = 0; i < _trials.size(); i++) {
			_trials.get(i).setNum(i+1);
		}
	}
	
	/**
	 * Get the type of this block.
	 */
	public AVBlockType getType() {
		return _type;
	}
	
	public void logTrials() {
		LogContext.getLogger().fine(String.format("\n----- Block %d : %d trials -----", 
				getNum(), _trials.size()));
		for (T t : _trials) {
			LogContext.getLogger().fine(t.getDescription());
		}
		LogContext.getLogger().fine(String.format("-----------------------------------\n", _trials.size()));
	}


	@Override
	public List<T> getTrials() {
		return _trials;
	}

    /**
     * Reduce the number of trials in this block to the given number. If number
     * is greater than current number then call is ignored. Used for constructing
     * warmup blocks.
     * TODO: refactor into Block
     * 
     * @param numWarmupTrials number of trials to clip to.
     */
    public void clipTrials(int numWarmupTrials) {
        if (numWarmupTrials < _trials.size()) {
            _trials = _trials.subList(0, numWarmupTrials);
        }
    }
	
	/**
	 * Create a single trial of the needed type from the given parameters.
	 */
	protected abstract T createTrial(AnimationSequence animationSequence, boolean isVideo, PlayableMedia media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots);

}
