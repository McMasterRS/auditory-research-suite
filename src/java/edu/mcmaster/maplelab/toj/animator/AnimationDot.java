package edu.mcmaster.maplelab.toj.animator;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;

public class AnimationDot {
	private Point2d _location;
	private Vector3d _color;
	private Double _size;
	private Double _luminance;
	
	public AnimationDot(Point2d location, Vector3d color, Double size, Double luminance) {
		_location = location;
		_color = color;
		_size = size;
		_luminance = luminance;
	}
	
	public AnimationDot(Point2d location) {
		this(location, new Vector3d (1, 1, 1), 0.3, 1d);
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
	
	public Double getLuminance () {
		return _luminance;
	}
	
	public void printDescription() {
		String loc = getLocation() != null ? String.format("(%.2f,%.2f)", getLocation().x, getLocation().y) : "null";
		String col = getColor() != null ? String.format("(%.2f, %.2f, %.2f)", getColor().x, getColor().y, getColor().z) : "null";
		String size = getSize() != null ? String.format("%.2f", getSize()) : "null";
		String lum = getLuminance() != null? String.format("%.2f", getLuminance()) : "null";
		
		System.out.printf("\t AnimationDot:  location: %s, color: %s, size: %s, luminance: %s\n", 
				loc, col, size, lum);
	}
}
