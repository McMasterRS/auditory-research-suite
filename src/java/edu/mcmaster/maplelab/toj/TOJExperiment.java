/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import javax.swing.JPanel;
import java.awt.*;
import java.util.logging.Level;

import javax.swing.*;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.*;
import edu.mcmaster.maplelab.toj.datamodel.*;

/**
 * TOJExperiment allows the user to set up and run one TOJTrial.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class TOJExperiment extends Experiment<TOJSession> {
		public static final String EXPERIMENT_BASENAME = "TOJ";

	    public TOJExperiment(TOJSession session) {
	        super(session);
	    }

		@Override
		protected void loadContent(JPanel contentCard) {
			TOJSession session = getSession();
	        if (contentCard != null) {
	            CardLayoutStepManager sMgr = new CardLayoutStepManager(contentCard);
	            contentCard.add(new Introduction(sMgr, session), "intro");   
	            if(session.getNumWarmupTrials() > 0) {
	                contentCard.add(new PreWarmupInstructions(sMgr, session),  "prewarmup");
	                contentCard.add(new TOJStimulusResponseScreen(sMgr, session, true), "warmup");
	            }   
	            contentCard.add(new PreTrialsInstructions(sMgr, session),  "pretrials");            
	            contentCard.add(new TOJStimulusResponseScreen(sMgr, session, false), "test");            
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

	    				final TOJSetupScreen setup = new TOJSetupScreen();
	    				setup.display();

	    				TOJFrame f = new TOJFrame(setup);
	    				f.setTitle(String.format("TOJ Experiment - Build %s", TOJExperiment.getBuildVersion()));
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