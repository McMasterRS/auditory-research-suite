package edu.mcmaster.maplelab.si;

import edu.mcmaster.maplelab.av.AVStimulusResponseScreen;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.SliderResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;
import edu.mcmaster.maplelab.si.datamodel.SIBlock;
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;
import edu.mcmaster.maplelab.si.datamodel.SIResponseParameters.SIDurationResponseParameters;
import edu.mcmaster.maplelab.si.datamodel.SIResponseParameters.SIAgreementResponseParameters;

public class SIStimulusResponseScreen extends AVStimulusResponseScreen<Response[], SIBlock, SITrial, 
									SITrialLogger, SISession> {

	public SIStimulusResponseScreen(StepManager steps, SISession session,
			boolean isWarmup) {
		super(steps, session, isWarmup);
	}

	@Override
	public ResponseInputs<Response[]> createResponseInputs(SISession session) {
		return new SliderResponseInputs(new SIDurationResponseParameters(session), 
				new SIAgreementResponseParameters(session));
	}

}
