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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private void initializeBuildInfo(File dataDir) throws IOException {
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

    private static class RFrame extends ExperimentFrame<RhythmSession, RhythmBlock, RhythmTrial, RhythmTrialLogger> {
    	// hackish way to store this value before creating session
    	private static int _midiID;
    	
    	public static void setMidiDeviceID(int id) {
    		_midiID = id;
    	}
       
        public RFrame(SimpleSetupScreen setup) {
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
            RhythmSession retval = new RhythmSession(props);
            retval.setMIDIInputDeviceID(_midiID);
            return retval;
        }

        @Override
        protected InputStream getConfigData(File dataDir) throws IOException {
            String name = EXPERIMENT_BASENAME.toLowerCase() + ".properties";
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
    
    
    /**
     * Test code.
     * @param args ignored
     */
    public static void main(String[] args) {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Rhythm Experiment");
        try {
            
            final SimpleSetupScreen setup = new SimpleSetupScreen(EXPERIMENT_BASENAME.toLowerCase());

            // TODO: Clean up extending setup screen. Add something similar to what's
            // in the python experiments.
            int midiDevID = setup.prefs().getInt(RhythmSession.ConfigKeys.midiDevID.name(), 0);
            
            setup.addLabel("<html><p align=right>Tap Source (MIDI <br>Device #):");
            final JFormattedTextField midiDev = new JFormattedTextField();
            midiDev.setValue(new Integer(midiDevID));
            setup.addField(midiDev);            
            

            // Kludge to provide simple MIDI testing utility.
            JButton midiTest = new JButton("List and Test Devices");
            midiTest.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Component source = (Component) e.getSource();
                    JDialog dialog = MIDITestPanel.createDialog(source);
                    dialog.setVisible(true);
                }
            });
            
            setup.addLabel("MIDI System:");
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(midiTest);
            setup.addField(p);
            setup.display();
            
            // TODO: Yuck. Refactor.
            midiDevID = Integer.parseInt(midiDev.getText());
            setup.prefs().putInt(RhythmSession.ConfigKeys.midiDevID.name(), midiDevID);
            RFrame.setMidiDeviceID(midiDevID);
            
            RFrame f = new RFrame(setup);
            f.setTitle(String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion()));
            f.pack();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            LogContext.getLogger().log(Level.SEVERE, "Unrecoverable error", ex);
        }
    }
}
