package edu.mcmaster.maplelab.si.datamodel;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.ContinuousResponseParameters;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.Response;

public abstract class SIResponseParameters extends ContinuousResponseParameters<SISession> {
	
	private enum ConfigLabels {
		durationLow,
		durationHigh,
		agreementLow,
		agreementHigh
	}

	public SIResponseParameters(SISession session) {
		super(session, 0, 101);
	}

	@Override
	public String getQuestion() {
		return null;
	}

	@Override
	public abstract Answer[] getAnswers();
	
	public static class SIDurationResponseParameters extends SIResponseParameters {
		private final String _durLow;
		private final String _durHigh;

		public SIDurationResponseParameters(SISession session) {
			super(session);

			_durLow = session.getString(ConfigLabels.durationLow, "Short");
			_durHigh = session.getString(ConfigLabels.durationHigh, "Long");
		}

		@Override
		public Answer[] getAnswers() {
			return Answer.values(_durLow, _durHigh);
		}
	}
	
	public static class SIAgreementResponseParameters extends SIResponseParameters {
		private final String _agreeLow;
		private final String _agreeHigh;

		public SIAgreementResponseParameters(SISession session) {
			super(session);
			
			_agreeLow = session.getString(ConfigLabels.agreementLow, "Low Agreement");
			_agreeHigh = session.getString(ConfigLabels.agreementHigh, "High Agreement");
		}

		@Override
		public Answer[] getAnswers() {
			return Answer.values(_agreeLow, _agreeHigh);
		}
	}
	
	public static DurationEnum getDuration(Response r) {
		Answer a = r != null ? r.getAnswer() : null;
		if (a == null) return null;
		
		switch (a.ordinal()) {
			case -1: return DurationEnum.NORMAL;
			case 0: return DurationEnum.SHORT;
			case 1: return DurationEnum.LONG;
		}
		
		return null;
	}
	
	public static boolean isLowAgreement(Response r) {
		return r != null && r.getAnswer().ordinal() <= 0;
	}

}
