package edu.mcmaster.maplelab.rhythm;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession;

/**
 * Rhythm-specific extension of the setup screen.
 * 
 * @author bguseman
 *
 */
public class RhythmSetupScreen extends SimpleSetupScreen<RhythmSession> {
	private JFormattedTextField _midiDev;

	public RhythmSetupScreen() {
		super(RhythmExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), false, true);
		try {
			RhythmExperiment.initializeBuildInfo(null);
		}
		catch (IOException e) { }
	}

	@Override
	protected void addExperimentFields() {
		addLabel("<html><p align=right>Tap Source (MIDI <br>Device #):");
		_midiDev = new JFormattedTextField();
		_midiDev.setColumns(12);
		_midiDev.setValue(new Integer(0));
		addField(_midiDev);    
		
		// provide access to test utility
		JButton midiTest = new JButton("List and Test Devices");
		midiTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				JDialog dialog = MIDITestPanel.createDialog(source);
				dialog.setVisible(true);
			}
		});

		addLabel("MIDI System:");
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(midiTest);
		addField(p);
	}

	@Override
	protected void applyExperimentSettings(RhythmSession session) {
		session.setMIDIInputDeviceID(Integer.parseInt(_midiDev.getText()));
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		int midiDevID = Integer.parseInt(_midiDev.getText());
		prefs.putInt(RhythmSession.ConfigKeys.midiDevID.name(), midiDevID);
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		int midiDevID = prefs.getInt(RhythmSession.ConfigKeys.midiDevID.name(), 0);
		_midiDev.setValue(new Integer(midiDevID));
	}

	@Override
	protected String getTitlePrefix() {
		return String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion());
	}

}
