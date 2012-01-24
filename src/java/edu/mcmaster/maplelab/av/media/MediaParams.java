package edu.mcmaster.maplelab.av.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.common.LogContext;

/**
 * Class for enumerating all parameters contributing to media object creation.
 * 
 * @author bguseman
 */
public class MediaParams {
	private static final Map<String, MediaParams> _parameters = new HashMap<String, MediaParams>();
	private static boolean _loaded = false;
	
	public static void loadMediaParams(AVSession<?, ?, ?> session) {
		if (_loaded) return;
		else _loaded = true;
		
		for (MediaType<?> mt : MediaType.values()) {
			for (String param : mt.getParams(session)) {
				if (_parameters.containsKey(param)) continue;
				_parameters.put(param, new MediaParams(param, session));
			}
		}
	}
	
	public static MediaParams getAvailableValues(String param) {
		return _parameters.get(param);
	}
	
	public static MediaParams valueOf(String param) {
		return getAvailableValues(param);
	}
	
	private final String _name;
	private final List<MediaParamValue> _values;
	
	private MediaParams(String name, AVSession<?, ?, ?> session) {
		_name = name;
		_values = new ArrayList<MediaParamValue>();
		List<String> strValues = session.getStringList(_name, (String[]) null);
		if (strValues != null) {
			for (String str : strValues) {
				_values.add(new MediaParamValue(_name, str));
			}
		}
		else {
			LogContext.getLogger().warning(String.format(
					"No values available for parameter %s", _name));
		}
	};
	
	public List<MediaParamValue> getValues() {
		return Collections.unmodifiableList(_values);
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	public static class MediaParamValue {
		private final String _paramName;
		private final String _paramValue;
		
		private MediaParamValue(String paramName, String paramValue) {
			_paramName = paramName;
			_paramValue = paramValue;
		}
		
		public String paramName() {
			return _paramName;
		}
		
		public String paramValue() {
			return _paramValue;
		}
	}
}
