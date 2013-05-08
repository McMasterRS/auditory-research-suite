package edu.mcmaster.maplelab.toj.datamodel;

import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.toj.TOJDemoGUIPanel;
import edu.mcmaster.maplelab.toj.TOJExperiment;
import edu.mcmaster.maplelab.toj.TOJTrialLogger;

public class TOJSession extends AVSession<TOJTrialManager, TOJTrial, TOJTrialLogger> {
	
	public TOJSession(Properties props) {
		super(props);
	}

	@Override
	public String getExperimentBaseName() {
		return TOJExperiment.EXPERIMENT_BASENAME;
	}

	@Override
	public TOJDemoGUIPanel getExperimentDemoPanel() {
		return new TOJDemoGUIPanel(this);
	}

	@Override
	public TOJTrialManager initializeTrialManager(boolean warmup) {
		return new TOJTrialManager(this, warmup);
	}
}
