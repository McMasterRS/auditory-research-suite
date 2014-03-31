package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.common.datamodel.BinaryAnswer;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceLevel;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class TOJResponseParameters extends ResponseParameters<TOJSession, ConfidenceLevel> {
	
	public TOJResponseParameters(TOJSession session) {
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

	public static boolean isDotFirst(ConfidenceResponse response) {
		return response != null && response.getAnswer().ordinal() == 0;
	}
}
