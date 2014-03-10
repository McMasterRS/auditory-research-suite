package edu.mcmaster.maplelab.rhythm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.gui.FileBrowseField;

/**
 * Widget for selection of midi devices and soundbank.
 */
public class MIDISettingsEditor extends JPanel {
    private static final String DEFAULTSOUNDBANK = "Using default internal soundbank.";
	
    /** GUI components. */
	private final JComboBox _toneSynth;
	private final JComboBox _tapInput;
	private final JComboBox _tapSynth;
	private final JRadioButton _useDefaultSB;
	private final JRadioButton _useSpecifiedSB;
    private final FileBrowseField _soundbankChooser;
    
    /** Action listener. */
    private final ComponentListener _cListener;
    
    /** Data object to edit. */
	private final MIDISettings _settings;
	
	/** Data listener. */
	private final DataListener _dListener;
	
	/**
	 * Constructor.
	 */
	public MIDISettingsEditor(MIDISettings settings) {
		super(new MigLayout("insets 0 0 0 0", "[right]10px[center]10px[center]10px[center]10px[push]", "0px[]3px[]8px[]0px[]0px"));

		_settings = settings;
		_dListener = new DataListener();
		_settings.addPropertyChangeListener(_dListener);
		
		setBorder(BorderFactory.createTitledBorder("MIDI Devices"));
		add(new JLabel("<html><div style=\"text-align: center;\">Tone Synth<br>(Must have RECEIVE)"), "skip 1");
		add(new JLabel("<html><div style=\"text-align: center;\">Tap Source<br>(Must have TRANS)"));
		add(new JLabel("<html><div style=\"text-align: center;\">Tap Synth<br>(Must have RECEIVE)"), "wrap");
		add(new JLabel("Device #:"));
		
		_toneSynth = new JComboBox();
		_tapInput = new JComboBox();
		_tapSynth = new JComboBox();
		
		_cListener = new ComponentListener();
		
		initDeviceCount(MIDISettings.VALID_DEV_COUNT);
		
		add(_toneSynth, "sgx");
		add(_tapInput, "sgx");
		add(_tapSynth, "sgx, wrap");
		
		_useDefaultSB = new JRadioButton("Use default soundbank", true);
		_useDefaultSB.addActionListener(_cListener);
		add(_useDefaultSB, "skip 1");
		
		_useSpecifiedSB = new JRadioButton("Use Specified Soundbank", false);
		_useSpecifiedSB.addActionListener(_cListener);
		add(_useSpecifiedSB, "wrap");
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(_useDefaultSB);
		bg.add(_useSpecifiedSB);
		
		_soundbankChooser = new FileBrowseField(false, "sf2", "dls");
		_soundbankChooser.setToolTipText("Select .sf2 or .dls soundbank file to load.");
		_soundbankChooser.addFileChoiceChangeListener(_cListener);
		_soundbankChooser.setEnabled(false);
		add(new JLabel("Soundbank file:"));
		add(_soundbankChooser, "growx, span");
		
		updateDisplay();
	}
	
	private void initDeviceCount(int count) {
		String[] toneSynthItems = new String[count];
		String[] tapInputItems = new String[count + 1];
		tapInputItems[0] = "None (Comp. Keys)";
		String[] tapSynthItems = new String[count + 1];
		tapSynthItems[0] = "None";
		
		for (int i = 0; i < count; i++) {
			toneSynthItems[i] = String.valueOf(i);
			tapInputItems[i + 1] = String.valueOf(i);
			tapSynthItems[i + 1] = String.valueOf(i);
		}
		
		_toneSynth.setModel(new DefaultComboBoxModel(toneSynthItems));
		_toneSynth.addActionListener(_cListener);
		_tapInput.setModel(new DefaultComboBoxModel(tapInputItems));
		_tapInput.addActionListener(_cListener);
		_tapSynth.setModel(new DefaultComboBoxModel(tapSynthItems));
		_tapSynth.addActionListener(_cListener);
	}
	
	public void updateDisplay() {
		_cListener.setEnabled(false);
		
		int toneSynth = _settings.getToneSynthesizerID();
		int tapInput = _settings.getTapInputID();
		int tapSynth = _settings.getTapSynthesizerID();
		
		if (toneSynth < _toneSynth.getItemCount()) {
			_toneSynth.setSelectedIndex(toneSynth);
		}
		if (tapInput < _tapInput.getItemCount()) {
			_tapInput.setSelectedIndex(tapInput + 1);
		}
		if (tapSynth < _tapSynth.getItemCount()) {
			_tapSynth.setSelectedIndex(tapSynth + 1);
		}
		if (!_settings.isUsingDefaultSoundBank()) {
			_soundbankChooser.setText(_settings.getSoundbankFilename());
			_useSpecifiedSB.setSelected(true);
		} else { // then _useDefault is selected
			_soundbankChooser.setText(DEFAULTSOUNDBANK);
			_useDefaultSB.setSelected(true);
		}

		updateEnabledState();
		_cListener.setEnabled(true);
	}
	
	private void updateEnabledState() {
		_soundbankChooser.setEnabled(!_settings.isUsingDefaultSoundBank());
	}
	
	public int getToneSynthIndex() { return _settings.getToneSynthesizerID(); }
	public int getTapInputIndex() { return _settings.getTapInputID(); }
	public int getTapSynthIndex() { return _settings.getTapSynthesizerID(); }
	public String getSoundbankFilename() { return _settings.getSoundbankFilename(); }

	
	private void applySynthSelections() {
		_settings.setToneSynthesizerID(_toneSynth.getSelectedIndex());
		// Subtract one because combobox has extra initial option for "None"
		_settings.setTapInputID(_tapInput.getSelectedIndex() - 1);
		// Subtract one because combobox has extra initial option for "None"
		_settings.setTapSynthesizerID(_tapSynth.getSelectedIndex() - 1);
	}
	
	/**
	 * Listener for data changes.
	 */
	private class DataListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			updateDisplay();
		}
	}
	
	/**
	 * Handler for GUI component actions.
	 */
	private class ComponentListener implements ActionListener, PropertyChangeListener {
		private boolean _enabled = true;
		
		public void setEnabled(boolean enabled) {
			_enabled = enabled;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!_enabled) {
				return;
			}
			
			if (_settings.isUsingDefaultSoundBank() != _useDefaultSB.isSelected()) {
				_settings.setSoundbankFilename("");
				if (_useDefaultSB.isSelected()) {
					_settings.setUsingDefaultSoundBank(true);
				} 
				else {
					_settings.setUsingDefaultSoundBank(false);
				}
			}
			else {
				applySynthSelections();
			}
			
			updateDisplay();
		}

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			File f = _soundbankChooser.getFile();
			_settings.setSoundbankFilename(f != null ? f.getAbsolutePath() : "");
			
			updateDisplay();
		}
	}
	
}
