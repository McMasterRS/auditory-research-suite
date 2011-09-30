package edu.mcmaster.maplelab.common.datamodel;

import java.util.List;

public abstract class AVBlock<S extends Session<?,?,?>, T extends Trial<?>> extends Block<S, T> {
	
	public enum AVBlockType {
		AUDIO_ONLY("Audio only"),
		VIDEO_ONLY("Video"),
		AUDIO_VIDEO("Audio and animation");
		
		private final String _uiName;
		
		private AVBlockType(String uiName) {
			_uiName = uiName;
		}
		
		/**
		 * Get the human-readable version of this block type.
		 */
		public String getUIName() {
			return _uiName;
		}
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
