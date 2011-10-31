package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceLevel;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class TOJResponseParameters extends ResponseParameters<TOJSession, ConfidenceLevel> {
	
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
	public ConfidenceLevel[] getDiscreteValues() {
		return ConfidenceLevel.values(getSession());
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

	public static boolean isDotFirst(ConfidenceResponse response) {
		return response != null && response.getAnswer().ordinal() == 0;
	}
}
