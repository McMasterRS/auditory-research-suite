package edu.mcmaster.maplelab.common.datamodel;

public enum DurationEnum {
	SHORT("Short"),
    LONG("Long"),
    NORMAL("Normal"),
    DAMPED("Damped") {
		@Override
		public String codeString() {
	    	return "p";
	    }
	},
    NONE("None") {
		@Override
		public String codeString() {
	    	return "x";
	    }
	};
	
	public static DurationEnum fromCodeString(String code) {
		for (DurationEnum dur : values()) {
			if (dur.codeString().equals(code.toLowerCase().trim())) return dur;
		}
		return null;
	}
	
	public static DurationEnum[] unDampedValues() {
		DurationEnum[] vals = values();
		DurationEnum[] retval = new DurationEnum[vals.length-1];
		int count = 0;
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] == DAMPED) continue;
			retval[count] = vals[i];
			count++;
		}
		return retval;
	}
        
    private final String _displayName;
    private DurationEnum(String displayName) {
        _displayName = displayName;
    }
    
    @Override
    public String toString() {
        return _displayName;
    }
    
    public String codeString() {
    	return name().substring(0, 1).toLowerCase();
    }
}
