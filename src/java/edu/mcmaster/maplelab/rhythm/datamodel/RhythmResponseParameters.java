package edu.mcmaster.maplelab.rhythm.datamodel;

import edu.mcmaster.maplelab.common.datamodel.BinaryAnswer;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceLevel;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class RhythmResponseParameters extends ResponseParameters<RhythmSession, ConfidenceLevel> {

	public RhythmResponseParameters(RhythmSession session) {
		super(session);
	}

	@Override
	public ConfidenceLevel[] getDiscreteValues() {
		return ConfidenceLevel.values(getSession());
	}

	@Override
	public boolean isDiscrete() {
		return true;
	}

	@Override
	public String getQuestion() {
		return getSession().getString("question.label", "Question");
	}

	@Override
	public BinaryAnswer[] getAnswers() {
		return BinaryAnswer.values(getSession());
	}
	
	/**
     * Indicate if the response indicates that the probe tone was accurate.
     * Returns false if no response yet.
     */
	public static boolean isProbeToneAccurate(ConfidenceResponse response) {
		return response != null && response.getAnswer().ordinal() == 0;
	}

}
