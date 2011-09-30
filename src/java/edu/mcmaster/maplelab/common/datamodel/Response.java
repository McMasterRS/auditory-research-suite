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

package edu.mcmaster.maplelab.common.datamodel;



/**
 * POJO for response data.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 8, 2006
 */
public class Response {
    private final Answer _answer;
    private final ConfidenceLevel _conf;

    public Response(Answer answer, ConfidenceLevel conf) {
        _answer = answer;
        _conf = conf;
    }
    
    public ConfidenceLevel getConfidence() {
        return _conf;
    }
    
    public Answer getAnswer() {
        return _answer;
    }
    
    @Override
    public String toString() {
        return String.format("Response: answer=\"%s\", confidence=\"%s\"", 
        		getAnswer(), getConfidence());
    }
}
