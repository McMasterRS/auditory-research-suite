/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: PreTrialsInstructions.java 468 2008-12-04 20:42:37Z bhocking $
*/
package edu.mcmaster.maplelab.common.gui;

import edu.mcmaster.maplelab.common.datamodel.Session;


/**
 * Text before doing actual experiment. Configuration keys are "preTrialTitle"
 * and "preTrialText".
 * 
 * @version $Revision: 468 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class PreTrialsInstructions extends Instructions {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -1072447131644131662L;

	protected enum ConfigKeys {
        preTrialTitle,
        preTrialText
    }
    
    public PreTrialsInstructions(StepManager mgr, Session<?,?> session) {
        setStepManager(mgr);
        setTitleText(session.getString(ConfigKeys.preTrialTitle, null));
        setInstructionText(session.getString(ConfigKeys.preTrialText, null));
        setInstructionTextFontSize(session.getDefaultFontSize());
    }
    
    @Override
    protected boolean canGoBack() {
        return false;
    }
}
