/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.PlayableListener;
import edu.mcmaster.maplelab.av.media.animation.AnimationListener;
import edu.mcmaster.maplelab.av.media.animation.AnimationPanel;
import edu.mcmaster.maplelab.av.media.animation.AnimationRenderer;
import edu.mcmaster.maplelab.av.media.animation.AnimationRenderer.GLDrawDelegate;
import edu.mcmaster.maplelab.av.media.animation.ScheduledAnimationTrigger;
import edu.mcmaster.maplelab.common.LogContext;

/**
 * @author bguseman
 *
 */
public class StimulusScheduler {
	private static final int PERIOD_OFFSET_MULTIPLIER = 1;
	private static final boolean APPLE_MODE = true;
	private static final int MAX_STIMULUS_COUNT = 12;
	private static final long REFRESH_PERIOD;
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		int refresh = gs.getDisplayMode().getRefreshRate();
		if (refresh == DisplayMode.REFRESH_RATE_UNKNOWN) {
			refresh = 60;
		}
		REFRESH_PERIOD = (long) (1000000000.0d / (double) refresh); // nanos
	}
	
	private final Scheduler _scheduler;
	private final ScheduledAnimationTrigger _trigger;
	private final AnimationRenderer _renderer;
	private final AnimationStartAlarm _animationStart;
	private final MediaPlaybackAlarm _mediaStart;
	private final AnimationCompletionListener _animationListener;
	private final MediaCompletionListener _mediaListener;
	private final VSyncedScheduleStarter _starter;
	private final VSyncedRefreshCalcScheduler _refreshCalculator;
	private final List<AVStimulusListener> _listeners;
	private final AnimationPanel _aniPanel;
	private long _audioCallAhead;
	private long _animationFrameAdvance;
	private AVTrial<?> _trial;
	private CountDownLatch _schedulingGuard;
	private CountDownLatch _completionLatch;
	private boolean _running = false;
	private Long _lastStart = null;
	
	public StimulusScheduler() {
		_scheduler = new Scheduler(REFRESH_PERIOD, TimeUnit.NANOSECONDS, MAX_STIMULUS_COUNT);
		_trigger = new ScheduledAnimationTrigger();
		_scheduler.schedule(_trigger);
		_renderer = new AnimationRenderer();
		_animationStart = new AnimationStartAlarm();
		_mediaStart = new MediaPlaybackAlarm();
		_scheduler.scheduleAlarmOnly(new CompletionAlarm(), 0, TimeUnit.NANOSECONDS);
		_animationListener = new AnimationCompletionListener();
		_mediaListener = new MediaCompletionListener();
		_renderer.addAnimationListener(_animationListener);
		_listeners = new ArrayList<AVStimulusListener>();
		_aniPanel = new AnimationPanel(_renderer, _trigger);
		_starter = new VSyncedScheduleStarter();
		_refreshCalculator = new VSyncedRefreshCalcScheduler();
	}
	
	public AnimationPanel getAnimationPanel() {
		return _aniPanel;
	}
	
	public void setRenderCallAhead(Long renderCallAhead, TimeUnit unit) {
		if (!_running) {
			_trigger.setRenderCallAhead(renderCallAhead != null ? 
					TimeUnit.NANOSECONDS.convert(renderCallAhead, unit) : 0);
		}
	}
	
	public void setAudioCallAhead(Long audioCallAhead, TimeUnit unit) {
		if (!_running) {
			_audioCallAhead = audioCallAhead != null ? 
					TimeUnit.NANOSECONDS.convert(audioCallAhead, unit) : 0;
		}
	}
	
	public void setAnimationFrameAdvance(Long animationFrameAdvance, TimeUnit unit) {
		if (!_running) {
			_animationFrameAdvance = animationFrameAdvance != null ? 
					TimeUnit.NANOSECONDS.convert(animationFrameAdvance, unit) : 0;
		}
	}
	
	/**
	 * Set the trial that will provide stimuli at start time.
	 */
	public void setStimulusSource(AVTrial<?> trial) {
		if (_running) return;
		
		_scheduler.unSchedule(_mediaStart);
		_scheduler.unSchedule(_animationStart);
		
		_trial = trial;
		
		if (_trial == null) return;

		_schedulingGuard = new CountDownLatch(1);
		calculateRefreshAndSchedule();
		_completionLatch = new CountDownLatch(_trial.getNumMediaObjects());
	}
	
	/**
	 * Schedule the trial for the given refresh period.
	 */
	private void scheduleTrial(long period) {
		Playable p = _trial.isVideo() ? _trial.getVideoPlayable() : _trial.getAudioPlayable();
		long adjust = 0;
		if (_trial.getAnimationSequence() != null) {
			_renderer.setAnimationSource(_trial);
			
			long aniDelay = _trial.getAnimationDelayNanos();
			adjust = (_trial.getAnimationStrikeTimeNanos() + aniDelay) % period;
			_scheduler.scheduleAlarmOnly(_animationStart, aniDelay - adjust, TimeUnit.NANOSECONDS);
		}
		if (p != null) {
			p.addListener(_mediaListener);
			long time = (_trial.getMediaDelayNanos() - adjust) + 
					(PERIOD_OFFSET_MULTIPLIER*period); // XXX: ~2+ periods for vsync buffering
			_scheduler.scheduleAlarmOnly(_mediaStart, time, TimeUnit.NANOSECONDS);
		}
		_schedulingGuard.countDown();
	}
	
	/**
	 * Run the refresh period calculation renderer.
	 */
	private void calculateRefreshAndSchedule() {
		if (_trial.isAnimation()) {
			// is this the best way to do this?
			_renderer.setDisplayProxy(_refreshCalculator);
			//XXX: executor service?
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					_trigger.forceDisplay();
				}}) {{
				setPriority(Thread.MAX_PRIORITY);
			}};
			t.start();
		}
		else {
			scheduleTrial(0);
		}
	}
	
	public void start() {
		if (_running || _trial == null) return;
		_running = true;

		_lastStart = null;
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					_schedulingGuard.await();
				} 
				catch (InterruptedException e) {} // ignore*/
				
				if (_trial.isAnimation()) {
					_renderer.setDisplayProxy(_starter);
					_trigger.forceDisplay();
				}
				else {
					_scheduler.start();
				}
			}}) {{
			setPriority(Thread.MAX_PRIORITY);
		}};
		t.start();
	}
	
	/**
	 * Stop the scheduler and return the reference start time used on the last run in nanoseconds.
	 */
	public Long stop() {
		if (_lastStart == null) {
			_lastStart = _scheduler.stop();
		}
		
		String runDesc = "\t---- Trial run complete. ---\n\tReference start time: %d\n" +
				"%s%s--------------------------\n\n";
		String aniDesc = "";
		String medDesc = "";
		long millisVal = 0;
		if (_trial.getNumMediaObjects() > 1) {
			millisVal = TimeUnit.MILLISECONDS.convert(
					_trial.getLastAnimationStartNanos() - _lastStart, TimeUnit.NANOSECONDS);
			aniDesc = String.format("\tRelative animation start time: %d\n", millisVal);
			millisVal = TimeUnit.MILLISECONDS.convert(
					_trial.getLastMediaStartNanos() - _lastStart, TimeUnit.NANOSECONDS);
			medDesc = String.format("\tRelative media start time: %d\n", millisVal);
		}
		millisVal = TimeUnit.MILLISECONDS.convert(_lastStart, TimeUnit.NANOSECONDS);
		LogContext.getLogger().fine(String.format(runDesc, millisVal, aniDesc, medDesc));

		_running = false;
		return _lastStart;
	}
	
	/**
	 * Add a listener.
	 */
	public void addStimulusListener(AVStimulusListener listener) {
		_listeners.add(listener);
	}
	
	/**
	 * Remove a listener.
	 */
	public void removeStimulusListener(AVStimulusListener listener) {
		_listeners.remove(listener);
	}
	
	/**
	 * Notify listeners of stimuli completion.
	 */
	private void notifyListeners() {
		for (AVStimulusListener listener : _listeners) {
			listener.stimuliComplete();
		}
	}
	
	/**
	 * Class for scheduling media playback.
	 */
	private class MediaPlaybackAlarm implements Scheduled {
		@Override
		public void markTime(ScheduleEvent e) {}
		@Override
		public void alarm(ScheduleEvent e) {
			long time = System.nanoTime();
			//System.out.println("media:\t" + time);
			_trial.markMediaStartNanos(time);
			_trial.getMedia().getMediaObject().play();
		}
		@Override
		public long callAheadNanoTime() {
			return _trial.isVideo() ? 0 : _audioCallAhead;
		}
	}
	
	/**
	 * Class for initializing the renderer's reference time at the scheduler start time.
	 */
	private class AnimationStartAlarm implements Scheduled {
		@Override
		public void markTime(ScheduleEvent e) {}
		@Override
		public void alarm(ScheduleEvent e) {
			// TODO - use time from event?
			_trial.markAnimationStartNanos(System.nanoTime());
			_renderer.setNanoStartTime(e.getEventTime(TimeUnit.NANOSECONDS));
		}
		@Override
		public long callAheadNanoTime() {
			return _animationFrameAdvance;
		}
	}
	
	/**
	 * Class for listening to animations being completed via the renderer.
	 */
	private class AnimationCompletionListener implements AnimationListener {
		@Override
		public void animationDone() {
			_renderer.setAnimationSource(null);
			_completionLatch.countDown();
		}
	}
	
	/**
	 * Class for listening to media playback completion.
	 */
	private class MediaCompletionListener extends PlayableListener {
		@Override
		public void playableEnded() {
			_trial.getMedia().getMediaObject().removeListener(this);
			_completionLatch.countDown();
		}
	}
	
	/**
	 * Class for providing completion notification.
	 */
	private class CompletionAlarm implements Scheduled {
		@Override
		public void markTime(ScheduleEvent e) {}
		@Override
		public void alarm(ScheduleEvent e) {
			try {
				_completionLatch.await();
			} 
			catch (InterruptedException e1) {}
			
			_lastStart = _scheduler.stop();
			notifyListeners();
		}
		@Override
		public long callAheadNanoTime() { return 0; }
	}
	
	/**
	 * Class for calculating the current refresh period on-the-fly.
	 */
	private class VSyncedRefreshCalcScheduler implements GLDrawDelegate {
		private static final long ITERATIONS = 100;
		@Override
		public void draw(GLAutoDrawable drawable) {
			_renderer.clearProxy(); // remove self
			
			GL2 gl = drawable.getGL().getGL2();
			gl.setSwapInterval(1);
			
			gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			long curr = System.nanoTime();
			long total = 0;
			
			if (APPLE_MODE) {
				for (int i = 0; i < ITERATIONS; i++) { 
					float alpha = 1.0f - (float) (.01 * i % 2);
					gl.glClearColor(0.0f, 0.0f, 0.0f, alpha);
					gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
					gl.glSwapAPPLE();
					long tmp = System.nanoTime();
					total += tmp - curr;
					curr = tmp;
				}
			}
			else {
				for (int i = 0; i < ITERATIONS; i++) { 
					float alpha = 1.0f - (float) (.01 * i % 2);
					gl.glClearColor(0.0f, 0.0f, 0.0f, alpha);
					gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
					gl.glFinish(); // TODO: would this even work?
					long tmp = System.nanoTime();
					total += curr - tmp;
					curr = tmp;
				}
			}
			long period = total / ITERATIONS;
			// cap the refresh period at an arbitrary, unreasonable value
			if (period < REFRESH_PERIOD / 20) period = REFRESH_PERIOD;
			_scheduler.setUpdatePeriodNanos(period);
			scheduleTrial(period);
		}
	}
	
	/**
	 * Class for attempting to start the scheduler at the front edge of the animation
	 * update period.
	 */
	private class VSyncedScheduleStarter implements GLDrawDelegate {
		@Override
		public void draw(GLAutoDrawable drawable) {
			_renderer.clearProxy(); // remove self
			CountDownLatch latch = new CountDownLatch(1);
			_scheduler.start(latch);
			
			GL2 gl = drawable.getGL().getGL2();
			gl.setSwapInterval(1);
			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);  
			
			if (APPLE_MODE) {
				gl.glSwapAPPLE();
			}
			else {
				gl.glFinish(); // TODO: would this even work?
			}
			
			// release scheduler to start after vsync
			latch.countDown();
		}
	}
	
	/**
	 * Class for rendering testing.
	 */
	private class TestRenderer implements GLDrawDelegate {
		int _count = 100;
		@Override
		public void draw(GLAutoDrawable drawable) {
			
			GL2 gl = drawable.getGL().getGL2();
			if (_count % 2 == 0) {
				gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);   
			}
			else {
				gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			}
			gl.glClear(GL_COLOR_BUFFER_BIT);
			--_count;
			
			if (_count == 0) {
				_renderer.clearProxy();
			}
		}
	}
}
