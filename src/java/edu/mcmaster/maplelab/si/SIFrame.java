package edu.mcmaster.maplelab.si;

import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import edu.mcmaster.maplelab.common.gui.ExperimentFrame;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

public class SIFrame extends ExperimentFrame<SISession, SITrial, SITrialLogger> {

	public SIFrame(SimpleSetupScreen<SISession> setup) {
		super(setup);
	}

	@Override
	protected Container createContent(SISession session) {
		return new SIExperiment(session);
	}

	@Override
	protected Color getFullScreenBackGround() {
		return Color.DARK_GRAY;
	}

	@Override
	protected SISession createSession(Properties props) {
		return new SISession(props);
	}

	@Override
	protected SITrialLogger initTrialLogger(File dataDir) {
		try {
            return new SITrialLogger(getSession(), dataDir);
        }
        catch (IOException ex) {
            logError(ex, "Error setting up logger", ex);
            return null;
        }  
	}

}
