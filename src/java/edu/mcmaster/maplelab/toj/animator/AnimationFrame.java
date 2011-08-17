package edu.mcmaster.maplelab.toj.animator;

import java.util.Collections;
import java.util.List;

public class AnimationFrame {
	// each frame contains one of the rows in the data file

	private List<AnimationDot> _pointList;
	private final double _time;

	public AnimationFrame(double time, List<AnimationDot> pointList) {
		this._time = 1000*time;					// time to animate the frame (in milliseconds)
		_pointList = pointList;
	}

	/** Provides an unmodifiable ordered list of joint locations. */
	//create object AnimatedDot
	// Point2d for loc, vector3d for color, ? luminence, size (double)
	public List<AnimationDot> getJointLocations() {	
		return Collections.unmodifiableList(_pointList);
	}

	/**
	 * @return The time frame should be rendered after start of animation.
	 */
	public double getTime() {
		return _time;
	}
}
