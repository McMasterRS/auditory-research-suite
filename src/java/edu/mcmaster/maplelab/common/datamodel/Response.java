package edu.mcmaster.maplelab.common.datamodel;

public abstract class Response<T> {
	private final BinaryAnswer _answer;
    private final T _value;

    public Response(BinaryAnswer answer, T value) {
        _answer = answer;
        _value = value;
    }
    
    public T getValue() {
        return _value;
    }
    
    public BinaryAnswer getAnswer() {
        return _answer;
    }
    
    @Override
    public abstract String toString();
}
