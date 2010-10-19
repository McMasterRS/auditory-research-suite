package edu.mcmaster.maplelab.common.sound;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * Midi note, event, and message interpreter.
 */
public class MidiInterpreter {
	public static final int OPCODE_MASK = 0xf0;
	private static final int CHANNEL_MASK = 0x0f;

	/**
	 * Indicate if the given event is a note on event.
	 */
	public static boolean isNoteOnEvent(MidiEvent event) {
		MidiMessage m = event.getMessage();
		return !(getOpcode(m) != ShortMessage.NOTE_ON || getVelocity(m) == 0);
	}

	/**
	 * Indicate if the given event is a note off event.
	 */
	public static boolean isNoteOffEvent(MidiEvent event) {
		MidiMessage m = event.getMessage();
		return (getOpcode(m) == ShortMessage.NOTE_ON && 
				getVelocity(m) == 0) || getOpcode(m) == ShortMessage.NOTE_OFF;
	}

	/**
	 * Get the channel from the given message.
	 */
	public static int getChannel(MidiMessage m) {
		return m.getStatus() & CHANNEL_MASK;
	}

	/**
	 * Get the opcode from the given message.
	 */
	public static int getOpcode(MidiMessage m) {
		return m.getStatus() & OPCODE_MASK;
	}

	/**
	 * Get the key property from the given message.  Returns null
	 * if not present.
	 */
	public static Integer getKey(MidiMessage message) {
		return message.getLength() > 1 ? Integer.valueOf(message.getMessage()[1]) : null;
	}

	/**
	 * Get the velocity property from the given message.  Returns null
	 * if not present.
	 */
	public static Integer getVelocity(MidiMessage message) {
		return message.getLength() > 2 ? Integer.valueOf(message.getMessage()[2]) : null;
	}

	

}
