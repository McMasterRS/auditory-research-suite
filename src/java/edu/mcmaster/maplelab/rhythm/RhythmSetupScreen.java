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
	private DeviceSelectionPanel _devs;

	public RhythmSetupScreen() {
		super(RhythmExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), false, true);
	}

	@Override
	protected void initializeBuildInfo() throws IOException {
		Experiment.initializeBuildInfo(RhythmExperiment.class, getPrefsPrefix());
	}

	@Override
	protected void addExperimentFields() {
		_devs = new DeviceSelectionPanel();
		JPanel p = new JPanel(new MigLayout("fill, insets 0", "0px[grow]0px", "10px[grow]10px"));
		p.add(_devs, "grow");
		addPanel(p);
		
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
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(midiTest);
		addField(p);
	}

	@Override
	protected void applyExperimentSettings(RhythmSession session) {
		session.setSynthDevID(_devs.getToneSynthIndex());
		session.setTapInputDevID(_devs.getTapInputIndex() - 1);
		session.setTapSynthDevID(_devs.getTapSynthIndex() - 1);
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		prefs.putInt(RhythmSession.ConfigKeys.toneSynthID.name(), _devs.getToneSynthIndex());
		prefs.putInt(RhythmSession.ConfigKeys.tapInputDevID.name(), _devs.getTapInputIndex());
		prefs.putInt(RhythmSession.ConfigKeys.tapSynthID.name(), _devs.getTapSynthIndex());
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		_devs.updateSelections(prefs.getInt(RhythmSession.ConfigKeys.toneSynthID.name(), 0), 
				prefs.getInt(RhythmSession.ConfigKeys.tapInputDevID.name(), 0), 
				prefs.getInt(RhythmSession.ConfigKeys.tapSynthID.name(), 0));
	}

	@Override
	protected String getTitlePrefix() {
		return String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion());
	}

}
