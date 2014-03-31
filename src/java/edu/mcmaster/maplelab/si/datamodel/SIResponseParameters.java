package edu.mcmaster.maplelab.si.datamodel;

import edu.mcmaster.maplelab.common.datamodel.BinaryAnswer;
import edu.mcmaster.maplelab.common.datamodel.ContinuousResponseParameters;
import edu.mcmaster.maplelab.common.datamodel.DurationResponse;
import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration.Duration;
import edu.mcmaster.maplelab.common.datamodel.IntegerResponse;

public abstract class SIResponseParameters<T> extends ContinuousResponseParameters<SISession, T> {
	
	public static ContinuousResponseParameters<?, ?>[] getResponseParameters(SISession s) {
		boolean dur = s.getString(ConfigLabels.durationLow, null) != null;
		boolean agree =  s.getString(ConfigLabels.agreementLow, null) != null;
		
		if (dur && agree) {
			return new ContinuousResponseParameters<?, ?>[] { new SIDurationResponseParameters(s), 
					new SIAgreementResponseParameters(s) };
		}
		else if (dur) {
			return new ContinuousResponseParameters<?, ?>[] { new SIDurationResponseParameters(s) };
		}
		else if (agree) {
			return new ContinuousResponseParameters<?, ?>[] { new SIAgreementResponseParameters(s) };
		}
		
		return new ContinuousResponseParameters<?, ?>[0];
	}
	
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
	public abstract BinaryAnswer[] getAnswers();
	
	// TODO: consider using Duration and DurationResponse here
	private static class SIDurationResponseParameters extends SIResponseParameters<Integer> {
		private final String _durLow;
		private final String _durHigh;

		public SIDurationResponseParameters(SISession session) {
			super(session, session.getInteger(ConfigLabels.durationMin, 0), 
					session.getInteger(ConfigLabels.durationMax, 101));

			_durLow = session.getString(ConfigLabels.durationLow, "Short");
			_durHigh = session.getString(ConfigLabels.durationHigh, "Long");
		}

		@Override
		public BinaryAnswer[] getAnswers() {
			return BinaryAnswer.values(_durLow, _durHigh);
		}

		@Override
		public IntegerResponse getResponseForValue(int value) {
			return new IntegerResponse(getAnswerForValue(value), 
					value); // XXX: no duration object for now
		}
	}
	
	private static class SIAgreementResponseParameters extends SIResponseParameters<Integer> {
		private final String _agreeLow;
		private final String _agreeHigh;

		public SIAgreementResponseParameters(SISession session) {
			super(session, session.getInteger(ConfigLabels.agreementMin, 0), 
					session.getInteger(ConfigLabels.agreementMax, 101));
			
			_agreeLow = session.getString(ConfigLabels.agreementLow, "Low Agreement");
			_agreeHigh = session.getString(ConfigLabels.agreementHigh, "High Agreement");
		}

		@Override
		public BinaryAnswer[] getAnswers() {
			return BinaryAnswer.values(_agreeLow, _agreeHigh);
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
