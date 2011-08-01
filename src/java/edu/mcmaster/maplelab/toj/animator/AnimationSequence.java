package edu.mcmaster.maplelab.toj.animator;
import java.util.List;

/**
 * This class creates a sequence of frames to be animated
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class AnimationSequence {
// ArrayList of animation frames
	private List<AnimationFrame> _aniFrames;
	private long _totalAnimationTime;
	
	public AnimationSequence(List<AnimationFrame> aniFrames) {
		_aniFrames = aniFrames;	
	}

	public AnimationFrame getFrame(int currentFrame) { 
		if(currentFrame < 0 || currentFrame >= getNumFrames()) {
			throw new IllegalArgumentException(String.format("Frame number must be between 0 and %d.", getNumFrames()));
		}
		return _aniFrames.get(currentFrame);
	}

	public int getNumFrames() {
		return _aniFrames.size();
	}

	/**
	 * @return the totalAnimationTime
	 */
	public long getTotalAnimationTime() {
		int numFrames = _aniFrames.size();
		AnimationFrame lastFrame = _aniFrames.get(numFrames - 1);
		
		return (long)lastFrame.getTime();
	}


}
