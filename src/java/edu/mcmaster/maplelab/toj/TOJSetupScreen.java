/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import java.io.IOException;
import java.util.prefs.Preferences;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;

/**
 * A TOJ specific implementation of SimpleSetupScreen.
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class TOJSetupScreen extends SimpleSetupScreen<TOJSession> {
	
	public TOJSetupScreen() {
		super(TOJExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), true, false);
	}

	@Override
	protected void initializeBuildInfo() throws IOException {
		Experiment.initializeBuildInfo(TOJExperiment.class, getPrefsPrefix());
	}

	@Override
	protected void addExperimentFields() {
	}

	@Override
	protected void applyExperimentSettings(TOJSession session) {
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
	}

	@Override
	protected String getTitlePrefix() {
		return String.format("TOJ Experiment - Build %s", TOJExperiment.getBuildVersion());
	}
}
