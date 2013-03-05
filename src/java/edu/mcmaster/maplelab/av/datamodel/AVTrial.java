package edu.mcmaster.maplelab.av.datamodel;

import java.util.concurrent.TimeUnit;

import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;

/**
 * Trial that encapsulates various media types.  May include video, audio, and/or animation.
 * Video is mutually exclusive with other types.  Audio and animation may be used in combination, 
 * and the experiment offset value is used to set their timing relative to one another.
 * 
 * XXX: ALL TIME VARIABLES WITHIN THIS CLASS ARE IN NANOSECONDS!
 * 
 * @author bguseman
 */
public abstract class AVTrial<T> extends AnimationTrial<T> {
	
	/************** MEDIA OBJECTS ******************/
	/** Number of media and/or animation objects. */
	private int _mediaObjectCount;
	/** External media. */
	private final MediaWrapper<Playable> _media;
	/** Indicator for type of media (audio and/or animation OR video). */
	private final boolean _isVideo;
	/** Animation sequence, if applicable. */
	private final AnimationSequence _animationSequence;
	/** Animation parameters, if applicable. */
	private final int _numPoints;
	private final float _diskRadius;
	private final boolean _connectDots;
	

	/************** TIMING OBJECTS (NANOSECONDS) **********/
	/** Experimental timing offset value, used for offset
	 *  when animation and audio are both present. */
	private final long _offset;
	/** Onset delay for the contained media, if applicable. */
	private final Long _inherentMediaDelay;
	/** Media delay relative to animation, if applicable. */
	private final Long _relativeMediaDelay;
	/** Animation delay relative to media, if applicable. */
	private final Long _relativeAnimationDelay;
	/** Media start times at last run (approx). */
	private Long _animationStart = null;
	private Long _mediaStart = null;

	/**
	 * Constructor.
	 */
	public AVTrial(AnimationSequence animationSequence, boolean isVideo, 
			MediaWrapper<Playable> media, Long timingOffsetMillis, int animationPoints, 
			float diskRadius, boolean connectDots, Long mediaDelayMillis) {
		
		_animationSequence = animationSequence;
		_isVideo = isVideo;
		_offset = TimeUnit.NANOSECONDS.convert(timingOffsetMillis, TimeUnit.MILLISECONDS);
		_numPoints = animationPoints;
		_media = media;
		_inherentMediaDelay = TimeUnit.NANOSECONDS.convert(mediaDelayMillis, TimeUnit.MILLISECONDS);
		_diskRadius = diskRadius;
		_connectDots = connectDots;
		_mediaObjectCount = 0;
		
		// gather durations
		Playable playable = _media != null ? _media.getMediaObject() : null;;
		long mediaDuration = 0;
		if (playable != null) {
			mediaDuration = TimeUnit.NANOSECONDS.convert(
					playable.durationMillis(), TimeUnit.MILLISECONDS);
			++_mediaObjectCount;
		}
		long aniDuration = 0;
		if (_animationSequence != null) {
			aniDuration = _animationSequence.getTotalAnimationTimeNanos();
			++_mediaObjectCount;
		}
		
		// if not 2 items to play, don't calculate
		boolean animation = aniDuration > 0;
		if (!animation || mediaDuration == 0) {
			_relativeMediaDelay = (long) 0;
			_relativeAnimationDelay = (long) 0;
		}
		else {
			// figure out: 	1. which stimulus starts first
			//				2. how much to delay 2nd stimulus
			long aniStrike = _animationSequence.getStrikeTimeNanos();
			boolean animationFirst = aniStrike > _inherentMediaDelay - _offset;
			if (animationFirst) {
				_relativeMediaDelay = aniStrike - _inherentMediaDelay + _offset;
				_relativeAnimationDelay = (long) 0;
			}
			else {
				_relativeAnimationDelay = _inherentMediaDelay - aniStrike - _offset;
				_relativeMediaDelay = (long) 0;
			}
		}
	}

	@Override
	public abstract boolean isResponseCorrect();
	
	/**
	 * Get the delay of the expected animation start time relative to
	 * the media start time in nanoseconds.
	 */
	public Long getAnimationDelayNanos() {
		return _relativeAnimationDelay;
	}
	
	/**
	 * Get the delay of the expected media start time relative to
	 * the animation start time in nanoseconds.
	 */
	public Long getMediaDelayNanos() {
		return _relativeMediaDelay;
	}
	
	/**
	 * Get the number of media objects in this trial.  Convenience method.
	 */
	public int getNumMediaObjects() {
		return _mediaObjectCount;
	}
	
	/**
	 * Get the media object from this trial.  May be null.
	 */
	public MediaWrapper<Playable> getMedia() {
		return _media;
	}

	/**
	 * Indicate if this trial contains a video media object.
	 */
	public boolean isVideo() {
		return _isVideo;
	}

	/**
	 * Indicate if this trial contains an animation.
	 */
	public boolean isAnimation() {
		return _animationSequence != null;
	}
	
	/**
	 * Get the timing offset value between animation and audio.
	 */
	public long getOffsetNanos() {
		return _offset;
	}
	
	/**
	 * Get the number of animation points to render.
	 */
	public int getNumPoints() {
		return _numPoints;
	}
	
	/**
	 * Indicate if animation points should be connected.
	 */
	public boolean isConnected() {
		return _connectDots;
	}
	
	/**
	 * Mark the animation start time in nanoseconds.
	 */
	public void markAnimationStartNanos(Long animationStart) {
		_animationStart = animationStart;
	}
	
	/**
	 * Get the approximate animation start time during the last run of this trial, if available.
	 */
	public Long getLastAnimationStartNanos() {
		return _animationStart;
	}
	
	/**
	 * Mark the media start time in nanoseconds.
	 */
	public void markMediaStartNanos(Long mediaStart) {
		_mediaStart = mediaStart;
	}
	
	/**
	 * Get the approximate media start time during the last run of this trial, if available.
	 */
	public Long getLastMediaStartNanos() {
		return _mediaStart;
	}
	
	@Override
	public AnimationSequence getAnimationSequence() {
		return _animationSequence;
	}
	
	/**
	 * Get the radius of the points to render for each joint.
	 */
	@Override
	public float getDiskRadius() {
		return _diskRadius;
	}
	
	/**
	 * Get the playable audio object, if applicable.  May be null.
	 */
	public Playable getAudioPlayable() {
		return !isVideo() && _media != null ? _media.getMediaObject() : null;
	}
	
	/**
	 * Get the playable video object, if applicable.  May be null.
	 */
	public Playable getVideoPlayable() {
		return isVideo() && _media != null ? _media.getMediaObject() : null;
	}
	
	@Override
	public String getDescription() {
		if (isVideo()) {
			String video = _media != null ? _media.getName() : "N/A";
			String format = "Trial %d:\n\tVideo file: %s";
			return String.format(format, getNum(), video);
		}
		else return getExtendedDescription();
	}
	
	/**
	 * Get full description that includes separate animation and audio parameters, if applicable.
	 */
	private String getExtendedDescription() {
		String retval = String.format("Trial %d", getNum());
		
		String ani = null, audio = null;
		Long timeMillis = null;
		if (_animationSequence != null && _media != null) {
			ani = _animationSequence.getSourceFileName();
			String frames = String.valueOf(_animationSequence.getNumFrames());
			timeMillis = TimeUnit.MILLISECONDS.convert(
					getAnimationStrikeTimeNanos(), TimeUnit.NANOSECONDS);
			String hits = timeMillis != null ? timeMillis.toString() : "N/A";
			String aspect = String.valueOf(_animationSequence.getPointAspect());
			audio = _media != null ? _media.getName() : "N/A";
			timeMillis = _inherentMediaDelay != null ? TimeUnit.MILLISECONDS.convert(
					_inherentMediaDelay, TimeUnit.NANOSECONDS) : null;
			String audioOnset = timeMillis != null ? timeMillis.toString() : "0";
			
			String delays = "";
			if (_relativeAnimationDelay != 0) {
				delays = String.format("\tAudio begins at time 0\n\tAnimation delayed by %d ms", 
						TimeUnit.MILLISECONDS.convert(_relativeAnimationDelay, TimeUnit.NANOSECONDS));
			}
			else if (_relativeMediaDelay != 0) {
				delays = String.format("\tAnimation begins at time 0\n\tAudio delayed by %d ms", 
						TimeUnit.MILLISECONDS.convert(_relativeMediaDelay, TimeUnit.NANOSECONDS));
			}
			
			timeMillis = TimeUnit.MILLISECONDS.convert(getOffsetNanos(), TimeUnit.NANOSECONDS);
			String format = ":\n\tAudio file: %s\n\tOffset: %d\n\tTone onset delay: %s\n" +
					"\tAnimation file: %s\n\tFrame count: %s\n\tAnimation points: %d\n" +
					"\tAnimation hit point(s): %s\n\tConnect points: %b\n\tPoint aspect: %s\n%s";
			retval += String.format(format, audio, timeMillis, audioOnset, ani, frames, 
					_numPoints, hits, _connectDots, aspect, delays);
		}
		else if (_animationSequence != null) {
			ani = _animationSequence.getSourceFileName();
			String frames = String.valueOf(_animationSequence.getNumFrames());
			timeMillis = TimeUnit.MILLISECONDS.convert(
					getAnimationStrikeTimeNanos(), TimeUnit.NANOSECONDS);
			String hits = timeMillis != null ? timeMillis.toString() : "N/A";
			String aspect = String.valueOf(_animationSequence.getPointAspect());
			
			String format = ":\n\tAnimation file: %s\n\tFrame count: %s\n\tAnimation points: %d\n" +
					"\tAnimation hit point(s): %s\n\tConnect points: %b\n\tPoint aspect: %s\n";
			retval += String.format(format, ani, frames, _numPoints, hits, _connectDots, aspect);
		}
		else if (_media != null) {
			audio = _media.getName();
			timeMillis = _inherentMediaDelay != null ? TimeUnit.MILLISECONDS.convert(
					_inherentMediaDelay, TimeUnit.NANOSECONDS) : null;
			String audioOnset = timeMillis != null ? timeMillis.toString() : "0";

			timeMillis = TimeUnit.MILLISECONDS.convert(getOffsetNanos(), TimeUnit.NANOSECONDS);
			String format = ":\n\tAudio file: %s\n\tOffset: %d\n\tTone onset delay: %s\n";
			retval += String.format(format, audio, timeMillis, audioOnset);
		}
		
		return retval;
	}
	
	/**
	 * Get the time that the strike occurs (mallet head is at its lowest point).
	 */
	public long getAnimationStrikeTimeNanos() {
		return _animationSequence != null ? _animationSequence.getStrikeTimeNanos() : 0;
	}
	
	/**
	 * Get the time the tone occurs (tone onset delay).
	 */
	public long getAudioToneOnsetNanos() {
		return !isVideo() && _inherentMediaDelay != null ? _inherentMediaDelay : 0;
	}

}
