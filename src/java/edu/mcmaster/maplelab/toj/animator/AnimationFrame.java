/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.animator;

import java.util.Collections;
import java.util.List;

/**
 * AnimationFrame represents one frame in the animation.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimationFrame {
	// each frame contains one of the rows in the data file

	private List<AnimationDot> _pointList;
	private final double _time;

	public AnimationFrame(double time, List<AnimationDot> pointList) {
		this._time = 1000*time;				// time to animate the frame (in ms)
		_pointList = pointList;
	}

	/** Provides an unmodifiable ordered list of joint locations. */
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
