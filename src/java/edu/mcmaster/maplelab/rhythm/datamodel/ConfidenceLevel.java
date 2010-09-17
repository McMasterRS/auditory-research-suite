package edu.mcmaster.maplelab.rhythm.datamodel;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is a dynamic replacement for the old, hard-coded ConfidenceRatingEnum.
 * The class must be initialized (initialize(RhythmSession session)) to populate
 * its values.
 * 
 * @author <a href="mailto:ben.guseman@mseedsoft.com">Ben Guseman</a>
 */
public class ConfidenceLevel {
	/**
	 * Property keys for ConfidenceLevel properties in the session.
	 */
	private enum Keys {
		confidence,
		confidenceMin,
		confidenceOrderHighToLow
	}
	
	/** ConfidenceLevel values extracted from the session properties file. */
	private static ArrayList<ConfidenceLevel> _levels = new ArrayList<ConfidenceLevel>();
	/** Initialization indicator. */
	private static boolean _initialized = false;
	
	/**
	 * Initialize all the ConfidenceLevel values valid for this session.
	 */
	public static void initialize(RhythmSession session) {
		String keyFormat = Keys.confidence + ".%s";
		String description = null;
		for (int counter = Integer.valueOf(session.getString(Keys.confidenceMin, "0")); 
				(description = session.getString(String.format(keyFormat, counter))) != null; 
				counter++) {
			
			_levels.add(new ConfidenceLevel(counter, description));
		}
		
		// Reverse the order, if required - this is a linear-time operation,
		// but the number of confidence levels should realistically stay < 10
		boolean reverse = Boolean.valueOf(
				session.getString(Keys.confidenceOrderHighToLow, "true"));
		if (reverse) Collections.reverse(_levels);
		
		_initialized = true;
	}
	
	/**
	 * Get all valid ConfidenceLevel values after initialization.
	 */
	public static ConfidenceLevel[] values() {
		if (!initialized()) return null;
		
		ConfidenceLevel[] retval = new ConfidenceLevel[_levels.size()];
		return _levels.toArray(retval);
	}
	
	/**
	 * Indicate if the class has been initialized.  No values are
	 * populated before initialization.
	 */
	public static boolean initialized() {
		return _initialized;
	}
	
	private final int _ordinal;
	private final String _description;
	
	private ConfidenceLevel(int ordinal, String description) {
		_ordinal = ordinal;
		_description = description;
	}
	
	public int ordinal() {
		return _ordinal;
	}
	
	@Override
	public String toString() {
		return _description;
	}
}
