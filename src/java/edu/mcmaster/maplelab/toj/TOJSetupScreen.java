package edu.mcmaster.maplelab.toj;

import java.util.prefs.Preferences;

import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;

import javax.swing.*;


public class TOJSetupScreen extends SimpleSetupScreen<TOJSession> {
	/** Experiment basename - placed here temporarily */
	public static final String EXPERIMENT_BASENAME = "Temporal Order Judgment";

	
	// new text fields
	private JFormattedTextField _screenWidth;
	private JFormattedTextField _screenHeight;
	private JFormattedTextField _dataFileName;
	private JCheckBox _includeVideoStimuli;
	
	public TOJSetupScreen(String prefsPrefix) {
		super(EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), true);
	}

	@Override
	protected void addExperimentFields() {
		// where on screen do they go?
		
		addLabel("Screen width");
		_screenWidth = new JFormattedTextField();
		_screenWidth.setValue(720);
		addField(_screenWidth);
		
		addLabel("Screen height");
		_screenHeight = new JFormattedTextField();
		_screenHeight.setValue(480);
		addField(_screenHeight);
		
		addLabel("Data file name");
		_dataFileName = new JFormattedTextField();
		_dataFileName.setValue("TemporalOrderJudgmentTrialData.txt");
		addField(_dataFileName);
		
		addLabel("Include video stimuli");
		_includeVideoStimuli = new JCheckBox();
		addField(_includeVideoStimuli);		
	}

	@Override
	protected void applyExperimentSettings(TOJSession session) {
		session.setScreenWidth(Integer.parseInt(_screenWidth.getText()));
		session.setScreenHeight(Integer.parseInt(_screenHeight.getText()));
		session.setDataFileName(_dataFileName.getText());
		session.includeVisualStimuli(_includeVideoStimuli.isSelected());
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		int prefScreenWidth = Integer.parseInt(_screenWidth.getText());
		prefs.putInt(TOJSession.ConfigKeys.screenWidth.name(), prefScreenWidth);
		
		int prefScreenHeight = Integer.parseInt(_screenHeight.getText());
		prefs.putInt(TOJSession.ConfigKeys.screenHeight.name(), prefScreenHeight);
		
		String prefDataFileName = _dataFileName.getText();
		prefs.put(TOJSession.ConfigKeys.dataFileName.name(), prefDataFileName);
		
		boolean prefIncludeVideoSimuli = _includeVideoStimuli.isSelected();
		prefs.putBoolean(TOJSession.ConfigKeys.includeVisualStimuli.name(), prefIncludeVideoSimuli);
		
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		int prefScreenWidth = prefs.getInt(TOJSession.ConfigKeys.screenWidth.name(), 720);
		_screenWidth.setValue(new Integer(prefScreenWidth));
		
		int prefScreenHeight = prefs.getInt(TOJSession.ConfigKeys.screenHeight.name(), 480);
		_screenHeight.setValue(new Integer(prefScreenHeight));
		
		String prefDataFileName = prefs.get(TOJSession.ConfigKeys.dataFileName.name(), "TemporalOrderJudgmentTrialData.txt");
		_dataFileName.setValue(prefDataFileName);
		
		boolean prefIncludeVideoStimuli = prefs.getBoolean(TOJSession.ConfigKeys.includeVisualStimuli.name(), true);
		_includeVideoStimuli.setSelected(new Boolean(prefIncludeVideoStimuli));
	}

}
