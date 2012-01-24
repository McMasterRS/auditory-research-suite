package edu.mcmaster.maplelab.av.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mcmaster.maplelab.av.media.MediaParams.MediaParamValue;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.MediaType;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Block;

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

	protected AVBlock(S session, int blockNum, AVBlockType type, List<Long> offsets, List<Integer> numPoints) {
		
		super(session, blockNum);
		
		_type = type;
		_trials = new ArrayList<T>();
		
		float pointSize = session.getBaseAnimationPointSize();
		boolean connect = session.connectDots();
		
		if (type == AVBlockType.VIDEO_ONLY) { 
			List<Map<String, MediaParamValue>> videoCombinations = MediaType.VIDEO.buildParameterMaps(session);
			for (Map<String, MediaParamValue> map : videoCombinations) {
				MediaWrapper<Playable> video = MediaType.VIDEO.createMedia(session, map.values());
				T trial = createTrial(null, true, video, 0l, 0, pointSize, connect);
				_trials.add(trial);
			}
		}
		else if (type == AVBlockType.AUDIO_ONLY) {
			List<Map<String, MediaParamValue>> audioCombinations = MediaType.AUDIO.buildParameterMaps(session);
			for (Map<String, MediaParamValue> map : audioCombinations) {
				MediaWrapper<Playable> audio = MediaType.AUDIO.createMedia(session, map.values());
				for (Long so : offsets) {
					T trial = createTrial(null, false, audio, so, 0, pointSize, connect);
					_trials.add(trial);
				}
			}
		}
		else if (type == AVBlockType.AUDIO_ANIMATION) {
			
			if (session.synchronizeParameters()) {
				Set<String> sharedParams = new HashSet<String>();
				sharedParams.addAll(MediaType.AUDIO.getParams(session));
				sharedParams.retainAll(MediaType.ANIMATION.getParams(session));
				
				List<Map<String, MediaParamValue>> audioCombinations = MediaType.AUDIO.buildParameterMaps(session);
				List<Map<String, MediaParamValue>> animationCombinations = MediaType.ANIMATION.buildParameterMaps(session);
				for (Map<String, MediaParamValue> audioMap : audioCombinations) {
					for (Map<String, MediaParamValue> animationMap : animationCombinations) {
						boolean synced = true;
						for (String param : sharedParams) {
							MediaParamValue audioVal = audioMap.get(param);
							MediaParamValue animationVal = audioMap.get(param);
							synced = audioVal == animationVal;
						}
						if (!synced) continue;
						
						MediaWrapper<Playable> audio = MediaType.AUDIO.createMedia(session, audioMap.values());
						MediaWrapper<AnimationSequence> ani = 
								MediaType.ANIMATION.createMedia(session, animationMap.values());
						for (Long so : offsets) {
							for (int pts : numPoints) {
								T trial = createTrial(ani.getMediaObject(), false, audio, so, 
										pts, pointSize, connect);
								_trials.add(trial);
							}
						}
					}
				}
			}
			else {
				List<Map<String, MediaParamValue>> audioCombinations = MediaType.AUDIO.buildParameterMaps(session);
				List<Map<String, MediaParamValue>> animationCombinations = MediaType.ANIMATION.buildParameterMaps(session);
				for (Map<String, MediaParamValue> audioMap : audioCombinations) {
					for (Map<String, MediaParamValue> animationMap : animationCombinations) {
						MediaWrapper<Playable> audio = MediaType.AUDIO.createMedia(session, audioMap.values());
						MediaWrapper<AnimationSequence> ani = 
								MediaType.ANIMATION.createMedia(session, animationMap.values());
						for (Long so : offsets) {
							for (int pts : numPoints) {
								T trial = createTrial(ani.getMediaObject(), false, audio, so, 
										pts, pointSize, connect);
								_trials.add(trial);
							}
						}
					}
				}
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
	 * 
	 * TODO: refactor trials to store parameters and load media later?
	 */
	protected abstract T createTrial(AnimationSequence animationSequence, boolean isVideo, MediaWrapper<Playable> media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots);

}
