package edu.mcmaster.maplelab.av;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.mcmaster.maplelab.common.LogContext;

public class TimeTracker {
	private static Map<String, List<Long>> _timeStamps = new HashMap<String, List<Long>>();
	private static ExecutorService _executor = Executors.newSingleThreadExecutor();
	
	/**
	 * Record the current time for the given identifier.  Does not block.
	 */
	public static void timeStamp(final String identifier) {
		final long currTime = System.currentTimeMillis();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				List<Long> stamps = _timeStamps.get(identifier);
				if (stamps == null) {
					stamps = new ArrayList<Long>();
					_timeStamps.put(identifier, stamps);
				}
				stamps.add(currTime);
			}
		};
		_executor.execute(r);
	}
	
	/**
	 * Log all times stamped for the given identifier in the given file.  Stamps are cleared
	 * according to the given boolean.  Does not block - file will be written in a separate
	 * thread.
	 */
	public static void logTimes(final String identifier, final File logFile, 
			final boolean clearStamps) {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				List<Long> stamps = clearStamps ? 
						_timeStamps.remove(identifier) : _timeStamps.get(identifier);
				if (stamps == null) return;
				
				FileWriter fw = null;
				try {
					fw = new FileWriter(logFile);
					fw.append(String.format("Times stamped for %s\n", identifier));
					for (Long ts : stamps) {
						fw.append(String.format("%d\n", ts));
					}
					LogContext.getLogger().info("Timestamp log written to " + 
							logFile.getAbsolutePath());
				} 
				catch (IOException e) {
					LogContext.getLogger().severe("Timestamp log failed for " + 
							logFile.getAbsolutePath());
				}
			}
		};
		
		_executor.execute(r);
	}
	
	/**
	 * Clear all time stamps for the given identifier and return them in
	 * a list.
	 */
	public static List<Long> clearStamps(String identifier) {
		return _timeStamps.remove(identifier);
	}
}
