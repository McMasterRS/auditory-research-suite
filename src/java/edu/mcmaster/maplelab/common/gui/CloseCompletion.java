package edu.mcmaster.maplelab.common.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.datamodel.Session;

public class CloseCompletion extends Completion {

	public CloseCompletion(StepManager mgr, Session<?, ?, ?> session) {
		super(mgr, session);
        
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[center]", "[center]"));
        p.add(new CloseButton("Close the experiment"), BorderLayout.CENTER);
        getContentPanel().add(p);
	}

}
