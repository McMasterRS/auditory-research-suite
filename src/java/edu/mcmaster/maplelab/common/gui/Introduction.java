/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: Introduction.java 468 2008-12-04 20:42:37Z bhocking $
*/
package edu.mcmaster.maplelab.common.gui;

import edu.mcmaster.maplelab.common.datamodel.Session;


/**
 * Introductory text.
 * 
 * @version $Revision: 468 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class Introduction extends Instructions {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -2997186166711236777L;

	/**
     * @version   $Revision: 468 $
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    enum ConfigKeys {
        introScreenTitle,
        introScreenText        
    }
    
    public Introduction(StepManager mgr, Session<?,?> session) {
        setStepManager(mgr);
        setInstructionTextFontSize(session.getDefaultFontSize());
        setTitleText(session.getString(ConfigKeys.introScreenTitle, null));
        setInstructionText(session.getString(ConfigKeys.introScreenText, null));
    }
    
    @Override
    protected boolean canGoBack() {
        return false;
    }
    
    /**
     * This is a hack to make sure an applet context is properly 
     * refreshed on initial startup on MacOS/Firefox.
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#onVisible()
     */
    @Override
    protected void onVisible() {
        super.onVisible();
    }
}
