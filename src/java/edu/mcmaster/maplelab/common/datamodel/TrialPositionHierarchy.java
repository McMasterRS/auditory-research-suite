package edu.mcmaster.maplelab.common.datamodel;


public class TrialPositionHierarchy {
	
	public static TrialPositionItem[] values() {
		TrialPositionItem[] thArr = TrialHierarchy.values();
		TrialPositionItem[] rtpArr = RelativeTrialPosition.values();
		TrialPositionItem[] retval = new TrialPositionItem[thArr.length + rtpArr.length];
		for (TrialPositionItem tpi : thArr) {
			retval[tpi.index()] = tpi;
		}
		for (TrialPositionItem tpi : rtpArr) {
			retval[tpi.index()] = tpi;
		}
		return retval;
	}
	
	public static interface TrialPositionItem {
		public int index();
		public boolean isRelative();
	}
	
	public enum TrialHierarchy implements TrialPositionItem {
		METABLOCK,
		BLOCK,
		TRIAL;

		@Override
		public int index() { return ordinal(); }
		@Override
		public boolean isRelative() { return false; }
	}
	
	public enum RelativeTrialPosition implements TrialPositionItem {
		REPETITION,
		BLOCK_IN_METABLOCK,
		TRIAL_IN_METABLOCK,
		BLOCK_INSTANCE,
		TRIAL_IN_BLOCK;
		
		@Override
		public int index() { return TrialHierarchy.values().length + ordinal(); }
		@Override
		public boolean isRelative() { return true; }
	}
}
