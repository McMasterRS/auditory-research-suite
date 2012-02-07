/**
 * 
 */
package edu.mcmaster.maplelab.av;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.PlayableListener;
import edu.mcmaster.maplelab.av.media.animation.AnimationListener;
import edu.mcmaster.maplelab.av.media.animation.AnimationRenderer;
import edu.mcmaster.maplelab.av.media.animation.AnimationTrigger;
import edu.mcmaster.maplelab.av.media.animation.ScheduledAnimationTrigger;
import edu.mcmaster.maplelab.common.LogContext;

/**
 * @author bguseman
 *
 */
public class StimulusScheduler {
	private static final int MAX_STIMULUS_COUNT = 15;
	private static final long REFRESH_PERIOD;
	private static final StimulusScheduler INSTANCE;
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		int refresh = gs.getDisplayMode().getRefreshRate();
		if (refresh == DisplayMode.REFRESH_RATE_UNKNOWN) refresh = 60;
		REFRESH_PERIOD = (long) (1000000000.0d / (double) refresh); // nanos
		INSTANCE = new StimulusScheduler();
	}
	
	/**
	 * Get the singleton instance.
	 */
	public static StimulusScheduler getInstance() {
		return INSTANCE;
	}
	
	private final Scheduler _scheduler;
	private final ScheduledAnimationTrigger _trigger;
	private final AnimationRenderer _renderer;
	private final AnimationStartAlarm _animationStart;
	private final MediaPlaybackAlarm _mediaStart;
	private final AnimationCompletionListener _animationListener;
	private final MediaCompletionListener _mediaListener;
	private final List<AVStimulusListener> _listeners;
	private AVTrial<?> _trial;
	private CountDownLatch _latch;
	private boolean _running = false;
	private Long _lastStart = null;
	
	private StimulusScheduler() {
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
		
		Playable p = _trial.isVideo() ? _trial.getVideoPlayable() : _trial.getAudioPlayable();
		if (p != null) {
			p.addListener(_mediaListener);
			_scheduler.scheduleAlarmOnly(_mediaStart, _trial.getMediaDelay(), TimeUnit.MILLISECONDS);
		}
		if (_trial.getAnimationSequence() != null) {
			_scheduler.scheduleAlarmOnly(_animationStart, _trial.getAnimationDelay(), TimeUnit.MILLISECONDS);
		}
	}
	
	public void start() {
		if (_running || _trial == null) return;
		_running = true;

		_lastStart = null;
		_latch = new CountDownLatch(_trial.getNumMediaObjects());
		_scheduler.start();
	}
	
	/**
	 * Stop the scheduler and return the reference start time used on the last run in milliseconds.
	 */
	public Long stop() {
		if (_lastStart == null) {
			_lastStart = TimeUnit.MILLISECONDS.convert(_scheduler.stop(), TimeUnit.NANOSECONDS);
		}
		
		String runDesc = "\t---- Trial run complete. ---\n\tReference start time: %d\n" +
				"%s%s--------------------------\n\n";
		String aniDesc = "";
		String medDesc = "";
		if (_trial.getNumMediaObjects() > 1) {
			aniDesc = String.format("\tRelative animation start time: %d\n", 
					_trial.getLastAnimationStart() - _lastStart);
			medDesc = String.format("\tRelative media start time: %d\n", 
					_trial.getLastMediaStart() - _lastStart);
		}
		LogContext.getLogger().fine(String.format(runDesc, _lastStart, aniDesc, medDesc));
		
		_running = false;
		return _lastStart;
	}
	
	/**
	 * Get the animation trigger used by this scheduler.
	 */
	public AnimationTrigger getAnimationTrigger() {
		return _trigger;
	}
	
	/**
	 * Get the animation renderer used by this scheduler.
	 */
	public AnimationRenderer getAnimationRenderer() {
		return _renderer;
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
			// TODO - use time from event?
			_trial.markMediaStart(TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
			_trial.getMedia().getMediaObject().play();
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
			_renderer.setAnimationSource(_trial);
			// TODO - use time from event?
			Long time =  e.getEventTime(TimeUnit.MILLISECONDS);
			_trial.markAnimationStart(time);
			_renderer.setStartTime(time);
		}
	}
	
	/**
	 * Class for listening to animations being completed via the renderer.
	 */
	private class AnimationCompletionListener implements AnimationListener {
		@Override
		public void animationDone() {
			_renderer.setAnimationSource(null);
			_latch.countDown();
		}
	}
	
	/**
	 * Class for listening to media playback completion.
	 */
	private class MediaCompletionListener extends PlayableListener {
		@Override
		public void playableEnded() {
			_trial.getMedia().getMediaObject().removeListener(this);
			_latch.countDown();
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
				_latch.await();
			} 
			catch (InterruptedException e1) {
				
			}
			
			_lastStart = TimeUnit.MILLISECONDS.convert(_scheduler.stop(), TimeUnit.NANOSECONDS);
			notifyListeners();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("blah");
	}
}
