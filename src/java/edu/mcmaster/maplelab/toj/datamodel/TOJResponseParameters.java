package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class TOJResponseParameters extends ResponseParameters<TOJSession> {

	public TOJResponseParameters(TOJSession session) {
		super(session);
	}

	@Override
	public String getQuestion() {
		return "Which came first?";
	}

	@Override
	public Answer[] getAnswers() {
		return Answer.values("Dot", "Tone");
	}

	public static boolean isDotFirst(Response response) {
		return response != null && response.getAnswer().ordinal() == 0;
	}
}
