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
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;


/**
 * Completion text screen
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   May 10, 2006
 */
public class Completion extends Instructions {
    public enum ConfigKeys {
        completionTitle,
        completionText
    }

    private TrialLogger<?> _logger;
    
    public Completion(StepManager mgr, Session<?,?,?> session) {
        this(mgr, session, false);
    }
    
    public Completion(StepManager mgr, Session<?,?,?> session, boolean showPrevNext) {
        super(showPrevNext);
        setStepManager(mgr);
        _logger = session.getTrialLogger();
        initText(session);
    }
    
    protected void initText(Session<?,?,?> session) {
        setTitleText(session.getString(ConfigKeys.completionTitle, "completion title missing"));
        setInstructionText(session.getString(ConfigKeys.completionText, "completion text missing"));
        setInstructionTextFontSize(session.getDefaultFontSize());
    }
    
    @Override
    protected void onVisible() {
        _logger.shutdown();
    }
    
    @Override
    protected boolean canGoBack() {
        return false;
    }
}
