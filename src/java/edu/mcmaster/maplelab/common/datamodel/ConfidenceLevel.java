package edu.mcmaster.maplelab.common.datamodel;

import java.util.ArrayList;
import java.util.Collections;


/**
 * This class is a dynamic replacement for the old, hard-coded ConfidenceRatingEnum.
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
	
	/**
	 * Get all valid ConfidenceLevel values.
	 */
	public static <S extends Session<?, ?, ?>> ConfidenceLevel[] values(S session) {
		ArrayList<ConfidenceLevel> levels = new ArrayList<ConfidenceLevel>();
		String keyFormat = Keys.confidence + ".%s";
		String description = null;
		for (int counter = Integer.valueOf(session.getString(Keys.confidenceMin, "0")); 
				(description = session.getString(String.format(keyFormat, counter))) != null; 
				counter++) {
			
			levels.add(new ConfidenceLevel(counter, description));
		}
		
		// Reverse the order, if required
		boolean reverse = Boolean.valueOf(
				session.getString(Keys.confidenceOrderHighToLow, "true"));
		if (reverse && levels.size() > 1) Collections.reverse(levels);
		
		ConfidenceLevel[] retval = new ConfidenceLevel[levels.size()];
		return levels.toArray(retval);
	}
	
	/**
	 * Generate a generic set of ConfidenceLevel values.
	 */
	public static ConfidenceLevel[] values(int min, int count) {
		ArrayList<ConfidenceLevel> levels = new ArrayList<ConfidenceLevel>();
		int currCount = 0;
		for (int i = min; currCount < count; i++) {
			levels.add(new ConfidenceLevel(i, String.valueOf(i)));
			currCount++;
		}
		ConfidenceLevel[] retval = new ConfidenceLevel[levels.size()];
		return levels.toArray(retval);
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
