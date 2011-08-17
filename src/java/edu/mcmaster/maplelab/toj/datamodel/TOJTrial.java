package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.Trial;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;

public class TOJTrial extends Trial<Response> {
	
	private final Playable _audio;
	private final boolean _isVideo;
//	private final File _videoFile;
//	private final File _audioFile;
//	private final File _animationFile;
	private final float _offset;
	private final int _numPoints;
	
	private final float _diskRadius;
	private final AnimationSequence _animationSequence;

	
	public TOJTrial(AnimationSequence animationSequence, boolean isVideo, Playable audio, Float timingOffset, int animationPoints, float diskRadius) {
		
		_animationSequence = animationSequence;
		_isVideo = isVideo;
		_offset = timingOffset;
		_numPoints = animationPoints;
		_audio = audio;
		
		_diskRadius = diskRadius;
		
		//TODO: determine files
//		_videoFile = null;
//		_audioFile = null;
//		_animationFile = null;
	}

	//getters

	public boolean isVideo() {
		return _isVideo;
	}
	public float getOffset() {
		return _offset;
	}
	public int getNumPoints() {
		return _numPoints;
	}
	
	public AnimationSequence getAnimation() {
		return getAnimationSequence();
	}

	
	// return a useful string to test creation of TOJTrial
	public void printDescription() {
		System.out.printf("TOJTrial:	%d frames, isVideo: %b, " +
				"Playable name: %s, timingOffset: %f, animationPoints: %d, diskRadius: %f\n",
				getAnimationSequence().getNumFrames(), _isVideo, _audio.name(), _offset, _numPoints, _diskRadius);
	}
	
	/**
	 * Get the radius of the points to render for each joint.
	 */
	public float getDiskRadius() {
		return _diskRadius;
	}

	/**
	 * @return the _animationSequence
	 */
	public AnimationSequence getAnimationSequence() {
		return _animationSequence;
	}
	
	public Playable getPlayable() {
		return _audio;
	}
}
