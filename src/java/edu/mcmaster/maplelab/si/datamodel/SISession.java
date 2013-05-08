package edu.mcmaster.maplelab.si.datamodel;

import java.util.Properties;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.si.SIDemoGUIPanel;
import edu.mcmaster.maplelab.si.SIExperiment;
import edu.mcmaster.maplelab.si.SITrialLogger;

public class SISession extends AVSession<SITrialManager, SITrial, SITrialLogger> {

	public SISession(Properties props) {
		super(props);
	}

	@Override
	public String getExperimentBaseName() {
		return SIExperiment.EXPERIMENT_BASENAME;
	}

	@Override
	public DemoGUIPanel<?, SITrial> getExperimentDemoPanel() {
		return new SIDemoGUIPanel(this);
	}

	@Override
	public SITrialManager initializeTrialManager(boolean warmup) {
		return new SITrialManager(this, warmup);
	}

}
