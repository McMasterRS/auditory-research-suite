/*
* Copyright (C) 2006 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/
package edu.mcmaster.maplelab.common.sound;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Encapsulation of a pitch stimulus.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Apr 17, 2006
 */
public class Pitch implements Comparable<Pitch> {
    private final NotesEnum _note;
    /**
     * @uml.property  name="octave"
     */
    private final int _octave;
    /**
     * @uml.property  name="detuneCents"
     */
    private final int _detuneCents;

    private static final double RT12_2 = Math.pow(2, 1.0/12.0);
    private static final Pitch REF_PITCH = new Pitch(NotesEnum.A, 4);
    private static final double REF_FREQ = 440.0;
    private static final Pattern P_PAT = Pattern.compile("([a-gA-G][#sS]?)(-?\\d+)([+-]\\d+)?");
    
    // Default value of Middle C is C4, so _middleCOctave is 4
    private static int _middleCOctave = 4;
    
    /**
     * Ctor
     * 
     * @param note Note value
     * @param octave octave [-1, 9] (assuming Middle C is C4)
     */
    public Pitch(NotesEnum note, int octave) {
        this(note, octave, 0);
    }
    
    /**
     * Ctor
     * 
     * @param note note value
     * @param octave octave [-1, 9] (assuming Middle C is C4)
     * @param detuneCents detune amount in cents (1/100th semitone) [-50, 50]
     */
    public Pitch(NotesEnum note, int octave, int detuneCents) {
        _note = note;
        _octave = octave;
        _detuneCents = detuneCents;
        
        if(_note == null) {
            throw new NullPointerException("Note value may not be null.");
        }
        if(_octave < _middleCOctave-5 || _octave > _middleCOctave+5) {
            throw new IllegalArgumentException("Octave must be in the range [" + (_middleCOctave-5) 
            		+ "," + (_middleCOctave+5) + "].");
        }
        if(_detuneCents < -50 || _detuneCents > 50) {
            throw new IllegalArgumentException("Detune must be in the range [-50,50].");
        }
    }
    
    /**
     * Create a pitch value from the given midi note number
     * @param midiNote [0, 127]
     */
    public Pitch(int midiNote) {
        this(midiNote, 0);
    }
    
    /**
     * Create a pitch value from the given midi note number
     * @param midiNote [0, 127]
     * @param detuneCents detune amount.
     */
    public Pitch(int midiNote, int detuneCents) {    
        if(midiNote < 0 || midiNote > 127) {
            throw new IllegalArgumentException("Midi note value must be in range [0, 127]");
        }

    	int middleCOffset = 5 - _middleCOctave;
        _octave = (midiNote / 12) - middleCOffset;
        _note = NotesEnum.values()[midiNote % 12];
        _detuneCents = detuneCents;
    }
    
    /**
     * Create a pitch from a fractional mini note number
     * 
     * @param fractionalMidiNote
     */
    public Pitch(float fractionalMidiNote) {
        this(Math.round(fractionalMidiNote), extractDetune(fractionalMidiNote));
    }

    public static void setMiddleCOctave(int midCOctave) {
    	_middleCOctave = midCOctave;
    }
    
    /**
     * Get note value.
     * @uml.property  name="note"
     */
    public NotesEnum getNote() {
        return _note;
    }
    
    /**
     * Get octave value in range middle C octave +- 5
     * If middle C is C4, then this range is [-1,9]
     * @uml.property  name="octave"
     */
    public int getOctave() {
        return _octave;
    }
    
    /**
     * Tone deviation from standard A=440Hz chromatic scale.
     * @uml.property  name="detuneCents"
     */
    public int  getDetuneCents() {
        return _detuneCents;
    }    
    
    /**
     * Extract the detune amount in cents from the nearest whole semitone value).
     * 
     * @param fractionalMidiNote midi/semitone number
     * @return detune amount in cents [-50,50]
     */
    private static int extractDetune(float fractionalMidiNote) {
        double rem = fractionalMidiNote - Math.floor(fractionalMidiNote);
        return (int) Math.round(100 * (rem > 0.5 ? rem - 1: rem));
    }

    /**
     * Convert to a midi note number.
     */
    public int toMidiNoteNumber() {
        // C-1 == note 0 if C4 is middle C
    	int middleCOffset = 5 - _middleCOctave;
    	// This number should be 60 for middle C
        int octaveOffset = (_octave + middleCOffset) * 12;
        return octaveOffset + _note.ordinal();
    }
    
    /**
     * Convert to a floating midi note number that incorporates
     * any detune amount
     */
    public float toFractionalMidiNoteNumber() {
        return toMidiNoteNumber() + getDetuneCents()/100f;
    }    
    
    /**
     * Convert to frequency in Hz.
     * 
     * @return frequency.
     */
    public double toFrequency() {
        float semis = REF_PITCH.distance(this);
        return REF_FREQ * Math.pow(RT12_2, semis);
    }
    
    /**
     * Compute signed distance in semitones from this pitch
     * to the given pitch.
     * 
     * @param pitch the pitch to measure against
     * @return distance in semitones.
     */
    public float distance(Pitch pitch) {
        return pitch.toFractionalMidiNoteNumber() - toFractionalMidiNoteNumber();
    }
    
    /**
     * Create a new pitch by subtracting the given number of semitones from
     * this.
     * 
     * @param semitones amount to subtract.
     * @return new tone, distance from this should be "semitones".
     */
    public Pitch sub(float semitones) {
        return new Pitch(toFractionalMidiNoteNumber() - semitones);
    }    
    
    /**
     * Create a new pitch by adding subtracting the given number of semitones to
     * this.
     * 
     * @param semitones semitones amount to add.
     * @return new tone, distance from this should be "semitones".
     */
    public Pitch add(float semitones) {
        return sub(-semitones);
    }
    
    /**
     * Create a new pitch by adjusting this one by cents (hundredths of a semitone).
     * @param cents int
     * @return new tone, distance "cents" hundredths of a semitone away.
     */
    public Pitch detune(int cents) {
    	// Need to deal with case where current fractionalNote + cents > 50 or <-50
    	int totalCents = getDetuneCents() + cents;
    	int wholeNote = toMidiNoteNumber() + (totalCents/50);
    	int fractionalNote = totalCents % 50;
    	if (fractionalNote > 0 && totalCents < 0){
    		fractionalNote -= 50; // shift to negative as mod operater returns positive
    	}
    	return new Pitch(wholeNote, fractionalNote);
    }

    /**
     * Logical equals. True of note, octave and detune are the same.
     * {@inheritDoc} 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Pitch) {
            Pitch p = (Pitch) obj;
            return _note.equals(p._note) && 
                   _octave == p._octave && 
                   _detuneCents == p._detuneCents;
        }
        return false;   
    }
    
    /**
     * Compare to another pitch.
     * {@inheritDoc} 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Pitch o) {
        if(equals(o)) return 0;
        
        if(_octave != o._octave) {
            return _octave < o._octave ? -1 : 1;
        }
        else if(_note != o._note) {
            return _note.compareTo(o._note);
        }
        else if(_detuneCents != o._detuneCents) {
            return _detuneCents < o._detuneCents ? -1 : 1;
        }
        else {
            throw new Error("Programming error. Shouldn't reach option.");
        }
    }
    
    /**
     * Convert from string generated by toString() to pitch.
     * 
     * @return Converted string, or null if parse error.
     */
    public static Pitch fromString(String rep) {
        Pitch retval = null;
        Matcher matcher = P_PAT.matcher(rep);
        if(matcher.matches()) {
            NotesEnum note = NotesEnum.valueOf(matcher.group(1).toUpperCase().replaceAll("#", "S"));
            int octave = Integer.parseInt(matcher.group(2));
            int detune = 0;
            if(matcher.groupCount() > 3) {
                String detuneStr = matcher.group(3);
                int mul = detuneStr.charAt(0) == '-' ? -1 : 1;
                detune = mul * Integer.parseInt(detuneStr.substring(1));
            }
            
            retval = new Pitch(note, octave, detune);
        }
        
        return retval;
    }
    
    /**
     * Human friendly representation.
     * {@inheritDoc} 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String retval = _note + String.valueOf(getOctave());
        
        int detune = getDetuneCents();
        if(detune != 0) {
            if(detune > 0) {
                retval += "+" + detune;
            }
            else {
                retval += detune;
            }
        }
        return retval;
    }

}
