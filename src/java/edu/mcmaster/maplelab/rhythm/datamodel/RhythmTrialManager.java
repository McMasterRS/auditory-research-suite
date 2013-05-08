package edu.mcmaster.maplelab.rhythm.datamodel;

import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy.BLOCK;
import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy.METABLOCK;
import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy.TRIAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mcmaster.maplelab.common.datamodel.PredeterminedTrialManager;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.RelativeTrialPosition;
import edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy;

/**
 * @author bguseman
 *
 */
public class RhythmTrialManager extends PredeterminedTrialManager<RhythmSession, RhythmTrial> {

	public RhythmTrialManager(RhythmSession session, boolean warmup) {
		super(session, TrialHierarchy.BLOCK, warmup);
	}

	@Override
	protected List<List<RhythmTrial>> generateMetaBlock(int metablock) {
        List<List<RhythmTrial>> retval = new ArrayList<List<RhythmTrial>>();
		
        // initialize common parameters
        RhythmSession session = getSession();
		List<Integer> baseIOIs = session.getBaseIOIs();
        List<Float> offsetDegrees = session.getOffsetDegrees();
 
        int trialCount = 0;
		int reps = session.getBlockSetRepetitions();
		// XXX: repetition and block instance are the same for rhythm
		for (int i = 0; i < reps; i++) {
			List<List<RhythmTrial>> rep = new ArrayList<List<RhythmTrial>>();
			for (Integer ioi : baseIOIs) {
				// first, create trials with tapping
				List<RhythmTrial> tapBlock = new ArrayList<RhythmTrial>();
				for (Float offset : offsetDegrees) {
					RhythmTrial trial = new RhythmTrial(ioi, offset, true);
					trial.setNumber(RelativeTrialPosition.REPETITION, i + 1);
					trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
		            tapBlock.add(trial);
		            ++trialCount;
		        }
				
				// next, create trials without tapping
				List<RhythmTrial> notapBlock = new ArrayList<RhythmTrial>();
				for (Float offset : offsetDegrees) {
					RhythmTrial trial = new RhythmTrial(ioi, offset, false);
					trial.setNumber(RelativeTrialPosition.REPETITION, i + 1);
					trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, i + 1);
					notapBlock.add(trial);
		            ++trialCount;
		        }

		        if (session.randomizeTrials()) {
		        	Collections.shuffle(tapBlock);
		        	Collections.shuffle(notapBlock);
		        }
		        
		        rep.add(tapBlock);
		        rep.add(notapBlock);
			}

			// Shuffle blocks - have to do it by each repetition here, if required
			if (session.randomizeBlocks() && !session.randomizeAcrossRepetitions()) {
				Collections.shuffle(rep);
			}
			retval.addAll(rep);
		}
		
		// Shuffle blocks - have to do it across each repetition here, if required
		if (session.randomizeBlocks() && session.randomizeAcrossRepetitions()) {
			Collections.shuffle(retval);
		}

		// numbering
    	// XXX: we assume that metablocks are all the same size!
		int blockNum = ((metablock - 1) * retval.size()) + 1;
		int blockInMetablock = 1;
		int trialNum = ((metablock - 1) * trialCount) + 1;
		int trialInMetablock = 1;
		
		for (List<RhythmTrial> block : retval) {
			int trialInBlock = 1;
			for (RhythmTrial trial : block) {
				trial.setNumber(METABLOCK, metablock);
				trial.setNumber(TrialHierarchy.BLOCK, blockNum);
				trial.setNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK, blockInMetablock);
				trial.setNumber(TrialHierarchy.TRIAL, trialNum);
				trial.setNumber(RelativeTrialPosition.TRIAL_IN_BLOCK, trialInBlock);
				trial.setNumber(RelativeTrialPosition.TRIAL_IN_METABLOCK, trialInMetablock);
				++trialNum;
				++trialInBlock;
				++trialInMetablock;
			}
			++blockNum;
			++blockInMetablock;
		}
 
		return retval;
	}

	// XXX: this is overridden to provide same functionality as before existence
	// of trial managers, but this should maybe eventually use super.generateWarmup()
	@Override
	protected List<List<RhythmTrial>> generateWarmup() {
		RhythmSession session = getSession();
        List<Float> offsetDegrees = session.getOffsetDegrees();
		int baseIOI = session.getBaseIOIs().get(0);
        
		List<RhythmTrial> block = new ArrayList<RhythmTrial>();
        for (Float offset : offsetDegrees) {
            RhythmTrial t = new RhythmTrial(baseIOI, offset, true);
            block.add(t);
        }
        
        int numTrials = session.getNumWarmupTrials();
        if (numTrials < block.size()) {
        	block = block.subList(0, numTrials);
        }

        if (session.randomizeTrials()) Collections.shuffle(block);
        
        // do numbering
		int trialNum = 1;
		int blockNum = -1;
		for (RhythmTrial trial : block) {
			trial.setNumber(TRIAL, -1 * trialNum);
			trial.setNumber(RelativeTrialPosition.TRIAL_IN_BLOCK, -1 * trialNum);
			trial.setNumber(RelativeTrialPosition.TRIAL_IN_METABLOCK, -1 * trialNum++);
			trial.setNumber(BLOCK, blockNum);
			trial.setNumber(RelativeTrialPosition.BLOCK_IN_METABLOCK, blockNum);
			trial.setNumber(METABLOCK, -1);
			trial.setNumber(RelativeTrialPosition.REPETITION, 1);
			trial.setNumber(RelativeTrialPosition.BLOCK_INSTANCE, 1);
		}
		
        List<List<RhythmTrial>> retval = new ArrayList<List<RhythmTrial>>(1);
        retval.add(block);
        return retval;
	}

	@Override
	protected String generateDescription(TrialHierarchy level) {
		if (level == TrialHierarchy.BLOCK) {
			return getStandardBlockDescription();
		}
		else if (level == TrialHierarchy.METABLOCK) {
			RhythmSession session = getSession();

			// blocks
	        List<Integer> iois = session.getBaseIOIs();
			String blockTypes = "\t\t\tBase IOIs: " + listString(iois) + "\n";
			blockTypes += "\t\t\tWith, without subject tapping\n";
			// each IOI, w/ and w/o tapping
			blockTypes = String.format("\t%d block(s), repeated %d time(s), constructed from:\n", 
							iois.size() * 2, session.getBlockSetRepetitions()) + blockTypes;
			
			// trials
			List<Float> offsets = session.getOffsetDegrees();
			String trialTypes = String.format("\tEach block contains %d trials constructed from:\n", 
					offsets.size());
			trialTypes += "\t\t\tOffset degrees: " + listString(offsets, 4, 4) + "\n";
			
			// block ordering
			String blockList = "\tBlocks (order will change on each repetition):\n";
			for (List<RhythmTrial> block : getCurrentMetablock()) {
				RhythmTrial t = block.get(0);
				blockList += String.format("\t\t\t%d: IOI=%d, %s tapping\n", t.getBlockNumber(), 
						t.getBaseIOI(), t.isWithTap() ? "with" : "without");
			}
			
			return String.format("\n********** Experiment Session Trial Details **********\n%s\n%s\n%s\n" +
					"**************************************************\n\n", blockTypes, trialTypes, blockList);
		}
		
		return null;
	}

}
