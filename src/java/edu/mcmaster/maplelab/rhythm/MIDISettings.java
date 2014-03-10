package edu.mcmaster.maplelab.rhythm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.sound.midi.MidiSystem;

/**
 * Data object to encapsulate device IDs and soundbank filenames.
 * @author zachbrown
 *
 */
public class MIDISettings {
	public static final int VALID_DEV_COUNT = MidiSystem.getMidiDeviceInfo().length;
	
	private int _toneSynthesizerID;
	private int _tapInputID;
	private int _tapSynthesizerID;
	private String _soundbankFilename;
	private boolean _isDefaultSoundbank;
	
	private final PropertyChangeSupport _PCS = new PropertyChangeSupport(this);
	
	/**
	 * default ctor
	 */
	public MIDISettings() {
		_toneSynthesizerID = 0;
		_tapInputID = -1;
		_tapSynthesizerID = -1;
		_soundbankFilename = "";
		_isDefaultSoundbank = true;
	}
	
	public int getToneSynthesizerID() {
		return _toneSynthesizerID;
	}
	
	public void setToneSynthesizerID(int toneSynthesizerID) {
		if (toneSynthesizerID >= 0 && toneSynthesizerID < VALID_DEV_COUNT) {
			_toneSynthesizerID = toneSynthesizerID;
		}
	}
	
	public int getTapInputID() {
		return _tapInputID;
	}
	
	public void setTapInputID(int tapInputID) {
		if (tapInputID >= -1 && tapInputID < VALID_DEV_COUNT) {
			_tapInputID = tapInputID;
		}
	}
	
	public int getTapSynthesizerID() {
		return _tapSynthesizerID;
	}
	
	public void setTapSynthesizerID(int tapSynthesizerID) {
		if (tapSynthesizerID >= -1 && tapSynthesizerID < VALID_DEV_COUNT) {
			_tapSynthesizerID = tapSynthesizerID;
		}
	}
	
	public String getSoundbankFilename() {
		return _soundbankFilename;
	}
	
	public void setSoundbankFilename(String filename) {
		_soundbankFilename = filename;
	}
	
	public boolean isUsingDefaultSoundBank() {
		return _isDefaultSoundbank;
	}
	
	public void setUsingDefaultSoundBank(boolean isDefaultSoundbank) {
		_isDefaultSoundbank = isDefaultSoundbank;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		_PCS.addPropertyChangeListener(pcl);
	}
	
	public void copy(MIDISettings other) {
		_toneSynthesizerID = other.getToneSynthesizerID();
		_tapInputID = other.getTapInputID();
		_tapSynthesizerID = other.getTapSynthesizerID();
		_soundbankFilename = other.getSoundbankFilename();
		_isDefaultSoundbank = other.isUsingDefaultSoundBank();
		
		_PCS.firePropertyChange("copy", this, other);
	}
	
	@Override
	public String toString() {
		return "[" + _toneSynthesizerID + ","
				+ _tapInputID + "," + _tapSynthesizerID
				+ "," + _soundbankFilename 
				+ "," + _isDefaultSoundbank + "]";
	}
	
}
