/**
 * 
 */
package edu.mcmaster.maplelab.av;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;

import edu.mcmaster.maplelab.av.media.animation.AnimationRenderer;
import edu.mcmaster.maplelab.av.media.animation.AnimationTrigger;
import edu.mcmaster.maplelab.av.media.animation.ScheduledAnimationTrigger;

/**
 * @author bguseman
 *
 */
public class StimulusScheduler {
	private static final int MAX_STIMULUS_COUNT = 5;
	private static final long REFRESH_PERIOD;
	private static final StimulusScheduler INSTANCE;
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		int refresh = gs.getDisplayMode().getRefreshRate();
		if (refresh == DisplayMode.REFRESH_RATE_UNKNOWN) refresh = 60;
		REFRESH_PERIOD = (long) (1000.0d / (double) refresh); // millis
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
	
	private StimulusScheduler() {
		_scheduler = new Scheduler(REFRESH_PERIOD, TimeUnit.MILLISECONDS, MAX_STIMULUS_COUNT);
		_trigger = new ScheduledAnimationTrigger();
		_scheduler.schedule(_trigger);
		_renderer = new AnimationRenderer();
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
	
	public static void main(String[] args) {
		System.out.println("blah");
	}
}
