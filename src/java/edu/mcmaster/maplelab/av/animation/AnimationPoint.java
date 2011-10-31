/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.animation;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
/**
 * This class represents one animation point (joint, in most cases) in the image.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimationPoint {
	private final Point2d _location;
	private final Vector3d _color;
	private final Double _size;
	private final AnimationShapeDrawable _shape;
	
	public AnimationPoint(Point2d location, Vector3d color, Double size) {
		this(location, color, size, AnimationShapeDrawable.DOT);
	}
	
	public AnimationPoint(Point2d location, Vector3d color, Double size, 
			AnimationShapeDrawable shape) {
		_location = location;
		_color = color;
		_size = size;
		_shape = shape != null ? shape : AnimationShapeDrawable.DOT;
	}
	
	public Point2d getLocation () {
		return _location;
	}
	
	public Vector3d getColor () {
		return _color;
	}
	
	public Double getSize () {
		return _size;
	}
	
	public AnimationShapeDrawable getShape () {
		return _shape;
	}
	
	/**
	 * Get a detailed description of the point.
	 */
	public String getDescription() {
		String loc = getLocation() != null ? String.format("(%.2f,%.2f)", getLocation().x, getLocation().y) : "null";
		String col = getColor() != null ? String.format("(%.2f, %.2f, %.2f)", getColor().x, getColor().y, getColor().z) : "null";
		String size = getSize() != null ? String.format("%.2f", getSize()) : "null";
		String shape = getShape().name();
		
		return String.format("\t AnimationPoint:  location: %s, color: %s, " +
				"size: %s, shape: %s\n", 
				loc, col, size, shape);
	}
}
