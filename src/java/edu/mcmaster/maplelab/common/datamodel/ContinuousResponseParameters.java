package edu.mcmaster.maplelab.common.datamodel;

public abstract class ContinuousResponseParameters<S extends Session<?, ?, ?>> 
														extends ResponseParameters<S> {
	private final int _rangeMin;
	private final int _rangeCount;
	private final int _middleValue;
	private final ConfidenceLevel[] _levels;

	public ContinuousResponseParameters(S session, int rangeMin, int rangeCount) {
		super(session);
		_rangeMin = rangeMin;
		_rangeCount = rangeCount;
		_middleValue = (int) Math.floor(_rangeCount/2 + _rangeMin);
		_levels = ConfidenceLevel.values(_rangeMin, _rangeCount);
	}
	
	public ConfidenceLevel getLevelForValue(int value) {
		return _levels[value - _rangeMin];
	}
	
	public Answer getAnswerForValue(int value) {
		if (value == _middleValue) return Answer.NEUTRAL;
		else if (value < _middleValue) return getAnswers()[0];
		else return getAnswers()[1];
	}
	
	@Override
	public ConfidenceLevel[] getConfidenceLevels() {
		return _levels;
	}

	@Override
	public boolean isDiscrete() {
		return false;
	}
	
	/**
	 * Get the middle/neutral value of the continuum.
	 */
	public int getMiddleValue() {
		return _middleValue;
	}

	@Override
	public abstract String getQuestion();

	@Override
	public abstract Answer[] getAnswers();

}
