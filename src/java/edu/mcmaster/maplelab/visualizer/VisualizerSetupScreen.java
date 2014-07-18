package edu.mcmaster.maplelab.visualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.gui.SimpleSetupScreen;
import edu.mcmaster.maplelab.common.gui.CloseButton;
import edu.mcmaster.maplelab.si.SIExperiment;
import edu.mcmaster.maplelab.visualizer.datamodel.VisualizerSession;

public class VisualizerSetupScreen extends SimpleSetupScreen<VisualizerSession> {
	public VisualizerSetupScreen() {
		super(VisualizerExperiment.EXPERIMENT_BASENAME.replace(" ", "").toLowerCase(), true, false);
	}

	@Override
	protected void initializeBuildInfo() throws IOException {
		// TODO Auto-generated method stub
		Experiment.initializeBuildInfo(VisualizerExperiment.class, getPrefsPrefix());
	}

	@Override
	protected void addExperimentFields() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getTitlePrefix() {
		// TODO Auto-generated method stub
		return String.format("Visualizer Experiment - Build %s", VisualizerExperiment.getBuildVersion());
	}

	@Override
	protected void applyExperimentSettings(VisualizerSession session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void putExperimentPrefs(Preferences prefs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadExperimentPrefs(Preferences prefs) {
		// TODO Auto-generated method stub
		
	}

    public void display() {
    	
        final JDialog d = new JDialog((Frame)null, true);
        d.setTitle(getTitlePrefix() + " - Setup");
        d.getContentPane().add(this);
        
        // TODO: refactor into CloseButton
        CloseButton close = new CloseButton("OKK");
        JPanel p = new JPanel();
        p.add(close);
        d.getRootPane().setDefaultButton(close);
        
        
        JButton b = new JButton("Cancel");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        p.add(b);
        
        d.getContentPane().add(p, BorderLayout.SOUTH);
        
        d.setMinimumSize(new Dimension(600, 200));
        d.pack();
        d.setLocationRelativeTo(null);
        
    	// Check for splash screen and close it
        SplashScreen splash = SplashScreen.getSplashScreen();
    	if (splash != null) splash.close();
    	
    	// modified by guanw
    	setDemoMode(true);
    	//setDataDir("/Users/guanw/Documents/Maplelab/auditory-research-suite/src/java/app/tmp/datafiles");

    	//d.setVisible(true);
    	// end of guanw
        
        //save();
        d.dispose();
    }

}
