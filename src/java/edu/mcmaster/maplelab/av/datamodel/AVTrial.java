package edu.mcmaster.maplelab.av.datamodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.SwingUtilities;

import edu.mcmaster.maplelab.av.TimeTracker;
import edu.mcmaster.maplelab.av.AVStimulusListener;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.PlayableListener;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationListener;
import edu.mcmaster.maplelab.av.media.animation.AnimationRenderer;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.LogContext;

/**
 * Trial that encapsulates and runs various media types.  May include video, audio, and/or animation.
 * Video is mutually exclusive with other types.  Audio and animation may be used in combination, 
 * and the experiment offset value is used to set their timing relative to one another.
 * 
 * @author bguseman
 */
public abstract class AVTrial<T> extends AnimationTrial<T> {
	
	/** External media. */
	private final MediaWrapper<Playable> _media;
	/** Indicator for type of media (audio and/or animation OR video). */
	private final boolean _isVideo;
	/** Onset delay for the contained media, if applicable. */
	private final Long _inherentMediaDelay;
	/** Media delay relative to animation, if applicable. */
	private final Long _relativeMediaDelay;
	/** Animation delay relative to media, if applicable. */
	private final Long _relativeAnimationDelay;
	/** Number of media and/or animation objects. */
	private int _mediaObjectCount;
	
	/** Animation sequence, if applicable. */
	private final AnimationSequence _animationSequence;
	/** Animation parameters, if applicable. */
	private final int _numPoints;
	private final float _diskRadius;
	private final boolean _connectDots;
	/** Experimental timing offset value, used for offset
	 *  when animation and audio are both present. */
	private final long _offset;
	
	/** Runner of various media types. */
	private TrialRunner _trialRunner = null;
	/** Trial run listeners. */
	private ArrayList<AVStimulusListener> _listeners;
	
	/** Media start times at last run (approx). */
	private Long _animationStart = null;
	private Long _mediaStart = null;

	/**
	 * Constructor.
	 */
	public AVTrial(AnimationSequence animationSequence, boolean isVideo, MediaWrapper<Playable> media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots, Long mediaDelay) {
		
		_animationSequence = animationSequence;
		_isVideo = isVideo;
		_offset = timingOffset;
		_numPoints = animationPoints;
		_media = media;
		_inherentMediaDelay = mediaDelay;
		_diskRadius = diskRadius;
		_connectDots = connectDots;
		_mediaObjectCount = 0;
		
		// gather durations
		Playable playable = _media != null ? _media.getMediaObject() : null;;
		long mediaDuration = 0;
		if (playable != null) {
			mediaDuration = playable.duration();
			++_mediaObjectCount;
		}
		long aniDuration = 0;
		if (_animationSequence != null) {
			aniDuration = _animationSequence.getTotalAnimationTime();
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
			long aniStrike = _animationSequence.getStrikeTime();
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
	
	/**
	 * Get the delay of the expected animation start time relative to
	 * the media start time in milliseconds.
	 */
	public Long getAnimationDelay() {
		return _relativeAnimationDelay;
	}
	
	/**
	 * Get the delay of the expected media start time relative to
	 * the animation start time in milliseconds.
	 */
	public Long getMediaDelay() {
		return _relativeMediaDelay;
	}
	
	/**
	 * Get the number of media objects in this trial.  Convenience method.
	 */
	public int getNumMediaObjects() {
		return _mediaObjectCount;
	}
	
	public MediaWrapper<Playable> getMedia() {
		return _media;
	}

	public boolean isVideo() {
		return _isVideo;
	}
	
	public long getOffset() {
		return _offset;
	}
	
	public int getNumPoints() {
		return _numPoints;
	}
	
	public boolean isConnected() {
		return _connectDots;
	}
	
	/**
	 * Mark the animation start time in milliseconds.
	 */
	public void markAnimationStart(Long animationStart) {
		_animationStart = animationStart;
	}
	
	/**
	 * Get the approximate animation start time during the last run of this trial, if available.
	 */
	public Long getLastAnimationStart() {
		return _animationStart;
	}
	
	/**
	 * Mark the media start time in milliseconds.
	 */
	public void markMediaStart(Long mediaStart) {
		_mediaStart = mediaStart;
	}
	
	/**
	 * Get the approximate media start time during the last run of this trial, if available.
	 */
	public Long getLastMediaStart() {
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
	
	public Playable getAudioPlayable() {
		return !isVideo() && _media != null ? _media.getMediaObject() : null;
	}
	
	public Playable getVideoPlayable() {
		return isVideo() && _media != null ? _media.getMediaObject() : null;
	}
	
	@Override
	public String getDescription() {
		if (isVideo()) {
			String video = _media != null && isVideo() ? _media.getName() : "N/A";
			String format = "Trial %d:\n\tVideo file: %s";
			return String.format(format, getNum(), video);
		}
		else return getExtendedDescription();
	}
	
	/**
	 * Get full description that includes separate animation and audio parameters, if applicable.
	 */
	private String getExtendedDescription() {
		String ani = _animationSequence != null ? _animationSequence.getSourceFileName() : "N/A";
		String frames = _animationSequence != null ? 
				String.valueOf(_animationSequence.getNumFrames()) : "N/A";
		String hits = _animationSequence != null ? String.valueOf(getAnimationStrikeTime()) : "N/A";
		String aspect = _animationSequence != null ? 
				String.valueOf(_animationSequence.getPointAspect()) : "N/A";
		String audio = _media != null && !isVideo() ? _media.getName() : "N/A";
		String audioOnset = _inherentMediaDelay != null ? _inherentMediaDelay.toString() : "0";
		String delays = "";
		if (_relativeAnimationDelay != 0) {
			delays = String.format("\tAudio begins at time 0\n\tAnimation delayed by %d ms", 
					_relativeAnimationDelay);
		}
		else if (_relativeMediaDelay != 0) {
			delays = String.format("\tAnimation begins at time 0\n\tAudio delayed by %d ms", 
					_relativeMediaDelay);
		}
		String format = "Trial %d:\n\tAudio file: %s\n\tOffset: %d\n\tTone onset delay: %s\n" +
				"\tAnimation file: %s\n\tFrame count: %s\n\tAnimation points: %d\n" +
				"\tAnimation hit point(s): %s\n\tConnect points: %b\n\tPoint aspect: %s\n%s";
		return String.format(format, getNum(), audio, _offset, audioOnset, ani, frames, _numPoints, 
				hits, _connectDots, aspect, delays);
	}
	
	/**
	 * Get the time that the strike occurs (mallet head is at its lowest point).
	 */
	public long getAnimationStrikeTime() {
		return _animationSequence != null ? _animationSequence.getStrikeTime() : 0;
	}
	
	/**
	 * Get the time the tone occurs (tone onset delay).
	 */
	public long getAudioToneOnset() {
		return !isVideo() && _inherentMediaDelay != null ? _inherentMediaDelay : 0;
	}
	
	public synchronized void preparePlayback(AnimationRenderer renderer) {
		_trialRunner = new TrialRunner(renderer);
	}
	
	/**
	 * Play the contained audio and/or visual items.  MUST BE CALLED ON THE EDT.
	 */
	public synchronized void play() {
		if (_trialRunner == null) {
			throw new IllegalStateException("Trial must be prepared before calling play.");
		}
		
		_trialRunner.play();
	}
	
	/**
	 * Add a playback listener.
	 */
	public void addPlaybackListener(AVStimulusListener listener) {
		if (_listeners == null) _listeners = new ArrayList<AVStimulusListener>();
		_listeners.add(listener);
	}
	
	/**
	 * Remove a playback listener.
	 */
	public void removePlaybackListener(AVStimulusListener listener) {
		if (_listeners != null) _listeners.remove(listener);
	}
	
	
	
	/**
	 * Class for encapsulating media preparation and running.
	 */
	private class TrialRunner {
		private final AnimationRenderer _renderer;
		private final AnimationRunnable _aniRunner;
		private final Thread _mediaThread;
		private int _playCount = 3;
		
		public TrialRunner(AnimationRenderer renderer) {
			_renderer = renderer;
			_animationStart = null;
			_mediaStart = null;
			
			// gather media data
			final Playable media = isVideo() ? getVideoPlayable() : getAudioPlayable();
			long mediaDuration = media != null ? media.duration() : 0; 
			long aniDuration = _animationSequence != null ? _animationSequence.getTotalAnimationTime() : 0;
			
			// if nothing to play, return
			boolean animation = aniDuration > 0;
			if (!animation && (mediaDuration == 0) ) {
				_aniRunner = null;
				_mediaThread = null;
				return;
			}
			
			_playCount = animation && mediaDuration > 0 ? 2 : 1;
				
			// establish reference time
			long currTime = System.currentTimeMillis();
			
			if (animation) {
				_aniRunner = new AnimationRunnable(_renderer, _relativeAnimationDelay, currTime);
				_renderer.addAnimationListener(new AnimationListener() {
					@Override
					public void animationDone() {
						/******************************/
						/*File f = null;
						try {
							f = File.createTempFile(AnimationRenderer.class.getSimpleName() + "-timestamps", 
									".log", _session.getDataDir());
						} 
						catch (IOException e) { }
						if (f != null) TimeTracker.logTimes(AnimationRenderer.TIMESTAMPS, f, true);
						/****************************/
						
						markFinish(this, null);
					}
				});
			}
			else _aniRunner = null;
			
			
			if (media != null) {
				_mediaThread = new Thread(new MediaRunnable(media, _relativeMediaDelay, currTime));
				media.addListener(new PlayableListener() {
					@Override
					public void playableEnded() {
						markFinish(null, this);
					}
				});
			}
			else _mediaThread = null;
		}
		
		/**
		 * Method for signaling playback end when appropriate.
		 */
		private synchronized void markFinish(final AnimationListener al, final PlayableListener pl) {
			--_playCount;
			
			if (al != null) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						_renderer.removeAnimationListener(al);
					}
				});
			}
			if (pl != null) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Playable p = isVideo() ? getVideoPlayable() : getAudioPlayable();
						p.removeListener(pl);
					}
				});
			}
			
			if (_playCount == 0 && _listeners != null) {
				for (AVStimulusListener tpl : _listeners) {
					tpl.stimuliComplete();
				}
				
				_trialRunner = null;
			}
		}
		
		/**
		 * Run the animation and sound.
		 */
		public void play() {
			boolean edt = SwingUtilities.isEventDispatchThread();
			
			if (_mediaThread != null) _mediaThread.start();
			if (_aniRunner != null) {
				if (!edt) {
					SwingUtilities.invokeLater(_aniRunner);
				}
				else {
					_aniRunner.run();
				}
			}
		}
		
		/**
		 * Runnable for animation.
		 */
		private class AnimationRunnable implements Runnable {
			private final AnimationRenderer _renderer;
			private final long _delay;
			private final long _refTime; // reference time
			
			public AnimationRunnable(AnimationRenderer renderer, 
					long delay, long refTime) {
				_renderer = renderer;
				_delay = delay;
				_refTime = refTime;
				_renderer.setAnimationSource(AVTrial.this);
			}
			
			public void run() {
				// delay
				try {
					Thread.sleep(_delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// render
				LogContext.getLogger().fine(String.format(
						"--> Animation started at time %d", System.currentTimeMillis() - _refTime));
				
	    		_renderer.setStartTime(_animationStart = System.currentTimeMillis()); 
	    		
				/*LogContext.getLogger().fine(String.format(
						"Animation ends at time %d", System.currentTimeMillis() - _refTime));*/
			}
		}
		
		/**
		 * Runnable for media.
		 */
		private class MediaRunnable implements Runnable {
			private final Playable _media;
			private final long _delay;
			private final long _refTime; // reference time
			
			public MediaRunnable(Playable media, long delay, long refTime) {
				_media = media;
				_delay = delay;
				_refTime = refTime;
			}
			
			public void run() {
				// delay
				try {
					Thread.sleep(_delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// play
				LogContext.getLogger().fine(String.format(
						"--> Media started at time %d", System.currentTimeMillis() - _refTime));
				
				_mediaStart = System.currentTimeMillis();
				_media.play();
				
				/*LogContext.getLogger().fine(String.format(
						"Sound ends at time %d", System.currentTimeMillis() - _refTime));*/
			}
		}
		
	}

	@Override
	public abstract boolean isResponseCorrect();

}
