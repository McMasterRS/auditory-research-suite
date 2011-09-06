package edu.mcmaster.maplelab.common.gui;

import java.awt.Component;
import java.awt.Window;

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
	
	/**
	 * Get the parent window containing this, if available.
	 */
	protected Window getParentWindow() {
		Component c = getParent();
		while (!(c instanceof Window)) {
			if (c == null) return null;
			c = c.getParent();
		}
		
		return (Window) c;
	}
}
