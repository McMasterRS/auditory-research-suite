package edu.mcmaster.maplelab.toj.animator;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * This class creates a sequence of frames to be animated
 * @author Catherine
 *
 */
public class AnimationSequence {
// ArrayList of animation frames
	ArrayList<AnimationFrame> _aniFrames;
	
	public AnimationSequence() {
		System.out.println("AnimationSequence constructor called");
	
	}
	
	public void display(AnimationSequence aniSequence) {
			
	}

	public AnimationFrame getFrame(int currentFrame) { // get first frame
		return _aniFrames.get(currentFrame);
		
		//return new AnimationFrame(0);
	}
}
