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

package edu.mcmaster.maplelab.av.media;

import java.util.concurrent.CountDownLatch;



/**
 * Interface for objects that can generate audio and/or video.
 * 
 * @version $Revision: 476 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Oct 1, 2007
 */
public interface Playable extends MediaSource {
    /**
     * Initiate playback of the source. Blocks until latch released, 
     * then blocks until playback is finished.
     */
    void play(CountDownLatch controlLatch);
    /**
     * Initiate playback of the source. Call blocks until playback
     * is finished.
     */
    void play();
    
    /**
     * Query duration of the source in milliseconds.
     */
    int duration();
    
    /**
     * Get an identifier name.
     */
    String name();
    
    /**
     * Set the volume level.  Must be called before {@link #play()}.
     */
    void setVolume(float volume);
    
    /**
     * Set the mute state.
     */
    void setMute(boolean mute);
    
    /**
     * Add a PlayableListener to this.
     */
    void addListener(PlayableListener listener);
    
    /**
     * Remove a PlayableListener from this.
     */
    void removeListener(PlayableListener listener);
}
