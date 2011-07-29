package edu.mcmaster.maplelab.toj.animator;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

public class AnimationFrame {
	// each frame contains one of the rows in the data file

	private List<Point2D> _pointList;
	private final double _time;

	public AnimationFrame(double time, List<Point2D> pointList) {
		this._time = time;
		_pointList = pointList;
	}

	/** Provides an unmodifiable ordered list of joint locations. */
	public List<Point2D> getJointLocations() {
//		List<Point2D> retval = new ArrayList<Point2D>();
//		retval.add(new Point2D.Float(5.611f,	2.771f));
//		retval.add(new Point2D.Float(4.403f,	2.833f));	
//		retval.add(new Point2D.Float(4.125f,	4.875f));	
//		retval.add(new Point2D.Float(2.419f,	5.088f));
////		retval.add(new Point2D.Float(0f,		0f));	
		return Collections.unmodifiableList(_pointList);
	}

	/**
	 * @return The time frame should be rendered after start of animation.
	 */
	public double getTime() {
		return _time;
	}
}
