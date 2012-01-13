/**
 * 
 */
package edu.mcmaster.maplelab.av;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;

/**
 * @author bguseman
 *
 */
public class StimulusScheduler {
	private static final int MAX_STIMULUS_COUNT = 5;
	private static final long REFRESH_PERIOD;
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		int refresh = gs.getDisplayMode().getRefreshRate();
		if (refresh == DisplayMode.REFRESH_RATE_UNKNOWN) refresh = 60;
		REFRESH_PERIOD = (long) (1000.0d / (double) refresh); // millis
	}
	
	private final Scheduler _scheduler;
	
	public StimulusScheduler() {
		_scheduler = new Scheduler(REFRESH_PERIOD, TimeUnit.MILLISECONDS, MAX_STIMULUS_COUNT);
	}
	
	
	
	
	public static void main(String[] args) {
		System.out.println("blah");
	}
}
