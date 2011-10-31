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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base class for trial data.
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Sep 14, 2006
 */
public abstract class Trial<T> {
    private int _trialNum;
    private String _timeStamp;
    
    /**
     * @uml.property  name="response"
     */
    private T _response;

    public Trial() {
    }
    
    public String toString() {
        return String.format("Trial %d", _trialNum);
    }

    /**
     * Get the trail number. 
     * 
     */
    public final int getNum() {
        return _trialNum;
    }
    
    /**
     * Set the trial number.
     */
    public final void setNum(int trialNum) {
        _trialNum = trialNum;
    }
    
    /**
     * Set the response value.
     * @uml.property  name="response"
     */
    public void setResponse(T response) {
        _response = response;
    }
    
    /**
     * Get the response value.
     * @uml.property  name="response"
     */
    public T getResponse() {
        return _response;
    }
    
    /**
     * Set the trial start time stamp.
     */
    public void setTimeStamp(Date stamp) {
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy; hh:mm:ss a");
    	_timeStamp = sdf.format(stamp);
    }
    
    /**
     * Get the trial start time stamp.
     */
    public String getTimeStamp() {
    	return _timeStamp;
    }
    
    /**
     * Indicate if the current response is accurate.
     */
    public abstract boolean isResponseCorrect();
    
    /**
     * Get a full, human-readable description of the trial.
     */
    public abstract String getDescription();
}
