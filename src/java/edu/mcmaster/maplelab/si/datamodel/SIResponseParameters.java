package edu.mcmaster.maplelab.si.datamodel;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.ContinuousResponseParameters;
import edu.mcmaster.maplelab.common.datamodel.DurationResponse;
import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration.Duration;
import edu.mcmaster.maplelab.common.datamodel.IntegerResponse;

public abstract class SIResponseParameters<T> extends ContinuousResponseParameters<SISession, T> {
	
	private enum ConfigLabels {
		durationLow,
		durationHigh,
		agreementLow,
		agreementHigh,
		durationMin,
		durationMax,
		agreementMin,
		agreementMax
	}

	public SIResponseParameters(SISession session, int min, int max) {
		super(session, min, max);
	}

	@Override
	public String getQuestion() {
		return null;
	}

	@Override
	public abstract Answer[] getAnswers();
	
	// TODO: consider using Duration and DurationResponse here
	public static class SIDurationResponseParameters extends SIResponseParameters<Integer> {
		private final String _durLow;
		private final String _durHigh;

		public SIDurationResponseParameters(SISession session) {
			super(session, session.getInteger(ConfigLabels.durationMin, 0), 
					session.getInteger(ConfigLabels.durationMax, 101));

			_durLow = session.getString(ConfigLabels.durationLow, "Short");
			_durHigh = session.getString(ConfigLabels.durationHigh, "Long");
		}

		@Override
		public Answer[] getAnswers() {
			return Answer.values(_durLow, _durHigh);
		}

		@Override
		public IntegerResponse getResponseForValue(int value) {
			return new IntegerResponse(getAnswerForValue(value), 
					value); // XXX: no duration object for now
		}
	}
	
	public static class SIAgreementResponseParameters extends SIResponseParameters<Integer> {
		private final String _agreeLow;
		private final String _agreeHigh;

		public SIAgreementResponseParameters(SISession session) {
			super(session, session.getInteger(ConfigLabels.agreementMin, 0), 
					session.getInteger(ConfigLabels.agreementMax, 101));
			
			_agreeLow = session.getString(ConfigLabels.agreementLow, "Low Agreement");
			_agreeHigh = session.getString(ConfigLabels.agreementHigh, "High Agreement");
		}

		@Override
		public Answer[] getAnswers() {
			return Answer.values(_agreeLow, _agreeHigh);
		}

		@Override
		public IntegerResponse getResponseForValue(int value) {
			return new IntegerResponse(getAnswerForValue(value), value);
		}
	}
	
	public static Duration getDuration(DurationResponse r) {
		return r != null ? r.getValue() : null;
	}
	
	public static boolean isLowAgreement(IntegerResponse r) {
		return r != null && r.getAnswer().ordinal() <= 0;
	}

}
