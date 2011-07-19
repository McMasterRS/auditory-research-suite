package edu.mcmaster.maplelab.rhythm;

import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.ExperimentFrame;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmBlock;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmTrial;

/**
 * Rhythm-specific extension of ExperimentFrame.
 * 
 * @author bguseman
 *
 */
public class RhythmFrame extends ExperimentFrame<RhythmSession, RhythmBlock, RhythmTrial, RhythmTrialLogger> {
    
    public RhythmFrame(RhythmSetupScreen setup) {
        super(setup); 
    }

    @Override
    protected Container createContent(RhythmSession session) {
    	RhythmExperiment re = new RhythmExperiment(session);
    	try {
			re.initializeBuildInfo(session.getDataDir());
		} 
        catch (IOException e) {
			LogContext.getLogger().log(Level.SEVERE, "Could not load build information.", e);
		}
    	return re;
    }

    @Override
    protected RhythmSession createSession(Properties props) {
        return new RhythmSession(props);
    }

    @Override
    protected InputStream getConfigData(File dataDir) throws IOException {
        String name = RhythmExperiment.EXPERIMENT_BASENAME.toLowerCase() + ".properties";
        File f = new File(dataDir, name);
        
        if(f.exists()) {
            return new FileInputStream(f);              
        }
        else {
            return getClass().getResourceAsStream(name);
        }
    }

    @Override
    protected RhythmTrialLogger initTrialLogger(File dataDir) {
        try {
            return new RhythmTrialLogger(getSession(), dataDir);
        }
        catch (IOException ex) {
            logError(ex, "Error setting up logger", ex);
            return null;
        }  
    }
    
};
