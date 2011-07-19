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
import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.*;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.*;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Main container and configuration for the rhythm experiment.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 7, 2006
 */
public class RhythmExperiment extends JPanel {
    public static final String EXPERIMENT_BASENAME = "Rhythm";
    
    private enum VersionProps {
		buildVersion,
		buildDate
	}
	
	private static String _buildVersion;
	private static String _buildDate;
	private final RhythmSession _session;
    private JPanel _contentCard;

    public RhythmExperiment(RhythmSession session) {
        super(new BorderLayout());
        _session = session;
        
        LogContext.getLogger().finest("baseIOIs: " + _session.getBaseIOIs());
        
        add(getContent(), BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(640, 480));
    }
    
    /**
     * Load build information from the given properties.
     */
	private static void loadBuildInfo(Properties props) {
		_buildVersion = props.getProperty(VersionProps.buildVersion.name(), "-1");
		_buildDate = props.getProperty(VersionProps.buildDate.name(), "00000000");
	}
	
	public static String getBuildVersion() {
		return _buildVersion;
	}
	
	public static String getBuildDate() {
		return _buildDate;
	}
    
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getContent() {
        if (_contentCard == null) {
            _contentCard = new JPanel();
            _contentCard.setLayout(new CardLayout());
            CardLayoutStepManager sMgr = new CardLayoutStepManager(_contentCard);
            _contentCard.add(new Introduction(sMgr, _session), "intro");   
            if(_session.getNumWarmupTrials() > 0) {
                _contentCard.add(new PreWarmupInstructions(sMgr, _session),  "prewarmup");
                _contentCard.add(new StimulusResponseScreen(sMgr, _session, true), "warmup");
            }   
            _contentCard.add(new PreTrialsInstructions(sMgr, _session),  "pretrials");            
            _contentCard.add(new StimulusResponseScreen(sMgr, _session, false), "test");            
            _contentCard.add(new Completion(sMgr, _session), "complete");            
            _contentCard.add(new JLabel(), "Blank");
        }
        return _contentCard;
    }
    
    /**
     * Initialize build information.
     */
    protected void initializeBuildInfo(File dataDir) throws IOException {
    	String name = EXPERIMENT_BASENAME.toLowerCase() + ".version.properties";
        File f = new File(dataDir, name);
        InputStream is = null;
        if (f.exists()) {
            is = new FileInputStream(f);            
        }
        else {
            is = getClass().getResourceAsStream(name);
        }
        
        Properties props = new Properties();
        try {
            props.load(is);
        }
        catch (Exception ex) {
        	LogContext.getLogger().log(Level.SEVERE, "Error reading version file", ex);
        }
        finally {
            if(is != null)  try { is.close(); } catch (IOException e) {}
        }
        
        loadBuildInfo(props);
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
