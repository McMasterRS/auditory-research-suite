package edu.mcmaster.maplelab.midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * This class provides the means for storing multiple midi events in a single event
 * that can be passed through the midi system via the standard API.  This is useful
 * when events are modified to suit application specifications but we still need
 * access to the original source event down the line.
 * 
 * @author bguseman
 */
public class MultiMidiEvent extends MidiEvent {
	
	public static MultiMidiEvent getMultiEvent(MidiEvent e) {
		if (e instanceof MultiMidiEvent) return (MultiMidiEvent) e;
		else return new MultiMidiEvent(e, null, e.getMessage(), e.getTick());
	}
	
	private final MidiEvent _sourceEvent;
	private final MidiEvent _modifiedSource;

	public MultiMidiEvent(MidiEvent sourceEvent, MidiEvent modifiedSource, 
			MidiMessage message, long tick) {
		super(message, tick);
		_sourceEvent = sourceEvent;
		_modifiedSource = modifiedSource;
	}
	
	public boolean hasModifiedSource() {
		return _modifiedSource != null;
	}
	
	public boolean isMultiEvent() {
		return _sourceEvent != null;
	}
	
	public MidiEvent getSourceEvent() {
		return _sourceEvent;
	}
	
	public MidiEvent getModifiedSourceEvent() {
		return _modifiedSource;
	}

}
