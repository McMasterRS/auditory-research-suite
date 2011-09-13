/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.ExperimentFrame;
import edu.mcmaster.maplelab.toj.datamodel.TOJBlock;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * TOJ specific extension of ExperimentFrame.
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class TOJFrame extends ExperimentFrame<TOJSession, TOJBlock, TOJTrial, TOJTrialLogger> {

	public TOJFrame(TOJSetupScreen setup) {
        super(setup); 
    }
	@Override
    protected Container createContent(TOJSession session) {
    	TOJExperiment re = new TOJExperiment(session);
    	try {
			re.initializeBuildInfo(session.getDataDir());
		} 
        catch (IOException e) {
			LogContext.getLogger().log(Level.SEVERE, "Could not load build information.", e);
		}
    	return re;
    }

    @Override
    protected TOJSession createSession(Properties props) {
        return new TOJSession(props);
    }

    @Override
    protected InputStream getConfigData(File dataDir) throws IOException {
        String name = TOJExperiment.EXPERIMENT_BASENAME.toLowerCase() + ".properties";

        File f = new File(dataDir, name);
        
        if(f.exists()) {
            return new FileInputStream(f);              
        }
        else {
            return getClass().getResourceAsStream(name);
        }
    }

    @Override
    protected TOJTrialLogger initTrialLogger(File dataDir) {
        try {
            return new TOJTrialLogger(getSession(), dataDir);
        }
        catch (IOException ex) {
            logError(ex, "Error setting up logger", ex);
            return null;
        }  
    }
	@Override
	protected Color getFullScreenBackGround() {
		return Color.DARK_GRAY;
	}
    
}