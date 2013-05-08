/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import edu.mcmaster.maplelab.av.AVStimulusResponseScreen;
import edu.mcmaster.maplelab.common.datamodel.AnswerConfidenceResponseInputs;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;
import edu.mcmaster.maplelab.toj.datamodel.TOJResponseParameters;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * Primary trial run screen for the TOJ experiment.
 */
public class TOJStimulusResponseScreen extends AVStimulusResponseScreen<ConfidenceResponse, 
														TOJTrial, TOJTrialLogger, TOJSession> {

	public TOJStimulusResponseScreen(StepManager steps, TOJSession session, boolean isWarmup) {
		super(steps, session, isWarmup);
    }

	@Override
	public ResponseInputs<ConfidenceResponse> createResponseInputs(TOJSession session) {
		return new AnswerConfidenceResponseInputs(false, true, new TOJResponseParameters(session));
	}

	@Override
	public void updateResponseInputs(TOJTrial trial) {
		// no-op
	}
}
