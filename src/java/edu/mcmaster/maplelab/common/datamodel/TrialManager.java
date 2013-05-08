package edu.mcmaster.maplelab.common.datamodel;

import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialPositionItem;


/**
 * Class for tracking the entire experiment hierarchy down to the trial level.
 */
public abstract class TrialManager<S extends Session<?,?,?>, T extends Trial<?>> {
	
	private final S _session;
	
	public TrialManager(S session) {
		_session = session;
	}
	
	protected S getSession() { return _session; }
	
	public abstract int currentPosition(TrialPositionItem level);
	public abstract boolean hasNext();
	public abstract T next();
	
}
