/*
* Copyright (C) 2006-2007 University of Virginia
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

import edu.mcmaster.maplelab.common.datamodel.Session;


/**
 * Text before warmup period. Configuration keys are "preWarmupTitle" and
 * "preWarmupText".
 * 
 * @version $Revision$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class PreWarmupInstructions extends Instructions {
    enum ConfigKeys {
        preWarmupTitle,
        preWarmupText
    }
    
    public PreWarmupInstructions(StepManager mgr, Session<?,?> session) {
        setStepManager(mgr);
        setTitleText(session.getString(ConfigKeys.preWarmupTitle, null));
        setInstructionText(session.getString(ConfigKeys.preWarmupText, null));
        setInstructionTextFontSize(session.getDefaultFontSize());
    }
}
