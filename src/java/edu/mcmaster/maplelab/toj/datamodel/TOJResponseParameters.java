package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class TOJResponseParameters extends ResponseParameters<TOJSession> {
	
	private enum ConfigLabels {
		dotLabel,
		toneLabel
	}
	
	private final String _dotLabel;
	private final String _toneLabel;

	public TOJResponseParameters(TOJSession session) {
		super(session);
		
		_dotLabel = session.getString(ConfigLabels.dotLabel, "Dot");
		_toneLabel = session.getString(ConfigLabels.toneLabel, "Tone");
	}

	@Override
	public boolean isDiscrete() {
		return true;
	}

	@Override
	public String getQuestion() {
		return "Which came first?";
	}

	@Override
	public Answer[] getAnswers() {
		return Answer.values(_dotLabel, _toneLabel);
	}

	public static boolean isDotFirst(Response response) {
		return response != null && response.getAnswer().ordinal() == 0;
	}
}
