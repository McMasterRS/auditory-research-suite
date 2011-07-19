package edu.mcmaster.maplelab.toj.datamodel;

import java.util.List;

import edu.mcmaster.maplelab.common.datamodel.Block;

public class TOJBlock extends Block<TOJSession, TOJTrial> {

	protected TOJBlock(TOJSession session, int blockNum) {
		super(session, blockNum);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<TOJTrial> getTrials() {
		// TODO Auto-generated method stub
		return null;
	}

}
