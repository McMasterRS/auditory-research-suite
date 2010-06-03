/*
* Copyright (C) 2006 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: NotesEnum.java 399 2008-01-11 22:20:55Z sfitch $
*/
package edu.mcmaster.maplelab.common.sound;

/**
 * @version   $Revision: 399 $
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
        
    private final String _displayName;
    private NotesEnum(String displayName) {
        _displayName = displayName;
    }
    
    @Override
    public String toString() {
        return _displayName;
    }
}
