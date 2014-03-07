package edu.mcmaster.maplelab.rhythm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Data object to encapsulate device IDs and soundbank filenames.
 * @author zachbrown
 *
 */
public class MIDISettings {
	
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
		_toneSynthesizerID = toneSynthesizerID;
	}
	
	public int getTapInputID() {
		return _tapInputID;
	}
	
	public void setTapInputID(int tapInputID) {
		_tapInputID = tapInputID;
	}
	
	public int getTapSynthesizerID() {
		return _tapSynthesizerID;
	}
	
	public void setTapSynthesizerID(int tapSynthesizerID) {
		_tapSynthesizerID = tapSynthesizerID;
	}
	
	public String getSoundbankFilename() {
		return _soundbankFilename;
	}
	
	public void setSoundbankFilename(String filename) {
		_soundbankFilename = filename;
	}
	
	public boolean getIsDefaultSoundBank() {
		return _isDefaultSoundbank;
	}
	
	public void setIsDefaultSoundBank(boolean setDefault) {
		_isDefaultSoundbank = setDefault;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		_PCS.addPropertyChangeListener(pcl);
	}
	
	public void copy(MIDISettings other) {
		_toneSynthesizerID = other.getToneSynthesizerID();
		_tapInputID = other.getTapInputID();
		_tapSynthesizerID = other.getTapSynthesizerID();
		_soundbankFilename = other.getSoundbankFilename();
		_isDefaultSoundbank = other.getIsDefaultSoundBank();
		
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
