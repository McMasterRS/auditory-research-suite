package edu.mcmaster.maplelab.common.datamodel;

import java.util.Arrays;
import java.util.List;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.RelativeTrialPosition;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialPositionItem;
import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy.*;

public abstract class PredeterminedTrialManager<S extends Session<?, ?, ?>, T extends Trial<?>> 
																	extends TrialManager<S, T> {

	private final int[] _totals;
	private List<List<T>> _metablock;
	private final int[] _indices; // zero-based
	private final int _logLevel;
	private final boolean _warmup;
	
	public PredeterminedTrialManager(S session, TrialHierarchy logLevel, boolean warmup) {
		super(session);
		_totals = new int[TrialHierarchy.values().length];
		Arrays.fill(_totals, -1);
		_logLevel = logLevel.index();
		_warmup = warmup;

		_indices = new int[TrialHierarchy.values().length];
		Arrays.fill(_indices, 0);

		setCount(METABLOCK, _warmup ? 1 : session.getMetaBlocks());
		_indices[METABLOCK.index()] = -1;
	}
	
	private void setCount(TrialHierarchy level, int count) {
		_totals[level.index()] = count;
	}
	
	private int getCount(TrialHierarchy level) {
		return _totals[level.index()];
	}
	
	private int getIndex(TrialHierarchy level) {
		return _indices[level.index()];
	}
	
	private void setIndex(TrialHierarchy level, int index) {
		_indices[level.index()] = index;
		if (!_warmup && _logLevel >= level.index()) {
			LogContext.getLogger().fine(generateDescription(level));
		}
	}
	
	public int getPosition(TrialHierarchy level) {
		return getIndex(level) + 1;
	}
	
	@Override
	public int currentPosition(TrialPositionItem item) {
		List<T> block = _metablock.get(getIndex(BLOCK));
		T trial = block.get(getIndex(TRIAL));
		return trial.getNumber(item);
	}
	
	protected abstract List<List<T>> generateMetaBlock(int metablock);
	protected abstract String generateDescription(TrialHierarchy level);
	
	protected List<List<T>> generateWarmup() {
		S session = getSession();
		List<List<T>> retval = generateMetaBlock(1);
		int warmupCount = session.getNumWarmupTrials();
		int initialCount = 0;
		for (List<T> block : retval) {
			initialCount += block.size();
		}
		
		if (initialCount > warmupCount) {
			int remaining = warmupCount;
			// Work through all blocks, pulling trials proportionally from each. 
			for (int i = retval.size()-1; i >= 0; i--) {
				List<T> block = retval.get(i);
				int localCount = block.size();
				if (localCount > 1) {
					// Get the percentage of total trials in this block
					float percent = (float) localCount / (float) initialCount;
					localCount = Math.round(warmupCount * percent);  
					
					// check for possibility of pulling out too many trials:
					// e.g. when there are 2 metablocks, and 3 warmups are requested, need to only pull 2, then 1 trial
					int difference = remaining - localCount;
					if (difference < 0) {
						localCount += difference; // difference is negative, so must be added to reduce localCount
					}
					block = block.subList(0, localCount);
					remaining -= localCount;
				}
				// Set the ith block to only have a proportional amount of trials, normalizing with warmup count.
				retval.set(i, block);
			}

		} 
		// else: does not matter. Should not ask for more warmup trials than actual trials.
		// If this does happen, the retval will be left unchanged, and all trials will be returned.
		
		// do numbering
		int trialNum = 1;
		int blockNum = 1;
		for (List<T> block : retval) {
			int trialInBlock = 1;
			for (T trial : block) {
				trial.setNumber(TRIAL, -1 * trialNum);
				trial.setNumber(RelativeTrialPosition.TRIAL_IN_BLOCK, -1 * trialInBlock++);
				trial.setNumber(RelativeTrialPosition.TRIAL_IN_METABLOCK, -1 * trialNum++);
				trial.setNumber(BLOCK, -1 * blockNum);
				trial.setNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK, -1 * blockNum);
				trial.setNumber(METABLOCK, -1);
			}
			blockNum++;
		}
		
		return retval;
	}
	
	/**
	 * Convenience method to get standard, simple block description output.
	 * Useful in generateDescription.
	 */
	protected String getStandardBlockDescription() {
		List<T> block = _metablock.get(getIndex(BLOCK));
		int size = block.size();
		int num = size > 0 ? block.get(0).getNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK) : 0;
		return String.format("> New block: Block %d: %d trials", num, size);
	}
	
	/**
	 * Log a grouped (consecutive) description of all trials in the current block.
	 */
	public void logCurrentBlockTrials() {
		List<T> block = _metablock.get(getIndex(BLOCK));
		int size = block.size();
		int num = size > 0 ? block.get(0).getNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK) : 0;
		LogContext.getLogger().fine(String.format("\n----- Block %d : %d trials -----", num, size));
		for (T t : block) {
			LogContext.getLogger().fine(t.getDescription());
		}
		LogContext.getLogger().fine("-----------------------------------\n");
	}
	
	protected List<List<T>> getCurrentMetablock() {
		return _metablock;
	}
	
	@Override
	public boolean hasNext() {
		if (getIndex(TRIAL) + 1 < getCount(TRIAL)) return true;
		if (getIndex(BLOCK) + 1 < getCount(BLOCK)) return true;
		if (getIndex(METABLOCK) + 1 < getCount(METABLOCK)) return true;
		return false;
	}
	
	@Override
	public T next() {
		
		// get next trial in block, if available
		int nextTrial = getIndex(TRIAL) + 1;
		if (nextTrial < getCount(TRIAL)) {
			setIndex(TRIAL, nextTrial);
			List<T> block = _metablock.get(getIndex(BLOCK));
			return block.get(nextTrial);
		}
		
		// get next block in metablock, if available
		int nextBlock = getIndex(BLOCK) + 1;
		if (nextBlock < getCount(BLOCK)) {
			setIndex(BLOCK, nextBlock);
			List<T> block = _metablock.get(getIndex(BLOCK));
			setCount(TRIAL, block.size());
			setIndex(TRIAL, 0);
			return block.get(getIndex(TRIAL));
		}
		
		// get next metablock, if available
		int nextMetablock = getIndex(METABLOCK) + 1;
		if (nextMetablock < getCount(METABLOCK)) {
			// the metablock number passed in to generate is a position, not an index
			_metablock = _warmup ? generateWarmup() : generateMetaBlock(nextMetablock + 1);
			setIndex(METABLOCK, nextMetablock);
			setCount(BLOCK, _metablock.size());
			setIndex(BLOCK, 0);
			List<T> block = _metablock.get(getIndex(BLOCK));
			setCount(TRIAL, block.size());
			setIndex(TRIAL, 0);
			return block.get(getIndex(TRIAL));
		}
		
		// otherwise, return null
		return null;
	}

	
	/**
	 * Utility method to generate a nicely formatted string list from a generic list of objects.
	 */
	protected static String listString(List<?> list) {
		return listString(list, -1, 0);
	}
	
	/**
	 * Utility method to generate a nicely formatted string list from a generic list of objects.
	 * Breakafter indicates where a line break should fall, and tabs is the tab depth.
	 */
	protected static String listString(List<?> list, int breakAfter, int tabs) {
		String retval = "[";
		for (int i = 0; i < list.size(); i++) {
			if (breakAfter > 0 && i > 0 && i % breakAfter == 0) {
				retval += "\n";
				for (int j = 0; j <= tabs; j++) {
					retval += "\t";
				}
			}
			retval += list.get(i).toString() + ", ";
		}
		return retval.substring(0, retval.length()-2) + "]";
	}
}
