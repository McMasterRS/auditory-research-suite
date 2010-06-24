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
package edu.mcmaster.maplelab.common.gui;

/**
 * Interface for class managing a set of steps to perform.
 * 
 * @version $Revision$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Apr 17, 2006
 */
public interface StepManager {
    /**
     * Go to the next step
     *
     */
    void next();
    
    /**
     * Determine if there is a next step available.
     */
    boolean hasNext();
    
    
    /**
     * Go to the previous step
     *
     */
    void previous();    
    
    /**
     * Determine if there is a previous step to go to.
     */
    boolean hasPrevious();
}
