package edu.mcmaster.maplelab.common.gui;

import javax.swing.JPanel;

import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.datamodel.Trial;

public abstract class DemoGUIPanel <S extends Session<?, T, ?>, T extends Trial<?>> extends JPanel{
	
	private S _session;
	
	public DemoGUIPanel(S session) {
		_session = session;
	}
	public abstract T getTrial();
	
	protected S getSession() {
		return _session;
	}
}
