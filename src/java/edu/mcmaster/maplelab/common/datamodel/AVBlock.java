package edu.mcmaster.maplelab.common.datamodel;

import java.util.List;

public abstract class AVBlock<S extends Session<?,?,?>, T extends Trial<?>> extends Block<S, T> {
	
	public enum AVBlockType {
		AUDIO_ONLY,
		VIDEO_ONLY,
		AUDIO_VIDEO
	}
	
	private final AVBlockType _type;

	protected AVBlock(S session, int blockNum, AVBlockType type) {
		super(session, blockNum);
		_type = type;
	}
	
	/**
	 * Get the type of this block.
	 */
	public AVBlockType getType() {
		return _type;
	}

	@Override
	public abstract List<T> getTrials();

}
