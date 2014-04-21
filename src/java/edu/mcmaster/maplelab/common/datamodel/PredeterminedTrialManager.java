package edu.mcmaster.maplelab.common.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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
		List<List<T>> metablock = generateMetaBlock(1);
		int warmupCount = session.getNumWarmupTrials();
		
		if (metablock.size() <= 0) {
			LogContext.getLogger().warning("No Trials were generated for warmup.\n" +
					"Check that properties file actually requests trials.");
		}
		
		int numBlocks = metablock.size();
		
		// Count total number of trials in all blocks
		int totalNumTrials = 0;
		// Also build list of list iterators, for iterating through each block.
		//	This allows for unevenly sized blocks
		List<ListIterator<T>> blockIterators = new ArrayList<ListIterator<T>>();
		// Build/initialize return list of blocks
		List<List<T>> retval = new ArrayList<List<T>>(numBlocks);
		
		for(int i=0; i < numBlocks; i++) {
			List<T> block = metablock.get(i);
			
			totalNumTrials += metablock.get(i).size();
			blockIterators.add(block.listIterator());
			retval.add(new ArrayList<T>());
		}
		
		// Check if more warmup trials have been requested than we have available. 
		// In this case, cap the warmup count to be the same size as the number of Trials.
		if (warmupCount > totalNumTrials) {
			LogContext.getLogger().fine(String.format("Requested %d warmup trials, " +
					"but only have %d experimental trials to pull from.\nMaking %d warmup trials.",
					warmupCount, totalNumTrials, totalNumTrials));
			warmupCount = totalNumTrials;
		}	
		
		int addedWarmupTrials = 0;
		ListIterator<List<T>> metablockIterator = metablock.listIterator();
		// WARNING TO FUTURE EDITS...
		// We can iterate infinitely here ONLY because warmupCount has been capped to the total 
		// number of trials (totalNumTrials) in all blocks in the metablock. So at least 'warmupCount' 
		// number of trials can be found. 
		while (addedWarmupTrials < warmupCount) {
			// Get next block... This if/else construct basically allows infinite 
			//  cycling through the blocks in metablock
			if (metablockIterator.hasNext()) {
				int ithBlock = metablockIterator.nextIndex();
				metablockIterator.next(); // step block selection forward
				
				// Using pre-gathered ListIterators for blocks, so we don't reset them.
				ListIterator<T> ithBlockIterator = blockIterators.get(ithBlock);
				if (ithBlockIterator.hasNext()) {
					T trial = ithBlockIterator.next();
					retval.get(ithBlock).add(trial);
					addedWarmupTrials++;
				} else {
					// Don't do anything... restarts while loop, and next block is checked.
				}
			} else {
				// have cycled through all blocks, so reset the iterator.
				metablockIterator = metablock.listIterator();
			}
		}
		
		
		// It is possible to end up with empty lists in the retval if fewer warmup trials are requested
		// than the number of blocks in the metablock.
		// Here they are removed to avoid index out of bounds errors later on.
		// Don't remove while going through list
		List<List<T>> toRemove = new ArrayList<List<T>>();
		for (List<T> block : retval) {
			if (block.isEmpty()) {
				// Don't remove while going through list
				toRemove.add(block);
			}
		}
		retval.removeAll(toRemove);
		
		
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
