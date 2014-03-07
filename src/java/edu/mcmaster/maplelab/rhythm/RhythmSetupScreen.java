package edu.mcmaster.maplelab.rhythm;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class RhythmSetupScreen extends SimpleSetupScreen<RhythmSession> implements PropertyChangeListener {
	private DeviceSelectionPanel _devs;
	
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
		_primaryMIDISettings.addPropertyChangeListener(this);
		
		_devs = new DeviceSelectionPanel(_primaryMIDISettings);
		JPanel p = new JPanel(new MigLayout("fill, insets 0", "0px[grow]0px", "10px[grow]10px"));
		p.add(_devs, "grow");
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
		session.setSynthDevID(_devs.getToneSynthIndex());
		session.setTapInputDevID(_devs.getTapInputIndex());
		session.setTapSynthDevID(_devs.getTapSynthIndex());
		session.setSoundbankFilename(_devs.getSoundbankFilename());
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		prefs.putInt(RhythmSession.ConfigKeys.toneSynthID.name(), _devs.getToneSynthIndex());
		prefs.putInt(RhythmSession.ConfigKeys.tapInputDevID.name(), _devs.getTapInputIndex());
		prefs.putInt(RhythmSession.ConfigKeys.tapSynthID.name(), _devs.getTapSynthIndex());
		prefs.put(RhythmSession.ConfigKeys.soundbankFilename.name(), _devs.getSoundbankFilename());
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		MIDISettings loaded = new MIDISettings();
		loaded.setToneSynthesizerID(prefs.getInt(RhythmSession.ConfigKeys.toneSynthID.name(), 0));
		loaded.setTapInputID(prefs.getInt(RhythmSession.ConfigKeys.tapInputDevID.name(), -1));
		loaded.setTapSynthesizerID(prefs.getInt(RhythmSession.ConfigKeys.tapSynthID.name(), -1));
		loaded.setSoundbankFilename(prefs.get(RhythmSession.ConfigKeys.soundbankFilename.name(), ""));
		_devs.updateSelections(loaded);
	}

	@Override
	protected String getTitlePrefix() {
		return String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("copy")) {
			// If the copy event occurred, then the primary settings should have been updated.
			_devs.updateSelections(_primaryMIDISettings);
		}
	}

}
