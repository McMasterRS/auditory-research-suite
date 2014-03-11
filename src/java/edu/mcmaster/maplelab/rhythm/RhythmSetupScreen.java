package edu.mcmaster.maplelab.rhythm;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession;

/**
 * Rhythm-specific extension of the setup screen.
 * 
 * @author bguseman
 *
 */
public class RhythmSetupScreen extends SimpleSetupScreen<RhythmSession> {
	private MIDISettings _primaryMIDISettings;

	public RhythmSetupScreen() {
		super(RhythmExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), false, true);
	}

	@Override
	protected void initializeBuildInfo() throws IOException {
		Experiment.initializeBuildInfo(RhythmExperiment.class, getPrefsPrefix());
	}

	@Override
	protected void addExperimentFields() {
		_primaryMIDISettings = new MIDISettings();
		
		MIDISettingsEditor editor = new MIDISettingsEditor(_primaryMIDISettings);
		JPanel p = new JPanel(new MigLayout("fill, insets 0", "0px[grow]0px", "10px[grow]10px"));
		p.add(editor, "grow");
		addPanel(p);
		
		// provide access to test utility
		JButton midiTest = new JButton("List and Test Devices");
		midiTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				JDialog dialog = MIDITestPanel.createDialog(source, _primaryMIDISettings);
				dialog.setVisible(true);
			}
		});

		addLabel("MIDI System:");
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(midiTest);
		addField(p);
	}

	@Override
	protected void applyExperimentSettings(RhythmSession session) {
		session.setSynthDevID(_primaryMIDISettings.getToneSynthesizerID());
		session.setTapInputDevID(_primaryMIDISettings.getTapInputID());
		session.setTapSynthDevID(_primaryMIDISettings.getTapSynthesizerID());
		session.setSoundbankLocation(_primaryMIDISettings.getSoundbankFilename());
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		prefs.putInt(RhythmSession.ConfigKeys.toneSynthID.name(), _primaryMIDISettings.getToneSynthesizerID());
		prefs.putInt(RhythmSession.ConfigKeys.tapInputDevID.name(), _primaryMIDISettings.getTapInputID());
		prefs.putInt(RhythmSession.ConfigKeys.tapSynthID.name(), _primaryMIDISettings.getTapSynthesizerID());
		prefs.put(RhythmSession.ConfigKeys.soundbankLoc.name(), _primaryMIDISettings.getSoundbankFilename());
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		MIDISettings loaded = new MIDISettings();
		loaded.setToneSynthesizerID(prefs.getInt(RhythmSession.ConfigKeys.toneSynthID.name(), 0));
		loaded.setTapInputID(prefs.getInt(RhythmSession.ConfigKeys.tapInputDevID.name(), -1));
		loaded.setTapSynthesizerID(prefs.getInt(RhythmSession.ConfigKeys.tapSynthID.name(), -1));
		String savedSBFile = prefs.get(RhythmSession.ConfigKeys.soundbankLoc.name(), "");
		if (!savedSBFile.equals("")) {
			loaded.setSoundbankFilename(savedSBFile);
			loaded.setUsingDefaultSoundBank(false);
		}
		_primaryMIDISettings.copy(loaded);
	}

	@Override
	protected String getTitlePrefix() {
		return String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion());
	}
}
