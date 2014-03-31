package edu.mcmaster.maplelab.common.datamodel;

import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration.Duration;

public class DurationResponse extends Response<Duration> {

	public DurationResponse(BinaryAnswer answer, Duration value) {
		super(answer, value);
	}
    
    @Override
    public String toString() {
        return String.format("Response: answer=\"%s\", duration=\"%s\"", 
        		getAnswer(), getValue().toString());
    }
}
