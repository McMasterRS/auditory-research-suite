package edu.mcmaster.maplelab.si;

import java.awt.EventQueue;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.CardLayoutStepManager;
import edu.mcmaster.maplelab.common.gui.CloseCompletion;
import edu.mcmaster.maplelab.common.gui.Introduction;
import edu.mcmaster.maplelab.common.gui.PreTrialsInstructions;
import edu.mcmaster.maplelab.common.gui.PreWarmupInstructions;
import edu.mcmaster.maplelab.si.datamodel.SISession;

public class SIExperiment extends Experiment<SISession> {
	public static final String EXPERIMENT_BASENAME = "SI";

	public SIExperiment(SISession session) {
		super(session);
	}

	@Override
	protected void loadContent(JPanel contentCard) {
		SISession session = getSession();
        if (contentCard != null) {
            CardLayoutStepManager sMgr = new CardLayoutStepManager(contentCard);
            contentCard.add(new Introduction(sMgr, session), "intro");   
            if(session.getNumWarmupTrials() > 0) {
                contentCard.add(new PreWarmupInstructions(sMgr, session),  "prewarmup");
                contentCard.add(new SIStimulusResponseScreen(sMgr, session, true), "warmup");
            }   
            contentCard.add(new PreTrialsInstructions(sMgr, session),  "pretrials");            
            contentCard.add(new SIStimulusResponseScreen(sMgr, session, false), "test");            
            contentCard.add(new CloseCompletion(sMgr, session), "complete");            
            contentCard.add(new JLabel(), "Blank");
        }
	}

	public static void main(String[] args) {
    	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TOJ Experiment");
    	try {
    		// GUI initialization must be done in the EDT
    		EventQueue.invokeAndWait(new Runnable() {

    			@Override
    			public void run() {

    				final SISetupScreen setup = new SISetupScreen();
    				setup.display();

    				SIFrame f = new SIFrame(setup);
    				f.setTitle(String.format("Sensory Integration Experiment - Build %s", 
    						SIExperiment.getBuildVersion()));
    				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    				f.pack();
					if (!f.isDemo()) {
    					f.setExperimentSize(800, 800);
    					f.setLocationRelativeTo(null);
	    			}
    				else {
    					// center window first
    					f.setLocationRelativeTo(null);
	    				// adjust the window left to make room for the animation window
    					f.setLocation(f.getLocation().x - 300, f.getLocation().y);
    				}
    				f.setVisible(true);
    			}
    		});
    		LogContext.getLogger().finer("Experiment GUI launched");
    	}
        catch (Exception ex) {
            ex.printStackTrace();
            LogContext.getLogger().log(Level.SEVERE, "Unrecoverable error", ex);
        }
    }
}
