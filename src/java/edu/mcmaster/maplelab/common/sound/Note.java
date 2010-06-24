/*
* Copyright (C) 2006-2007 University of Virginia
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

/**
 * A pitch with a duration.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   May 16, 2006
 */
public class Note {
    private final Pitch _pitch;
    /**
     * @uml.property  name="duration"
     */
    private final int _duration;

    /**
     * Standard ctor.
     * 
     * @param pitch pitch to play
     * @param duration duration of playback in milliseconds.
     */
    public Note(Pitch pitch, int duration) {
        _pitch = pitch;
        _duration = duration;
        
    }
    
    /**
     * Create a rest.
     * @param restDuration duration of rest in milliseconds.
     */
    public Note(int restDuration) {
        _pitch = null;
        _duration = restDuration;
    }

    /**
     * @return Pitch specification.
     */
    public Pitch getPitch() {
        return _pitch;
    }
    
    /**
     * @return Duration in milliseconds.
     */
    public int getDuration() {
        return _duration;
    }
    
    /**
     * Debuggging string representation.
     * {@inheritDoc} 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getPitch() + ":" + getDuration();
    }

}
