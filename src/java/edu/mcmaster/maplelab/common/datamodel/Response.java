package edu.mcmaster.maplelab.common.datamodel;

public abstract class Response<T> {
	private final Answer _answer;
    private final T _value;

    public Response(Answer answer, T value) {
        _answer = answer;
        _value = value;
    }
    
    public T getValue() {
        return _value;
    }
    
    public Answer getAnswer() {
        return _answer;
    }
    
    @Override
    public abstract String toString();
}
