package edu.mcmaster.maplelab.toj.animator;

import java.awt.geom.Point2D;
import java.util.*;
import java.io.File;

public class AnimationFrame {
	// each frame contains one of the rows in the data file

	List<Point2D> _pointList;
	private final double _time;

	public AnimationFrame(double time) {
		this._time = time;
		System.out.printf("AnimationFrame constructor called\n");

	}

	public List<Point2D> getJointLocations() {
		List<Point2D> retval = new ArrayList<Point2D>();
		retval.add(new Point2D.Float(5.611f,	2.771f));
		retval.add(new Point2D.Float(4.403f,	2.833f));	
		retval.add(new Point2D.Float(4.125f,	4.875f));	
		retval.add(new Point2D.Float(2.419f,	5.088f));
//		retval.add(new Point2D.Float(0f,		0f));	
		return retval;
	}
}
