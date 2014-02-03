package edu.mcmaster.maplelab.common.datamodel;

public abstract class ResponseParameters<S extends Session<?, ?, ?>, DataType> {
	private S _session;
	
	public ResponseParameters(S session) {
		_session = session;
	}
	
	public S getSession() {
		return _session;
	}
	
	public abstract DataType[] getDiscreteValues();
	
	public abstract boolean isDiscrete();
	
	public abstract String getQuestion();
	
	public abstract Answer[] getAnswers();
}
