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

import java.util.Random;

import edu.mcmaster.maplelab.common.datamodel.PropertyFileValue;

/**
 * Encapsulation of an upper/lower bound on a range of tones.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Sep 14, 2006
 */
public class PitchRange implements PropertyFileValue {
    // This is protected so test rig can futz with generator seed.
    protected static final Random rand = new Random();
    
    private final Pitch _lower;
    private final Pitch _upper;

    /**
     * Standard ctor. Provided pitches will be automatically sorted into
     * upper/lower positions.
     * 
     * @param p1 first pitch
     * @param p2 second pitch
     */
    public PitchRange(Pitch p1, Pitch p2) {
        boolean lowerLess = p1.compareTo(p2) <= 0;
        _lower =  lowerLess ? p1 : p2;
        _upper = lowerLess ? p2 : p1;
    }
    
    /**
     * Create a range centered around "center" with the given width in semitones.
     * 
     * @param center Center of range
     * @param semitoneWidth width in semitones.
     */
    public PitchRange(Pitch center, float semitoneWidth) {
        this(center.sub(semitoneWidth/2.0f), center.add(semitoneWidth/2.0f));
    }

    /**
     * Lower pitch ofrange.
     * @uml.property  name="lower"
     */
    public Pitch getLower() {
        return _lower;
    }

    /**
     * Upper pitch of range
     * @uml.property  name="upper"
     */
    public Pitch getUpper() {
        return _upper;
    }
    
    /**
     * Get the center pitch in the range with cents precision.
     * 
     * @return center pitch in range.
     */
    public Pitch center() {
        float width = semitoneWidth()/2.0f;
        return new Pitch(_lower.toFractionalMidiNoteNumber() + width);
    }
    
    /**
     * Compute the distance between the upper and lower bounds in 
     * fractional semitones.
     */
    public float semitoneWidth() {
        return (_upper.toFractionalMidiNoteNumber() - _lower.toFractionalMidiNoteNumber());
    }
    
    /**
     * Generate a random pitch within the range.
     */
    public Pitch randomInRange() {
        float note = getLower().toFractionalMidiNoteNumber() + rand.nextFloat() * semitoneWidth();
        
        return new Pitch(note);
    }
    
    /**
     * Determine if the given pitch falls inside the given range (bounds inclusive).
     * 
     * @param pitch pitch to test against.
     * @return True if pitch is included in the range, false otherwise.
     */
    public boolean contains(Pitch pitch) {
        return _lower.compareTo(pitch) <= 0 && _upper.compareTo(pitch) >= 0;
    }    
    
    /**
     * Create a display string representation of this.
     * {@inheritDoc} 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return _lower + " \u2194 " + _upper;
    }

    /**
     * Same as toString but doesn't use "fancy" arrow to divide bounds.
     */
    public String toPropertyValueFormat() {
        return _lower + " to " + _upper;
    }


}
