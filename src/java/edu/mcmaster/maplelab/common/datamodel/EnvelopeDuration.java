package edu.mcmaster.maplelab.common.datamodel;


public class EnvelopeDuration {
	/** Envelope value - for now, a string. */
	private final String _envelope;
	/** Duration. */
	private final Duration _duration;
	
	
	/**
	 * This is not entirely robust, since these strings are given by the experiment
	 * properties file.  Expected to be of the form: ENVELOPE-DURATION, where
	 * DURATION consists of a number value and a unit (e.g., 500ms).
	 */
	public EnvelopeDuration(String valueString) {
		int dash = valueString.indexOf("-");
		if (dash < 0) {
			throw new IllegalArgumentException("String must be of the form [ENV]-[DUR]");
		}
		_envelope = valueString.substring(0, dash);
		_duration = new Duration(valueString.substring(dash+1));
	}
	
	public String getEnvelope() {
		return _envelope;
	}
	
	public Duration getDuration() {
		return _duration;
	}
	
	public String toString() {
		return getEnvelope() + "-" + getDuration().toString();
	}

	public enum DurationUnit {
		ms(1),
		s(1000),
		µs(1/1000);
		
		private final double _multiplier;
		
		private DurationUnit(int multiplier) {
			_multiplier = multiplier;
		}
		
		private double divide(double value) {
			return value / _multiplier;
		}
		
		private double multiply(double value) {
			return _multiplier * value;
		}
		
		public double convert(double value, DurationUnit toUnit) {
			double millis = multiply(value);
			return toUnit.divide(millis);
		}
	}
	
	public static class Duration {
		public static Duration getDuration(int value, DurationUnit unit) {
			return new Duration(value, unit);
		}
		
		private final int _value;
		private DurationUnit _unit = null;
		
		private Duration(String valueString) {

			String val = "";
			String unit = "";
			for (char c : valueString.toCharArray()) {
				if (Character.isDigit(c)) val += c;
				else unit += c;
			}
			
			_value = Integer.valueOf(val);
			try {
				_unit = DurationUnit.valueOf(unit.trim().toLowerCase());
			}
			catch (Exception e) { }
			
		}
		
		private Duration(int value, DurationUnit unit) {
			_value = value;
			_unit = unit;
		}
		
		public Long getMilliseconds() {
			return !isUnitLess() ? (long) _unit.convert(_value, DurationUnit.ms) : null;
		}
		
		@Override
		public String toString() {
			return String.valueOf(_value) + (isUnitLess() ? "" : _unit.name());
		}
		
		public boolean isUnitLess() {
			return _unit == null;
		}
	}
}
