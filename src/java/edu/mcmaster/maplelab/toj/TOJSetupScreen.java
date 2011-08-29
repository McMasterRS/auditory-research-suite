/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import java.util.prefs.Preferences;

import javax.swing.JFormattedTextField;

import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;

/**
 * A TOJ specific implementation of SimpleSetupScreen.
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class TOJSetupScreen extends SimpleSetupScreen<TOJSession> {
	// new text fields
	private JFormattedTextField _screenWidth;
	private JFormattedTextField _screenHeight;
	//private JFormattedTextField _dataFileName;
	
	public TOJSetupScreen() {
		super(TOJExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), true);
	}

	@Override
	protected void addExperimentFields() {
		addLabel("Screen width");
		_screenWidth = new JFormattedTextField();
		_screenWidth.setValue(720);
		addField(_screenWidth);
		
		addLabel("Screen height");
		_screenHeight = new JFormattedTextField();
		_screenHeight.setValue(480);
		addField(_screenHeight);
		
		/*addLabel("Data file name");
		_dataFileName = new JFormattedTextField();
		_dataFileName.setValue("TemporalOrderJudgmentTrialData.txt");
		addField(_dataFileName);*/
	}

	@Override
	protected void applyExperimentSettings(TOJSession session) {
		session.setScreenWidth(Integer.parseInt(_screenWidth.getText()));
		session.setScreenHeight(Integer.parseInt(_screenHeight.getText()));
		/*session.setDataFileName(_dataFileName.getText());*/
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		int prefScreenWidth = Integer.parseInt(_screenWidth.getText());
		prefs.putInt(TOJSession.ConfigKeys.screenWidth.name(), prefScreenWidth);
		
		int prefScreenHeight = Integer.parseInt(_screenHeight.getText());
		prefs.putInt(TOJSession.ConfigKeys.screenHeight.name(), prefScreenHeight);
		
		/*String prefDataFileName = _dataFileName.getText();
		prefs.put(TOJSession.ConfigKeys.dataFileName.name(), prefDataFileName);*/
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		int prefScreenWidth = prefs.getInt(TOJSession.ConfigKeys.screenWidth.name(), 720);
		_screenWidth.setValue(new Integer(prefScreenWidth));
		
		int prefScreenHeight = prefs.getInt(TOJSession.ConfigKeys.screenHeight.name(), 480);
		_screenHeight.setValue(new Integer(prefScreenHeight));
		
		/*String prefDataFileName = prefs.get(TOJSession.ConfigKeys.dataFileName.name(), "TemporalOrderJudgmentTrialData.txt");
		_dataFileName.setValue(prefDataFileName);*/
	}
}
