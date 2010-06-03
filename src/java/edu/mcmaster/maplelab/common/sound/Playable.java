/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: Playable.java 476 2009-06-11 12:56:54Z bhocking $
*/

package edu.mcmaster.maplelab.common.sound;




/**
 * Interface for objects that can generate audio.
 * 
 * @version $Revision: 476 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Oct 1, 2007
 */
public interface Playable {
    /**
     * Initiate playback of the audio source. Call blocks until playback
     * is finished.
     */
    void play();
    
    /**
     * Query duration of audio source in milliseconds.
     */
    int duration();
    
    /**
     * Get an identifier name.
     */
    String name();
}
