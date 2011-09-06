/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.datamodel;

import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.SwingUtilities;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.Trial;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.PlayableListener;
import edu.mcmaster.maplelab.common.sound.SoundClip;
import edu.mcmaster.maplelab.toj.animator.AnimationListener;
import edu.mcmaster.maplelab.toj.animator.AnimationRenderer;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;

/**
 * TOJ specific implementation of Trial.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class TOJTrial extends Trial<Response> {
	
	private final Playable _audio;
	private final boolean _isVideo;
	private final long _offset;
	private final int _numPoints;
	private final float _diskRadius;
	private final AnimationSequence _animationSequence;
	private TrialRunner _trialRunner = null;
	private ArrayList<TOJTrialPlaybackListener> _listeners;
	private Long _animationStart = null;
	private Long _audioStart = null;
	private Long _animationStrike = null;
	private Long _audioTone = null;

	public TOJTrial(AnimationSequence animationSequence, boolean isVideo, Playable audio, 
			Long timingOffset, int animationPoints, float diskRadius) {
		
		_animationSequence = animationSequence;
		_isVideo = isVideo;
		_offset = timingOffset;
		_numPoints = animationPoints;
		_audio = audio;
		
		_diskRadius = diskRadius;
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
	
	/**
	 * Get the approximate animation start time during the last run of this trial, if available.
	 */
	public Long getLastAnimationStart() {
		return _animationStart;
	}
	
	/**
	 * Get the approximate audio start time during the last run of this trial, if available.
	 */
	public Long getLastAudioStart() {
		return _audioStart;
	}
	
	public AnimationSequence getAnimationSequence() {
		return _animationSequence;
	}
	
	@Override
	public boolean isResponseCorrect() {
		Response response = getResponse();
        if (response != null) {
        	// if offset==0, either is correct
            if (TOJResponseParameters.isDotFirst(response)) {
                return getOffset() >= 0;
            }
            else {
                return getOffset() <= 0;
            }
                
        }
        return false;
	}
	
	/**
	 * Get the radius of the points to render for each joint.
	 */
	public float getDiskRadius() {
		return _diskRadius;
	}
	
	public Playable getPlayable() {
		return _audio;
	}
	
	/** 
	 * Get a human-readable string description of TOJTrial
	 */
	public String getDescription() {
		String frames = (_animationSequence != null ? 
				String.valueOf(_animationSequence.getNumFrames()) : "N/A") + " frames";
		String audio = _audio != null ? _audio.name() : "N/A";
		return String.format("TOJTrial:	%s, isVideo: %b, " +
				"Playable name: %s, timingOffset: %f, animationPoints: %d, diskRadius: %f\n",
				frames, _isVideo, audio, _offset, _numPoints, _diskRadius);
	}
	
	/**
	 * Get the total animation time.
	 */
	public long getAnimationDuration() {
		if (getAnimationSequence() == null) return (long) 0;
		return getAnimationSequence().getTotalAnimationTime();
	}
	
	/**
	 * Get the time that the strike occurs (mallet head is at its lowest point).
	 */
	public long getAnimationStrikeTime() {
		if (_animationStrike == null) {
			_animationStrike = _animationSequence != null ? _animationSequence.getStrikeTime() : 0;
		}
		return _animationStrike;
	}
	
	/**
	 * Get the time the tone occurs (tone onset delay).
	 */
	public long getAudioToneOnset() {
		return _audioTone != null ? _audioTone : 0;
	}
	
	public synchronized void preparePlayback(TOJSession session, AnimationRenderer renderer) {
		_trialRunner = new TrialRunner(session, renderer);
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
	public void addPlaybackListener(TOJTrialPlaybackListener listener) {
		if (_listeners == null) _listeners = new ArrayList<TOJTrialPlaybackListener>();
		_listeners.add(listener);
	}
	
	/**
	 * Remove a playback listener.
	 */
	public void removePlaybackListener(TOJTrialPlaybackListener listener) {
		if (_listeners != null) _listeners.remove(listener);
	}
	
	/**
	 * Class for encapsulating animation and sound preparation and running.
	 */
	private class TrialRunner {
		private final TOJSession _session;
		private final AnimationRenderer _renderer;
		private final AnimationRunnable _aniRunner;
		private final Thread _audThread;
		private int _playCount = 3;
		
		public TrialRunner(TOJSession session, AnimationRenderer renderer) {
			_session = session;
			_renderer = renderer;
			_animationStart = null;
			_audioStart = null;
			
			// gather animation and audio data
			final Playable audio = getPlayable();
			long aniDuration = getAnimationDuration();
			long aniStrike = getAnimationStrikeTime();
			long audDuration = audio != null ? ((SoundClip) audio).getClipDuration() : 0; 
			_audioTone = audio != null ? _session.getToneOnsetTime(audio.name()) : 0;	
			
			// if nothing to play, return
			if ((aniDuration == 0) && (audDuration == 0) ) {
				_aniRunner = null;
				_audThread = null;
				return;
			}
			
			_playCount = aniDuration > 0 && audDuration > 0 ? 2 : 1;
			
			// figure out: 	1. which stimulus starts first
			//				2. how much to delay 2nd stimulus
			long offset = getOffset();
			boolean animationFirst = aniStrike > _audioTone - offset;
			long aniDelay = animationFirst ? 0 : _audioTone - aniStrike - offset;
			long audDelay = animationFirst ? aniStrike - _audioTone + offset : 0;
			
			LogContext.getLogger().fine(String.format(
					"Calculated events: audio tone delay=%d, animation strike delay=%d", _audioTone, aniStrike));
			LogContext.getLogger().fine(String.format(
					"Will play with animation delay = %d", (int) aniDelay));
			LogContext.getLogger().fine(String.format(
					"Will play with audio delay = %d", (int) audDelay));
			
			long currTime = System.currentTimeMillis();
			_aniRunner = new AnimationRunnable(_renderer, aniDelay, currTime);
			_renderer.addAnimationListener(new AnimationListener() {
				@Override
				public void animationDone() {
					markFinish(this, null);
				}
			});
			
			if (audio != null) {
				_audThread = new Thread(new AudioRunnable(audio, audDelay, currTime));
				audio.addListener(new PlayableListener() {
					@Override
					public void playableEnded(EventObject e) {
						markFinish(null, this);
					}
				});
			}
			else _audThread = null;
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
						getPlayable().removeListener(pl);
					}
				});
			}
			
			if (_playCount == 0 && _listeners != null) {
				for (TOJTrialPlaybackListener tpl : _listeners) {
					tpl.playbackEnded();
				}
				
				_trialRunner = null;
			}
		}
		
		/**
		 * Run the animation and sound.
		 */
		public void play() {
			boolean edt = SwingUtilities.isEventDispatchThread();
			
			if (_audThread != null) _audThread.start();
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
						"Animation starts at time %d", System.currentTimeMillis() - _refTime));
				
				_renderer.setTrial(TOJTrial.this);
	    		_renderer.setStartTime(_animationStart = System.currentTimeMillis()); 
	    		
				LogContext.getLogger().fine(String.format(
						"Animation ends at time %d", System.currentTimeMillis() - _refTime));
				
			}
		}
		
		/**
		 * Runnable for audio.
		 */
		private class AudioRunnable implements Runnable {
			private final Playable _audio;
			private final long _delay;
			private final long _refTime; // reference time
			
			public AudioRunnable(Playable audio, long delay, long refTime) {
				_audio = audio;
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
						"Sound starts at time %d", System.currentTimeMillis() - _refTime));
				
				_audioStart = System.currentTimeMillis();
				_audio.play();
				
				LogContext.getLogger().fine(String.format(
						"Sound ends at time %d", System.currentTimeMillis() - _refTime));
				
			}
		}
		
	}
}
