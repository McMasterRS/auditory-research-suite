package edu.mcmaster.maplelab.toj;

import java.util.prefs.Preferences;

import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;

public class TOJSetupScreen extends SimpleSetupScreen<TOJSession> {
	/** Experiment basename - placed here temporarily */
	public static final String EXPERIMENT_BASENAME = "Temporal Order Judgment";

	public TOJSetupScreen(String prefsPrefix) {
		super(EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), true);
	}

	@Override
	protected void addExperimentFields() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void applyExperimentSettings(TOJSession session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		// TODO Auto-generated method stub
		
	}

}
