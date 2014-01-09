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

/**
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Feb 28, 2007
 */
public enum NotesEnum {
    C("C"),
    CS("C#"),
    D("D"),
    DS("D#"),
    E("E"),
    F("F"),
    FS("F#"),
    G("G"),
    GS("G#"),
    A("A"),
    AS("A#"),
    B("B");
    
    // Number of notes, so we don't keep calling values().length
    private static final int numNotes = 12;
    
    private final String _displayName;
    private NotesEnum(String displayName) {
        _displayName = displayName;
    }
    
    @Override
    public String toString() {
        return _displayName;
    }
    
    public NotesEnum upSemitone(int numSemitones) {
    	return downSemitone(-numSemitones);
    }
    
    public NotesEnum downSemitone(int numSemitones) {
    	int newNote = (this.ordinal() - numSemitones) % numNotes;
    	if (newNote < 0) newNote += numNotes; //adjustment for mod returning negative numbers
    	return NotesEnum.values()[newNote];
    }
}
