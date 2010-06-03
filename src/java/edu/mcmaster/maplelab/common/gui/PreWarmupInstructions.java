/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: PreWarmupInstructions.java 487 2009-07-05 16:31:09Z bhocking $
*/
package edu.mcmaster.maplelab.common.gui;

import edu.mcmaster.maplelab.common.datamodel.Session;


/**
 * Text before warmup period. Configuration keys are "preWarmupTitle" and
 * "preWarmupText".
 *
 * @version $Revision: 487 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class PreWarmupInstructions extends Instructions {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -4332707008106478345L;

	enum ConfigKeys {
        preWarmupTitle,
        preWarmupText
    }

    public PreWarmupInstructions(StepManager mgr, Session<?,?> session, String titleKey, String textKey) {
        setStepManager(mgr);
        setTitleText(session.getString(titleKey));
        setInstructionText(session.getString(textKey));
        setInstructionTextFontSize(session.getDefaultFontSize());
    }

    public PreWarmupInstructions(StepManager mgr, Session<?,?> session) {
        this(mgr, session, ConfigKeys.preWarmupTitle.toString(), ConfigKeys.preWarmupText.toString());
    }
}
