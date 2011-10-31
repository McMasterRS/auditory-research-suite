package edu.mcmaster.maplelab.common.datamodel;

public class IntegerResponse extends Response<Integer> {

	public IntegerResponse(Answer answer, Integer value) {
		super(answer, value);
	}
    
    @Override
    public String toString() {
        return String.format("Response: answer=\"%s\", value=\"%d\"", 
        		getAnswer(), getValue());
    }

}
