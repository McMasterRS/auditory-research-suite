package edu.mcmaster.maplelab.common.datamodel;

public abstract class ResponseParameters<S extends Session<?, ?, ?>> {
	private S _session;
	
	public ResponseParameters(S session) {
		_session = session;
	}
	
	public ConfidenceLevel[] getConfidenceLevels() {
		return ConfidenceLevel.values(_session);
	}
	
	public abstract String getQuestion();
	
	public abstract Answer[] getAnswers();
}
