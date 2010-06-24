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
 * POJO for response data.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 8, 2006
 */
public class Response {
    private final boolean _isProbeToneAccurate;
    private final ConfidenceRatingEnum _conf;

    public Response(boolean isProbeToneAccurate, ConfidenceRatingEnum conf) {
        _isProbeToneAccurate = isProbeToneAccurate;
        _conf = conf;
    }
    
    public ConfidenceRatingEnum getConfidence() {
        return _conf;
    }
    
    public boolean getProbeToneAccurate() {
        return _isProbeToneAccurate;
    }
    
    @Override
    public String toString() {
        return String.format("Response[accurate=%b,confidence=%s]", 
            getProbeToneAccurate(), getConfidence());
    }
}
