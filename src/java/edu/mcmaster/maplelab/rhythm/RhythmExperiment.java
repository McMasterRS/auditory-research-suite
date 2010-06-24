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
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.*;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Main container and configuration for the rhythm experiment.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 7, 2006
 */
public class RhythmExperiment extends JPanel {
    public static final String CONF_BASENAME = "rhythm";
    
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
    
    private static class RFrame extends ExperimentFrame<RhythmSession, RhythmBlock, RhythmTrial> {
        private final int _midiID;

        public RFrame(SimpleSetupScreen setup, int midiID) {
            super(setup);
            _midiID = midiID;
        }

        @Override
        protected Container createContent(RhythmSession session) {
            return new RhythmExperiment(session);
        }

        @Override
        protected RhythmSession createSession(Properties props) {
            RhythmSession retval = new RhythmSession(props);
            retval.setMIDIInputDeviceID(_midiID);
            return retval;
        }

        @Override
        protected InputStream getConfigData(File dataDir) throws IOException {
            String name = CONF_BASENAME + ".properties";
            File f = new File(dataDir, name);
            
            if(f.exists()) {
                return new FileInputStream(f);
                
            }
            else {
                return getClass().getResourceAsStream(name);
            }
        }

        @Override
        protected TrialLogger<RhythmBlock, RhythmTrial> initTrialLogger(File dataDir) {
            try {
                return new RhythmTrialLogger(getSession(), dataDir, CONF_BASENAME);
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
            
            final SimpleSetupScreen setup = new SimpleSetupScreen(CONF_BASENAME);

            // TODO: Clean up extending setup screen. Add something similar to what's
            // in the python experiments.
            int midiDevID = setup.prefs().getInt(RhythmSession.ConfigKeys.midiDevID.name(), 0);
            
            setup.addLabel("MIDI Device:");
            final JFormattedTextField midiDev = new JFormattedTextField();
            midiDev.setValue(new Integer(midiDevID));
            setup.addField(midiDev);            
            

            // Kludge to provide simple MIDI testing utility.
            JButton midiTest = new JButton("Test");
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
            setup.prefs().putInt(RhythmSession.ConfigKeys.midiDevID.name(), Integer.parseInt(midiDev.getText()));
            
            int midiID = Integer.parseInt(midiDev.getText());
            
            RFrame f = new RFrame(setup, midiID);
            f.setTitle("Rhythm Experiment");
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
