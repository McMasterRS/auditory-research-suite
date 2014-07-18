package edu.mcmaster.maplelab.visualizer;

import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import edu.mcmaster.maplelab.common.gui.ExperimentFrame;
import edu.mcmaster.maplelab.visualizer.VisualizerSetupScreen;
import edu.mcmaster.maplelab.si.SIExperiment;
import edu.mcmaster.maplelab.si.SITrialLogger;
import edu.mcmaster.maplelab.visualizer.datamodel.VisualizerSession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;
//import edu.mcmaster.maplelab.visualizer.VisualizerSimpleSetupScreen;

public class VisualizerFrame extends
		ExperimentFrame<VisualizerSession, SITrial, SITrialLogger> {

	public VisualizerFrame(VisualizerSetupScreen setup) {
		super(setup);
	}
	
	public VisualizerFrame(VisualizerSetupScreen setup, boolean warning) {
		super(setup, warning);
	}

	@Override
	protected Container createContent(VisualizerSession session) {
		return new SIExperiment(session);
	}

	@Override
	protected Color getFullScreenBackGround() {
		return Color.DARK_GRAY;
	}

	@Override
	protected VisualizerSession createSession(Properties props) {
		return new VisualizerSession(props);
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

