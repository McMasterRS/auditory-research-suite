package edu.mcmaster.maplelab.av.datamodel;

public enum AVBlockType {
	AUDIO_ONLY("Audio only"),
	VIDEO_ONLY("Video"),
	AUDIO_ANIMATION("Audio and animation") {
		@Override
		public boolean usesAnimation() {
			return true;
		}
	},
	ANIMATION_ONLY("Animation only") {
		@Override
		public boolean usesAnimation() {
			return true;
		}
	};
	
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
	
	/**
	 * Indicate if this type uses animation.
	 */
	public boolean usesAnimation() {
		return false;
	}
}