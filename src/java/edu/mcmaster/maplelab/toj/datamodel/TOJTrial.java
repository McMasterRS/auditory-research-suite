package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.Trial;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;

public class TOJTrial extends Trial<Response> {
	private final NotesEnum _pitch;
	private final boolean _isVideo;
//	private final File _videoFile;
//	private final File _audioFile;
//	private final File _animationFile;
	private final int _offset;
	private final int _numPoints;
	
	private final float _diskRadius;
	//public Point2D.Float _diskLocation;
	private final AnimationSequence _animationSequence;
	
	public TOJTrial(AnimationSequence animationSequence, NotesEnum pitch, boolean isVideo, DurationEnum toneDuration, 
			DurationEnum strikeDuration, int timingOffset, int animationPoints, float diskRadius) {
		_animationSequence = animationSequence;
		_pitch = pitch;
		_isVideo = isVideo;
		_offset = timingOffset;
		_numPoints = animationPoints;
		
		_diskRadius = diskRadius;
		
		//TODO: determine files
//		_videoFile = null;
//		_audioFile = null;
//		_animationFile = null;
	}
	
	//getters
	public NotesEnum getPitch() {
		return _pitch;
	}
	public boolean isVideo() {
		return _isVideo;
	}
	public int getOffset() {
		return _offset;
	}
	public int getNumPoints() {
		return _numPoints;
	}
	
	public AnimationSequence getAnimation() {
		return _animationSequence;
	}

	/**
	 * Get the radius of the points to render for each joint.
	 */
	public float getDiskRadius() {
		return _diskRadius;
	}
}
