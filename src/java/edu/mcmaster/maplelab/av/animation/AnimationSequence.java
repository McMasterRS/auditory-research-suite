/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.animation;
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
	/** Aspect ratio of points - for reporting only. */
	private final float _aspect;
	/** Cached values. */
	private AnimationFrame _lowestFrame = null;
	private Rectangle2D.Float _extent = null;
	
	public AnimationSequence(String sourceFileName, List<AnimationFrame> aniFrames, float aspect) {
		_fileName = sourceFileName;
		_aniFrames = aniFrames;	
		_aspect = aspect;
		calculateExtents();
	}
	
	public String getSourceFileName() {
		return _fileName;
	}
	
	public float getPointAspect() {
		return _aspect;
	}

	public AnimationFrame getFrameAtIndex(int currentFrame) { 
		if(currentFrame < 0 || currentFrame >= getNumFrames()) {
			throw new IllegalArgumentException(String.format("Frame number must be between 0 and %d.\n", getNumFrames()));
		}
		return _aniFrames.get(currentFrame);
	}

	/**
	 * Get a frame for the given time, interpolating if necessary.
	 * @param 
	 * @return a new frame using interpolation. time in ms.
	 */
	public AnimationFrame getFrameAtTime(long time) {
		if (time < getFrameAtIndex(0).getTimeInMillis() || 
				time > getFrameAtIndex(getNumFrames()-1).getTimeInMillis()) {
			return null;
		}
		
		// get frames
		AnimationFrame frame1 = null;
		AnimationFrame frame2 = getFrameAtIndex(0);
		for (int i = 1; i < getNumFrames() && time > frame2.getTimeInMillis(); i++) {
			frame1 = getFrameAtIndex(i-1);
			frame2 = getFrameAtIndex(i);
		}
		if (frame2.getTimeInMillis() == time) return frame2;
		
		return interpolate(frame1, frame2, time);
		
	}
	
	/**
	 * Interpolate between the two given frames to produce a new frame at the given time.
	 * @param frame1 the first-occurring frame to interpolate
	 * @param frame2 the second-occurring frame to interpolate
	 * @param time the time for which to interpolate a frame (should fall between the two
	 *             given frames' times)
	 * @return a new, interpolate frame
	 */
	private AnimationFrame interpolate(AnimationFrame frame1, AnimationFrame frame2, long time) {
		double alpha = (time - frame1.getTimeInMillis()) / 
				(frame2.getTimeInMillis() - frame1.getTimeInMillis());
		
		ArrayList<AnimationPoint> dotList = new ArrayList<AnimationPoint>();
		
		// determine luminance
		Double lum = frame1.getLuminance();
		Double lum2 = frame2.getLuminance();
		if (lum == null || lum2 == null) {
			if (alpha > 0.5) lum = lum2;
		}
		else {
			lum = alpha*lum2 + (1 - alpha)*lum;
		}
		
		// interpolate each animation point
		for (int i = 0; i < frame1.getJointLocations().size(); i++) {
			AnimationPoint dot1 = frame1.getJointLocations().get(i);
			AnimationPoint dot2 = frame2.getJointLocations().get(i);
			
			// determine location
			Point2d pt = dot1.getLocation();
			Point2d pt2 = dot2.getLocation();
			if (pt == null || pt2 == null) {
				if (alpha > 0.5) pt = pt2;
			}
			else {
				pt.interpolate(pt2, alpha);
			}
			
			// determine color
			Vector3d col = dot1.getColor();
			Vector3d col2 = dot2.getColor();
			if (col == null || col2 == null) {
				if (alpha > 0.5) col = col2;
			}
			else {
				col.interpolate(col2, alpha);
			}
			
			// determine size
			Double size = dot1.getSize();
			Double size2 = dot2.getSize();
			if (size == null || size2 == null) {
				if (alpha > 0.5) size = size2;
			}
			else {
				size = alpha*size2 + (1 - alpha)*size;
			}
			
			// determine shape
			AnimationShapeDrawable shape = dot1.getShape();
			if (shape != dot2.getShape() && alpha >= 0.5) {
				shape = dot2.getShape();
			}
			
			dotList.add(new AnimationPoint(pt, col, size, shape));
		}
		
		return new AnimationFrame(time, dotList, lum);
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
			List<AnimationPoint> dots = frame.getJointLocations();
			for (int i = 0; i < dots.size(); i++) {
				AnimationPoint dot = dots.get(i);
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
