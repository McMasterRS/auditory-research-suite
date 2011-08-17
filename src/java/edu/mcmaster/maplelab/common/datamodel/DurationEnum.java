package edu.mcmaster.maplelab.common.datamodel;

public enum DurationEnum {
	SHORT("Short"),
    LONG("Long"),
    NORMAL("Normal"),
    NONE("None") {
		@Override
		public String codeString() {
	    	return "";
	    }
	};
        
    private final String _displayName;
    private DurationEnum(String displayName) {
        _displayName = displayName;
    }
    
    @Override
    public String toString() {
        return _displayName;
    }
    
    public String codeString() {
    	return name().substring(0, 1);
    }
}
