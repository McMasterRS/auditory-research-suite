package edu.mcmaster.maplelab.rhythm;

import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import edu.mcmaster.maplelab.common.gui.ExperimentFrame;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmTrial;

/**
 * Rhythm-specific extension of ExperimentFrame.
 * 
 * @author bguseman
 *
 */
public class RhythmFrame extends ExperimentFrame<RhythmSession, RhythmTrial, RhythmTrialLogger> {
    
    public RhythmFrame(RhythmSetupScreen setup) {
        super(setup); 
    }

    @Override
    protected Container createContent(RhythmSession session) {
    	return new RhythmExperiment(session);
    }

    @Override
    protected RhythmSession createSession(Properties props) {
        return new RhythmSession(props);
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

	@Override
	protected Color getFullScreenBackGround() {
		return Color.DARK_GRAY;
	}
    
};
