/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A class for scheduling events to occur at specific delays relative to an initial
 * start time.  This class is not intended for absolute scheduling of events; rather,
 * it tries to provide accurate timing for events relative to one another.  Because
 * of this, overhead occurs up-front as much as possible, and the actual start time
 * is not initially known.
 * 
 * @author bguseman
 *
 */
public class Scheduler {
	private long _startTime;
	private long _updatePeriod;
	private final int _threads;
	private final ScheduledExecutorService _executor;
	private TimerClock _clock;
	private ScheduledFuture<?> _clockFuture;
	private final Set<Alarm> _alarms;
	private final Set<Metronome> _recurring;
	private CountDownLatch _controlLatch = null;
	
	/**
	 * Create a scheduler that runs repeated events after every update period
	 * and utilizes up to the given number of threads.  If more items are 
	 * scheduled than threads are available, timing for some items will be
	 * incorrect.
	 */
	public Scheduler(long updatePeriod, TimeUnit unit, int threads) {
		_updatePeriod = TimeUnit.NANOSECONDS.convert(updatePeriod, unit);
		_threads = threads;
		_alarms = new HashSet<Alarm>();
		_recurring = new HashSet<Metronome>();
		_executor = Executors.newScheduledThreadPool(1, new PriorityThreadFactory());
		_clock = new TimerClock();
	}
	
	public void setUpdatePeriod(long updatePeriod) {
		_updatePeriod = updatePeriod;
	}

	/**
	 * Start the scheduler.  Some initialization overhead will occur.
	 * The scheduler will not actually begin until the latch is released.
	 */
	public void start(CountDownLatch controlLatch) {
		_controlLatch = controlLatch;
		
		startTimer();
	}

	/**
	 * Start the scheduler.  Some initialization overhead will occur.
	 */
	public void start() {
		start(null);
	}
	
	/**
	 * Stop the scheduler and return the start time that was used on the last run.
	 */
	public long stop() {
		stopTimer();
		long retval = _startTime;
		_startTime = 0;
		return retval;
	}
	
	/**
	 * Remove the given Scheduled item.
	 */
	public void unSchedule(Scheduled sched) {
		_recurring.remove(new Metronome(sched));
		_alarms.remove(new Alarm(sched, (long) 0));
	}
	
	/**
	 * Schedule the given action to occur repeatedly every period via Scheduled.markTime.
	 */
	public void schedule(Scheduled sched) {
		_recurring.add(new Metronome(sched));
	}
	
	/**
	 * Schedule the given action to occur once after the given alarm delay via
	 * Scheduled.alarm.
	 */
	public void scheduleAlarmOnly(Scheduled sched, long alarmTime, TimeUnit unit) {
		long time = TimeUnit.NANOSECONDS.convert(alarmTime, unit);
		_alarms.add(new Alarm(sched, time - sched.callAheadNanoTime()));
	}
	
	private void startTimer() {
		if (_clockFuture != null) return;
		
		_clockFuture = _executor.schedule(_clock, 0, TimeUnit.NANOSECONDS);
		
		//_clockFuture = _executor.scheduleAtFixedRate(_clock, 0, 
		//		_updatePeriod, TimeUnit.NANOSECONDS);
	}
	
	private void stopTimer() {
		if (_clockFuture != null) {
			//_clockFuture.cancel(true);
			_clockFuture = null;
			_clock.shutDown();
			_controlLatch = null;
			_clock = new TimerClock();
		}
	}
	
	private long currTime() {
		return System.nanoTime();
	}
	

	
	/**
	 * Class for running scheduled items at regular intervals.
	 */
	private class TimerClock implements Runnable {
		private final ScheduledExecutorService _exec = 
				Executors.newScheduledThreadPool(_threads, new PriorityThreadFactory());
		
		public void shutDown() {
			_exec.shutdown();
			_exec.shutdownNow();
		}
		
		@Override
		public void run() {
			if (_controlLatch != null) {
				try {
					_controlLatch.await();
				}
				catch (InterruptedException e) {} // if wait fails, just move on
			}
			
			// XXX: 'call ahead' values larger than the update period could cause
			// serious problems
			_startTime = currTime() + _updatePeriod;
			for (Alarm alarm : _alarms) {
				_exec.schedule(alarm, alarm.getDelay() + _updatePeriod, TimeUnit.NANOSECONDS);
			}
			
			for (Metronome m : _recurring) {
				_exec.scheduleAtFixedRate(m, _updatePeriod - m.callAhead(), _updatePeriod, TimeUnit.NANOSECONDS);
			}
		}
	}
	
	/**
	 * Class for running periodic event updates.
	 */
	private class Metronome implements Runnable {
		private final Scheduled _sched;
		public Metronome(Scheduled s) {
			_sched = s;
		}
		
		public long callAhead() {
			return _sched.callAheadNanoTime();
		}
		
		@Override
		public void run() {
			long time = currTime();
			_sched.markTime(new ScheduleEvent(time, time - _startTime));
		}
		
		/********* Must be implemented to support removal! *******/
		@Override
		public boolean equals(Object o) {
			return o instanceof Metronome && ((Metronome) o)._sched == this._sched;
		}
		@Override
		public int hashCode() {
			return _sched.hashCode();
		}
		/*********************************************************/
	}
	
	/**
	 * Class for running alarm event updates (one time events).
	 */
	private class Alarm implements Runnable {
		private final Scheduled _sched;
		private final long _delay;
		public Alarm(Scheduled s, Long delay) {
			_sched = s;
			_delay = delay;
		}
		
		public long getDelay() {
			return _delay;
		}
		
		@Override
		public void run() {
			long time = currTime();
			_sched.alarm(new ScheduleEvent(time, time - _startTime));
		}

		/********* Must be implemented to support removal! *******/
		@Override
		public boolean equals(Object o) {
			return o instanceof Alarm && ((Alarm) o)._sched == this._sched;
		}
		@Override
		public int hashCode() {
			return _sched.hashCode();
		}
		/*********************************************************/
	}
	
	private static class PriorityThreadFactory implements ThreadFactory {
		ThreadFactory _parent = Executors.defaultThreadFactory();
		@Override
		public Thread newThread(Runnable r) {
			Thread retval = _parent.newThread(r);
			retval.setPriority(Thread.MAX_PRIORITY - (r instanceof Metronome ? 0 : 1));
			return retval;
		}
	}
	
	public static void main(String[] args) {
		try {
			Scheduler scheduler = new Scheduler(1000, TimeUnit.MILLISECONDS, 5);
			scheduler.schedule(new Scheduled() {
				@Override
				public void markTime(ScheduleEvent e) {
					long time = e.getRelativeTime(TimeUnit.MILLISECONDS);
					System.out.println("metronome only: " + time);
				}
				@Override
				public void alarm(ScheduleEvent e) {
				}
				@Override
				public long callAheadNanoTime() {
					return 0;
				}
			});
			scheduler.scheduleAlarmOnly(new Scheduled() {
				@Override
				public void markTime(ScheduleEvent e) {
				}
				@Override
				public void alarm(ScheduleEvent e) {
					long time = e.getRelativeTime(TimeUnit.MILLISECONDS);
					System.out.println("alarm only: " + time);
				}
				@Override
				public long callAheadNanoTime() {
					return 0;
				}
			}, 3000, TimeUnit.MILLISECONDS);
			
			scheduler.start();
			Thread.sleep(12000);
			scheduler.stop();
		} 
		catch (Exception e1) { 
			System.exit(1);
		}
		 
		System.exit(0);
	}
}
