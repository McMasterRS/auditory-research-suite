package edu.mcmaster.maplelab.toj.datamodel;

import java.util.List;

import edu.mcmaster.maplelab.common.datamodel.AVBlock;

public class TOJBlock extends AVBlock<TOJSession, TOJTrial> {

	protected TOJBlock(TOJSession session, int blockNum, AVBlockType type) {
		super(session, blockNum, type);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<TOJTrial> getTrials() {
		// TODO Auto-generated method stub
		return null;
	}

}
