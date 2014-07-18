package edu.mcmaster.maplelab.visualizer.datamodel;

import java.util.Properties;

//import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.visualizer.VisualizerGUIPanel;
//import edu.mcmaster.maplelab.si.SITrialLogger;
import edu.mcmaster.maplelab.si.datamodel.*;


public class VisualizerSession extends SISession {

	public VisualizerSession(Properties props) {
		super(props);
	}


	@Override
	public DemoGUIPanel<?, SITrial> getExperimentDemoPanel() {
		return new VisualizerGUIPanel(this);
	}

}
