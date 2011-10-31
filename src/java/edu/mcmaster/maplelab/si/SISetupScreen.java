package edu.mcmaster.maplelab.si;

import java.io.IOException;
import java.util.prefs.Preferences;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.si.datamodel.SISession;

public class SISetupScreen extends SimpleSetupScreen<SISession> {

	public SISetupScreen() {
		super(SIExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), true, false);
	}

	@Override
	protected void initializeBuildInfo() throws IOException {
		Experiment.initializeBuildInfo(SIExperiment.class, getPrefsPrefix());
	}

	@Override
	protected void addExperimentFields() {
	}

	@Override
	protected void applyExperimentSettings(SISession session) {
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
	}

	@Override
	protected String getTitlePrefix() {
		return String.format("Sensory Integration Experiment - Build %s", SIExperiment.getBuildVersion());
	}
}
