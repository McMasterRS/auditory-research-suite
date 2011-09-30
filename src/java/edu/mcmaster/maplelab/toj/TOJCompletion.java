package edu.mcmaster.maplelab.toj;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.gui.CloseButton;
import edu.mcmaster.maplelab.common.gui.Completion;
import edu.mcmaster.maplelab.common.gui.StepManager;

public class TOJCompletion extends Completion {

	public TOJCompletion(StepManager mgr, Session<?, ?, ?> session) {
		super(mgr, session);
        
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[center]", "[center]"));
        p.add(new CloseButton("Close the experiment"), BorderLayout.CENTER);
        getContentPanel().add(p);
	}

}
