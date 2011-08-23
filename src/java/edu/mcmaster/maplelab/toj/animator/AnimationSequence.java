package edu.mcmaster.maplelab.toj.animator;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.*;

/**
 * This class creates a sequence of frames to be animated
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class AnimationSequence {
// ArrayList of animation frames
	private List<AnimationFrame> _aniFrames;
	//private long _totalAnimationTime;
	
	public AnimationSequence(List<AnimationFrame> aniFrames) {
		_aniFrames = aniFrames;	
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
	public AnimationFrame getFrameAtTime(double time) {
//		System.out.printf("getting frame at time %f\n", time);

		if (time <= getFrameAtIndex(0).getTime()) {
			return getFrameAtIndex(0);
		}
		if (time >= getFrameAtIndex(getNumFrames() - 1).getTime()) {
			return getFrameAtIndex(getNumFrames() - 1);
		}
		
		// get frames
		AnimationFrame frame1 = getFrameAtIndex(0);
		AnimationFrame frame2 = getFrameAtIndex(1);
		for (int i = 0; i < getNumFrames(); i++) {
			double t = getFrameAtIndex(i).getTime();
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
		double alpha = (time - frame1.getTime()) / (frame2.getTime() - frame1.getTime());
		
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
			
			/*
			// alternate method: fade in and out
			if (col1 != null) {
				col = col1;
				if (col2 != null) {
					col.interpolate(col2, alpha);
				}
				else {
					col.scale(1 - alpha);
				}
			}
			else {
				if (col2 != null) {
					col = col2;
					col.scale(alpha);
				}
				else {
					col = null;
				}
			}
			*/
			
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
			
			/*
			 // alternate method: grow and shrink dots
			if (size1 != null) {
				if (size2 != null) {
					size = alpha*size2 + (1 - alpha)*size1;
				}
				else {
					size = (1 - alpha)*size1;
				}
			}
			else {
				if (col2 != null) {
					size = alpha*size2;
				}
				else {
					size = null;
				}
			}
			*/
			
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
			
			/*
			// alternate method: fade in and out
			if (lum1 != null) {
				if (lum2 != null) {
					lum = alpha*lum2 + (1 - alpha)*lum1;
				}
				else {
					lum = (1 - alpha)*lum1;
				}
			}
			else {
				if (lum2 != null) {
					lum = alpha*lum2;
				}
				else {
					lum = null;
				}
			}
			*/
	
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
	public double getTotalAnimationTime() {
		int numFrames = _aniFrames.size();
		AnimationFrame lastFrame = _aniFrames.get(numFrames - 1);
		
		return lastFrame.getTime();
	}
	
	/**
	 * 
	 * @return the time stamp of the frame at which the strike occurs.
	 * This method assumes that the mallet head is always the first dot in the file,
	 * 	and the strike occurs when the mallet head is at its lowest point.
	 */
	public double getStrikeTime() {
		
		AnimationFrame lowestFrame = _aniFrames.get(0);
		double	lowestPt = lowestFrame.getJointLocations().get(0).getLocation().y;
		
		for (AnimationFrame frame: _aniFrames) {
			AnimationDot malletDot = frame.getJointLocations().get(0);
			if (malletDot.getLocation().y < lowestPt) {
				lowestFrame = frame;
				lowestPt = lowestFrame.getJointLocations().get(0).getLocation().y;
			}
		}
		return lowestFrame.getTime();
	}
}
