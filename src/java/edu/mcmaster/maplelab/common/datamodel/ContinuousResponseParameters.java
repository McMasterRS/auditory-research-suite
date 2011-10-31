package edu.mcmaster.maplelab.common.datamodel;

public abstract class ContinuousResponseParameters<S extends Session<?, ?, ?>, DataType> 
														extends ResponseParameters<S, Integer> {
	private final int _rangeMin;
	private final int _rangeMax;
	private final int _middleValue;

	public ContinuousResponseParameters(S session, int rangeMin, int rangeMax) {
		super(session);
		_rangeMin = Math.min(rangeMin, rangeMax);
		_rangeMax = Math.max(rangeMin, rangeMax);
		_middleValue = (int) ((_rangeMax + _rangeMin) / 2);
	}
	
	protected Answer getAnswerForValue(int value) {
		if (value == _middleValue) return Answer.NEUTRAL;
		else if (value < _middleValue) return getAnswers()[0];
		else return getAnswers()[1];
	}
	
	public int getMin() {
		return _rangeMin;
	}
	
	public int getMax() {
		return _rangeMax;
	}
	
	@Override
	public Integer[] getDiscreteValues() {
		return null;
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
	
	public abstract Response<DataType> getResponseForValue(int value);

}
