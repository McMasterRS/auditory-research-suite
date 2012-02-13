package edu.mcmaster.maplelab.av;

import sun.misc.Perf;

/**
 * Container class to store alternate implementation for Scheduler.  May be removed if not used.
 * @author bguseman
 *
 */
public class SchedulerThreads {
	private long _startTime;
	
	private abstract class SchedulerThread extends Thread {
		private long _threadStartTime;
		private long _refTime;
		private final Scheduled _sched;
		public SchedulerThread(Scheduled sched) {
			_sched = sched;
			setPriority(MAX_PRIORITY);
		}
		
		protected Scheduled scheduled() {
			return _sched;
		}
		
		protected long setRefTime(long refTime) {
			return _refTime = refTime;
		}
		
		protected long refTime() {
			return _refTime;
		}
		
		protected long startTime() {
			return _threadStartTime;
		}
		
		@Override
		public void start() {
			_threadStartTime = Perf.getPerf().highResCounter();
			_refTime = _threadStartTime;
			super.start();
		}
		
		public abstract void run();
		
		/********* Must be implemented to support removal! *******/
		@Override
		public boolean equals(Object o) {
			return o instanceof SchedulerThread && ((SchedulerThread) o)._sched == this._sched;
		}
		@Override
		public int hashCode() {
			return _sched.hashCode();
		}
		/*********************************************************/
	}
	
	private class MetronomeThread extends SchedulerThread {
		private final long _period;
		private final long _duration;
		public MetronomeThread(Scheduled sched, long period, long duration) {
			super(sched);
			_period = period;
			_duration = duration;
		}

		@Override
		public void run() {
			while (true) {
				long curr = Perf.getPerf().highResCounter();
				if (curr - refTime() >= _period) {
					setRefTime(refTime() + _period);
					scheduled().markTime(new ScheduleEvent(curr, curr - _startTime));
				}
				
				if (curr - startTime() >= _duration) {
					break;
				}
			}
		}
	}
	
	private class AlarmThread extends SchedulerThread {
		private final long _alarmTime;
		public AlarmThread(Scheduled sched, long alarmTime) {
			super(sched);
			_alarmTime = alarmTime;
		}

		@Override
		public void run() {
			while (true) {
				long curr = Perf.getPerf().highResCounter();
				if (curr - startTime() >= _alarmTime) {
					scheduled().alarm(new ScheduleEvent(curr, curr - _startTime));
					break;
				}
			}
		}
		
	}
}
