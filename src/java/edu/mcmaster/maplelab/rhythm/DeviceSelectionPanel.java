package edu.mcmaster.maplelab.rhythm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.midi.MidiSystem;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.mcmaster.maplelab.common.gui.FileBrowseField;

import net.miginfocom.swing.MigLayout;

/**
 * Widget for selection of midi devices and soundbank.
 */
public class DeviceSelectionPanel extends JPanel implements ActionListener, PropertyChangeListener {
	
	private JComboBox _toneSynth;
	private JComboBox _tapInput;
	private JComboBox _tapSynth;
	
	private JRadioButton _useDefaultSoundbankRButton;
	private JRadioButton _useSpecifiedSoundbankRButton;
    private FileBrowseField _soundbankChooser;
    private static final String DEFAULTSOUNDBANK = "Using default internal soundbank.";

	private MIDISettings _settings;
	
	
	public DeviceSelectionPanel(MIDISettings settings) {
		super(new MigLayout("insets 0 0 0 0", "[right]10px[center]10px[center]10px[center]10px[push]", "0px[]3px[]8px[]0px[]0px"));
		setBorder(BorderFactory.createTitledBorder("MIDI Devices"));
		add(new JLabel("<html><div style=\"text-align: center;\">Tone Synth<br>(Must have RECEIVE)"), "skip 1");
		add(new JLabel("<html><div style=\"text-align: center;\">Tap Source<br>(Must have TRANS)"));
		add(new JLabel("<html><div style=\"text-align: center;\">Tap Synth<br>(Must have RECEIVE)"), "wrap");
		add(new JLabel("Device #:"));
		
		_toneSynth = new JComboBox();
		_tapInput = new JComboBox();
		_tapSynth = new JComboBox();
		_settings = settings;
		
		initDeviceCount(MidiSystem.getMidiDeviceInfo().length);
		
		add(_toneSynth, "sgx");
		add(_tapInput, "sgx");
		add(_tapSynth, "sgx, wrap");
		
		_useDefaultSoundbankRButton = new JRadioButton("Use default soundbank", _settings.getIsDefaultSoundBank());
		_useDefaultSoundbankRButton.addActionListener(this);
		add(_useDefaultSoundbankRButton, "skip 1");
		
		_useSpecifiedSoundbankRButton = new JRadioButton("Use Specified Soundbank", !_settings.getIsDefaultSoundBank());
		_useSpecifiedSoundbankRButton.addActionListener(this);
		add(_useSpecifiedSoundbankRButton, "wrap");
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(_useDefaultSoundbankRButton);
		bg.add(_useSpecifiedSoundbankRButton);
		
		_soundbankChooser = new FileBrowseField(false, "sf2", "dls");
		_soundbankChooser.setToolTipText("Select .sf2 or .dls soundbank file to load.");
		_soundbankChooser.addFileChoiceChangeListener(this);
		_soundbankChooser.setText(_settings.getSoundbankFilename());
		_soundbankChooser.setEnabled(false);
		add(new JLabel("Soundbank file:"));
		add(_soundbankChooser, "growx, span");
		
		updateSelections(settings);
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
		_toneSynth.addActionListener(this);
		_tapInput.setModel(new DefaultComboBoxModel(tapInputItems));
		_tapInput.addActionListener(this);
		_tapSynth.setModel(new DefaultComboBoxModel(tapSynthItems));
		_tapSynth.addActionListener(this);
	}
	
	public void updateSelections(MIDISettings settings) {
		updateSelections(
				settings.getToneSynthesizerID(), 
				settings.getTapInputID(), 
				settings.getTapSynthesizerID(),
				settings.getSoundbankFilename(),
				settings.getIsDefaultSoundBank());
	}
	
	private void updateSelections(int toneSynth, int tapInput, int tapSynth, String sbFilename, boolean isDefaultSB) {
		if (toneSynth < _toneSynth.getItemCount()) {
			_toneSynth.setSelectedIndex(toneSynth);
			_settings.setToneSynthesizerID(toneSynth);
		}
		if (tapInput < _tapInput.getItemCount()) {
			_tapInput.setSelectedIndex(tapInput + 1);
			_settings.setTapInputID(tapInput);
		}
		if (tapSynth < _tapSynth.getItemCount()) {
			_tapSynth.setSelectedIndex(tapSynth + 1);
			_settings.setTapSynthesizerID(tapSynth);
		}
		if (sbFilename != null) {
			if (!isDefaultSB) {
				_soundbankChooser.setText(sbFilename);
				_soundbankChooser.setEnabled(true);
				_useSpecifiedSoundbankRButton.setSelected(true);
				_settings.setSoundbankFilename(sbFilename);
				_settings.setIsDefaultSoundBank(false);
			} else { // then _useDefault is selected
				_soundbankChooser.setText(DEFAULTSOUNDBANK);
				_soundbankChooser.setEnabled(false);
				_useDefaultSoundbankRButton.setSelected(true);
				_settings.setSoundbankFilename("");
				_settings.setIsDefaultSoundBank(true);
			}
			
		}
	}
	
	public int getToneSynthIndex() { return _settings.getToneSynthesizerID(); }
	public int getTapInputIndex() { return _settings.getTapInputID(); }
	public int getTapSynthIndex() { return _settings.getTapSynthesizerID(); }
	public String getSoundbankFilename() { return _settings.getSoundbankFilename(); }

	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Do combobox checks
		if (_settings != null) {
			if (e.getSource().equals(_toneSynth)) {
				_settings.setToneSynthesizerID(_toneSynth.getSelectedIndex());
			} else if (e.getSource().equals(_tapInput)) {
				// Subtract one because combobox has extra initial option for "None"
				_settings.setTapInputID(_tapInput.getSelectedIndex() - 1);
			} else if (e.getSource().equals(_tapSynth)) {
				// Subtract one because combobox has extra initial option for "None"
				_settings.setTapSynthesizerID(_tapSynth.getSelectedIndex() - 1);
			}
		}
		
		// Do radio button checks
		if (e.getSource().equals(_useDefaultSoundbankRButton)) {
			_soundbankChooser.setEnabled(false);
			_soundbankChooser.setText(DEFAULTSOUNDBANK);
			_settings.setIsDefaultSoundBank(true);
		} else if (e.getSource().equals(_useSpecifiedSoundbankRButton)) {
			_soundbankChooser.setEnabled(true);
			_soundbankChooser.setText("");
			_settings.setIsDefaultSoundBank(false);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		_settings.setSoundbankFilename(_soundbankChooser.getFile().getAbsolutePath());
		if (_useDefaultSoundbankRButton.isSelected()) {
			_settings.setIsDefaultSoundBank(true);
		}
		else if (_useSpecifiedSoundbankRButton.isSelected()) {
			_settings.setIsDefaultSoundBank(false);
		}
	}
	
}
