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

package edu.mcmaster.maplelab.rhythm.datamodel;


/**
 * Enumeration of user confidence ratings.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 8, 2006
 */
public enum ConfidenceRatingEnum {
    VERY_CONFIDENT,
    CONFIDENT,
    SOMEWHAT_CONFIDENT,
    SOMEWHAT_UNCONFIDENT,
    UNCONFIDENT,
    VERY_UNCONFIDENT;
    
    public String toString() {
        String name = name().toLowerCase();
        name = name.replace('_', ' ');
        
        name = Character.toTitleCase(name.charAt(0)) + name.substring(1);
        
        return name;
    }
}
