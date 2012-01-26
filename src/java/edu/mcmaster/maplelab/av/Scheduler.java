package edu.mcmaster.maplelab.av;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
	private final long _updatePeriod;
	private final int _threads;
	private final ScheduledExecutorService _executor;
	private TimerClock _clock;
	private ScheduledFuture<?> _clockFuture;
	private final Set<Alarm> _alarms;
	private final Set<Metronome> _recurring;
	
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
		_executor = Executors.newScheduledThreadPool(1);
	}

	/**
	 * Start the scheduler.  Some initialization overhead will occur.
	 */
	public void start() {
		startTimer();
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
	 * Schedule the given action to occur repeatedly every period via Scheduled.markTime.
	 */
	public void schedule(Scheduled sched) {
		_recurring.add(new Metronome(sched));
	}
	
	/**
	 * Schedule the given action to - after the delay - occur repeatedly every period
	 * via Scheduled.markTime.
	 */
	public void scheduleDelayed(Scheduled sched, long alarmTime, TimeUnit unit) {
		if (alarmTime <= 0) {
			schedule(sched);
			return;
		}
		scheduleAlarmOnly(new DelayedMetronome(sched), 
				// scheduled ahead, so subtract two update periods
				TimeUnit.NANOSECONDS.convert(alarmTime, unit) - 2*_updatePeriod, 
				TimeUnit.NANOSECONDS);
	}
	
	/**
	 * Schedule the given action to occur once after the given alarm delay via
	 * Scheduled.alarm.
	 */
	public void scheduleAlarmOnly(Scheduled sched, long alarmTime, TimeUnit unit) {
		long time = TimeUnit.NANOSECONDS.convert(alarmTime, unit);
		_alarms.add(new Alarm(sched, time));
	}
	
	private void startTimer() {
		if (_clockFuture != null) return;
		
		_clockFuture = _executor.scheduleAtFixedRate(_clock = new TimerClock(), 0, 
				_updatePeriod, TimeUnit.NANOSECONDS);
	}
	
	private void stopTimer() {
		if (_clockFuture != null) {
			_clockFuture.cancel(true);
			_clockFuture = null;
			_clock.shutDown();
		}
	}
	
	/**
	 * Class for running scheduled items at regular intervals.
	 */
	private class TimerClock implements Runnable {
		private final ScheduledExecutorService _exec = Executors.newScheduledThreadPool(_threads);
		private boolean _initialized = false;
		
		public void shutDown() {
			_exec.shutdown();
			_exec.shutdownNow();
		}
		
		@Override
		public void run() {
			if (!_initialized) {
				_startTime = System.nanoTime() + _updatePeriod;
				for (Alarm alarm : _alarms) {
					_exec.schedule(alarm, alarm.getDelay() + _updatePeriod, TimeUnit.NANOSECONDS);
				}
				_initialized = true;
			}
			
			for (Metronome m : _recurring) {
				_exec.schedule(m, _updatePeriod, TimeUnit.NANOSECONDS);
			}
		}
	}
	
	/**
	 * Class for scheduling a metronome after an initial delay.
	 */
	private class DelayedMetronome implements Scheduled {
		private final Metronome _metronome;
		
		public DelayedMetronome(Scheduled sched) {
			_metronome = new Metronome(sched);
		}
		@Override
		public void markTime(ScheduleEvent e) {
		}
		@Override
		public void alarm(ScheduleEvent e) {
			_recurring.add(_metronome);
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
		
		@Override
		public void run() {
			long time = System.nanoTime();
			long relative = time - _startTime;
			_sched.markTime(new ScheduleEvent(time, relative));
		}
	}
	
	/**
	 * Class for running alarm event updates.
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
			long time = System.nanoTime();
			long relative = time - _startTime;
			_sched.alarm(new ScheduleEvent(time, relative));
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
			});
			scheduler.scheduleDelayed(new Scheduled() {
				@Override
				public void markTime(ScheduleEvent e) {
					long time = e.getRelativeTime(TimeUnit.MILLISECONDS);
					System.out.println("delayed metronome: " + time);
				}
				@Override
				public void alarm(ScheduleEvent e) {
					long time = e.getRelativeTime(TimeUnit.MILLISECONDS);
					System.out.println("alarm: " + time);
				}
			}, 7000, TimeUnit.MILLISECONDS);
			scheduler.scheduleAlarmOnly(new Scheduled() {
				@Override
				public void markTime(ScheduleEvent e) {
				}
				@Override
				public void alarm(ScheduleEvent e) {
					long time = e.getRelativeTime(TimeUnit.MILLISECONDS);
					System.out.println("alarm only: " + time);
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
