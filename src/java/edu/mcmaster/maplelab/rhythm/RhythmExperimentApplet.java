/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: RhythmExperimentApplet.java 474 2009-03-20 17:53:30Z bhocking $
*/
package edu.mcmaster.maplelab.rhythm;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.ExperimentApplet;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Top level applet container for rhythm experiment.
 * 
 * 
 * @version $Revision: 474 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class RhythmExperimentApplet extends ExperimentApplet<RhythmSession> {

    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = 4385730654355580183L;

	@Override
    protected Container createContent(RhythmSession session) {
        return new RhythmExperiment(session);
    }

    @Override
    protected RhythmSession createSession(Properties props) {
        return new RhythmSession(props);
    }

    @Override
    protected TrialLogger<RhythmBlock, RhythmTrial> initTrialLogger() {
        String idStr = getParameter("experiment_id");
        if(idStr == null) {
            logError(null, "Parameter 'experiment_id' is missing.");
            return null;
        }
        
        URL base = getCodeBase();
        LogContext.getLogger().finer(String.format("Base URL is '%s'", base));
        
        try {
//            return new RhythmTrialLogger(getSession(), base);
            File trialLog = File.createTempFile("rhythm", "txt");
            return new RhythmTrialLogger(getSession(), trialLog.getParentFile(), trialLog.getName());
        }
        catch (IOException ex) {
            logError(ex, "Couldn't connect to experiment_id: %s", idStr);
            return null;
        }        
    }
}
