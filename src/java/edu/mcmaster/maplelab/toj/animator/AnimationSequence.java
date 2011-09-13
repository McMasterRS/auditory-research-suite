/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.animator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.*;

/**
 * This class creates a sequence of frames to be animated
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class AnimationSequence {
	/** Animation extents buffer (each of the 4 sides). */
	private static final float EXTENT_BUFFER = 0.75f;
	/** Frames. */
	private final List<AnimationFrame> _aniFrames;
	/** Source file name. */
	private final String _fileName;
	/** Cached values. */
	private AnimationFrame _lowestFrame = null;
	private Rectangle2D.Float _extent = null;
	
	public AnimationSequence(String sourceFileName, List<AnimationFrame> aniFrames) {
		_fileName = sourceFileName;
		_aniFrames = aniFrames;	
		calculateExtents();
	}
	
	public String getSourceFileName() {
		return _fileName;
	}

	public AnimationFrame getFrameAtIndex(int currentFrame) { 
		if(currentFrame < 0 || currentFrame >= getNumFrames()) {
			throw new IllegalArgumentException(String.format("Frame number must be between 0 and %d.\n", getNumFrames()));
		}
		return _aniFrames.get(currentFrame);
	}

	/**
	 * 
	 * @param 
	 * @return a new frame using interpolation. time in ms.
	 */
	public AnimationFrame getFrameAtTime(long time) {
//		System.out.printf("getting frame at time %f\n", time);

		if (time <= getFrameAtIndex(0).getTimeInMillis()) {
			return getFrameAtIndex(0);
		}
		if (time >= getFrameAtIndex(getNumFrames() - 1).getTimeInMillis()) {
			return getFrameAtIndex(getNumFrames() - 1);
		}
		
		// get frames
		AnimationFrame frame1 = getFrameAtIndex(0);
		AnimationFrame frame2 = getFrameAtIndex(1);
		for (int i = 0; i < getNumFrames(); i++) {
			double t = getFrameAtIndex(i).getTimeInMillis();
			if (t >= time) {
				if (t == time) {
//					System.out.printf("animating frame %d\n", i);
					return getFrameAtIndex(i);
				}
				else {
					frame2 = getFrameAtIndex(i);
					frame1 = getFrameAtIndex(i-1);
//					System.out.printf("interpolating b/t frames %d and %d\n", i-1, i);
					break;
				}
			}
		}
		double alpha = (time - frame1.getTimeInMillis()) / (frame2.getTimeInMillis() - frame1.getTimeInMillis());
		
		ArrayList<AnimationDot> dotList = new ArrayList<AnimationDot>();
		
		for (int i = 0; i < frame1.getJointLocations().size(); i++) {
			AnimationDot dot1 = frame1.getJointLocations().get(i);
			AnimationDot dot2 = frame2.getJointLocations().get(i);
			
			// get location
			Point2d pt1 = dot1.getLocation();
			Point2d pt2 = dot2.getLocation();
			Point2d pt = pt1;
			if ((pt1 == null) || (pt2 == null)) {
				pt = null;
			}
			else {
				pt.interpolate(pt2, alpha);
			}
			
			// get color
			Vector3d col1 = dot1.getColor();
			Vector3d col2 = dot2.getColor();
			Vector3d col;

			if ((col1 == null) || (col2 == null)) {
				if (alpha < 0.5) {
					col = col1;
				}
				else {
					col = col2;
				}
			}
			else {
				col = col1;
				col.interpolate(col2, alpha);
			}
			
			// get size
			Double size1 = dot1.getSize();
			Double size2 = dot2.getSize();
			Double size;
			
			if ((size1 == null) || (size2 == null)) {
				if (alpha < 0.5) {
					size = size1;
				}
				else {
					size = size2;
				}
			}
			else {
				size = alpha*size2 + (1 - alpha)*size1;
			}
			
			// get luminance
			Double lum1 = dot1.getLuminance();
			Double lum2 = dot2.getLuminance();
			Double lum;
			
			if ((lum1 == null) || (lum2 == null)) {
				if (alpha < 0.5) {
					lum = lum1;
				}
				else {
					lum = lum2;
				}
			}
			else {
				lum = alpha*lum2 + (1 - alpha)*lum1;
			}
			
			AnimationDot dot = new AnimationDot(pt, col, size, lum);
//			dot.printDescription();
			dotList.add(dot);
		}
		AnimationFrame frame = new AnimationFrame(time, dotList);
		return frame;
	}

	public int getNumFrames() {
		return _aniFrames.size();
	}

	/**
	 * @return the totalAnimationTime
	 */
	public long getTotalAnimationTime() {
		int numFrames = _aniFrames.size();
		if (numFrames == 0) return 0;
		
		AnimationFrame lastFrame = _aniFrames.get(numFrames - 1);
		
		return lastFrame.getTimeInMillis();
	}
	
	/**
	 * Get the time stamp of the frame at which the strike occurs.
	 */
	public long getStrikeTime() {
		if (_lowestFrame == null) calculateExtents();
		return _lowestFrame != null ? _lowestFrame.getTimeInMillis() : 0;
	}
	
	/**
	 * Get the extent (bounds) of the animation sequence.  IMPORTANT: The
	 * (x,y) coords of the returned rectangle correspond to the LOWER LEFT
	 * corner of the rectangle.
	 */
	public Rectangle2D.Float getExtents() {
		if (_extent == null) calculateExtents();
		return _extent != null ? 
				new Rectangle2D.Float(_extent.x, _extent.y, _extent.width, _extent.height) : null;
	}
	
	/**
	 * Calculate the extent (bounds) of the animation sequence, and
	 * the lowest location/frame of the mallet head, assumed to be the
	 * "strike" of the mallet.  The mallet head is assumed to be the 
	 * first dot.
	 */
	private void calculateExtents() {
		if (_aniFrames.size() == 0) return;
		
		_extent = new Rectangle2D.Float(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 
				Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		
		for (AnimationFrame frame : _aniFrames) {
			List<AnimationDot> dots = frame.getJointLocations();
			for (int i = 0; i < dots.size(); i++) {
				AnimationDot dot = dots.get(i);
				Point2d loc = dot.getLocation();
				if (loc == null) continue;
				
				if (loc.x < _extent.x) {
					_extent.x = (float) loc.x;
				}
				if (loc.y < _extent.y) {
					// only consider first dot for lowest frame
					if (i == 0) _lowestFrame = frame;
					_extent.y = (float) loc.y;
				}
				if (loc.x > _extent.width) {
					_extent.width = (float) loc.x;
				}
				if (loc.y > _extent.height) {
					_extent.height = (float) loc.y;
				}
			}
		}
		
		// width and height initially store absolute coordinates,
		// but they should be relative values
		_extent.width = _extent.width - _extent.x;
		_extent.height = _extent.height - _extent.y;
		
		// make the rectangle square by choosing max
		// of the two dimensions and re-centering
		if (_extent.width > _extent.height) {
			float halfDiff = (_extent.width - _extent.height) * .5f;
			_extent.height = _extent.width;
			_extent.y -= halfDiff;
		}
		else {
			float halfDiff = (_extent.height - _extent.width) * .5f;
			_extent.width = _extent.height;
			_extent.x -= halfDiff;
		}
		
		// add buffers and re-center
		_extent.width += 2*EXTENT_BUFFER;
		_extent.height += 2*EXTENT_BUFFER;
		_extent.x -= EXTENT_BUFFER;
		_extent.y -= EXTENT_BUFFER;
	}
}
