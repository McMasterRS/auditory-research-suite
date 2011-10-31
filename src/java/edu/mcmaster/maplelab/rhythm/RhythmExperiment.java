/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id$
 */
package edu.mcmaster.maplelab.rhythm;

import java.awt.*;
import java.util.logging.Level;

import javax.swing.*;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.*;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Main container and configuration for the rhythm experiment.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 7, 2006
 */
public class RhythmExperiment extends Experiment<RhythmSession> {
    public static final String EXPERIMENT_BASENAME = "Rhythm";

    public RhythmExperiment(RhythmSession session) {
        super(session);
        
        setPreferredSize(new Dimension(640, 480));
    }
    
    @Override
    protected void loadContent(JPanel contentCard) {
    	RhythmSession session = getSession();
        if (contentCard != null) {
            CardLayoutStepManager sMgr = new CardLayoutStepManager(contentCard);
            contentCard.add(new Introduction(sMgr, session), "intro");   
            if(session.getNumWarmupTrials() > 0) {
                contentCard.add(new PreWarmupInstructions(sMgr, session),  "prewarmup");
                contentCard.add(new StimulusResponseScreen(sMgr, session, true), "warmup");
            }   
            contentCard.add(new PreTrialsInstructions(sMgr, session),  "pretrials");            
            contentCard.add(new StimulusResponseScreen(sMgr, session, false), "test");            
            contentCard.add(new Completion(sMgr, session), "complete");            
            contentCard.add(new JLabel(), "Blank");
        }
    }
    
    
    /**
     * @param args ignored
     */
    public static void main(String[] args) {
    	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Rhythm Experiment");
    	try {
    		// GUI initialization must be done in the EDT
    		EventQueue.invokeAndWait(new Runnable() {

    			@Override
    			public void run() {

    				final RhythmSetupScreen setup = new RhythmSetupScreen();
    				setup.display();

    				RhythmFrame f = new RhythmFrame(setup);
    				f.setTitle(String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion()));
    				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    				f.setLocationRelativeTo(null);
    				f.pack();
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
