/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.media.animation;

import static javax.media.opengl.GL2.*;

import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point2f;

import edu.mcmaster.maplelab.av.TimeTracker;

/**
 * AnimationRenderer animates an AnimationSequence.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimationRenderer implements GLEventListener { 
	public static final String TIMESTAMPS = "ANIMATION_TIMESTAMPS";
	
	private AnimationSource _source = null;
	
	private long _startTime;				// ms
	private boolean _animatedOnce = true; // set to true when 1 stroke is animated
	private boolean _extentsDirty = true;
	private Point _lastLoc = null;
	
	private ArrayList<AnimationListener> _listeners;

	/**
	 * Constructor.
	 */
	public AnimationRenderer() {
	}

	/** Set the current animation source. Doing so implies starting at the first frame. */
	public void setAnimationSource(AnimationSource source) {
		_extentsDirty = _source != source;
		_source = source;
	}

	@Override
	public void init(GLAutoDrawable canvas) {
		GL2 gl = canvas.getGL().getGL2();

		gl.glShadeModel(GL_SMOOTH);                            //Enables Smooth Color Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               //This Will Clear The Background Color To Black
		gl.glClearDepth(1.0);                                  //Enables Clearing Of The Depth Buffer
		gl.glEnable(GL_DEPTH_TEST);                            //Enables Depth Testing
		gl.glDepthFunc(GL_LEQUAL);                             //The Type Of Depth Test To Do
		gl.glEnable(GL_LINE_SMOOTH);
		gl.glEnable(GL_BLEND);
		gl.glLineWidth(1.5f);
		gl.glEnable(GL_POLYGON_SMOOTH);
	}


	@Override
	public void display(GLAutoDrawable d) {
		//TimeTracker.timeStamp(TIMESTAMPS);
		
		// have to force reshape on animation change
		if (_extentsDirty && _lastLoc != null) {
			reshape(d, _lastLoc.x, _lastLoc.y, d.getWidth(), d.getHeight());
		}
//		GL2 gl = new DebugGL2((GL2) d.getGL());
		GL2 gl = d.getGL().getGL2();
		
		gl.glPopAttrib();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);       //Clear The Screen And The Depth Buffer
		gl.glDisable(GL_DEPTH_TEST);
		
		if(_source == null || _animatedOnce == true) {
			return;
		}

		gl.glMatrixMode(GL_MODELVIEW);                          // Make sure we're in the correct matrix mode
		gl.glLoadIdentity();                                    //Reset The View
		
		AnimationSequence as = _source.getAnimationSequence();
		long currentTime = (System.currentTimeMillis() - getStartTime());			// animate only once
		
		if (currentTime > as.getTotalAnimationTime()) {
			_animatedOnce = true;
		}
		displayFrame(gl, as.getFrameAtTime(currentTime));
		
		if (_animatedOnce && _listeners != null) {
			for (AnimationListener al : _listeners) {
				al.animationDone();
			}
		}
	} 

	// display 1 frame
	private void displayFrame(GL2 gl, AnimationFrame frame) {
		if (frame == null) return;

		// TODO: Can the disk can be instantiated in the init method?
		GLU glu = new GLU();
		AnimationShapeDrawable.initialize(gl, glu);
		float radius = _source.getDiskRadius();

		Double luminance = frame.getLuminance();
		List<AnimationPoint> jointLocations = frame.getJointLocations();
		
		// draw connecting lines
		gl.glColor3f(1f, 1f, 1f);
		
		if (_source.isConnected()) {
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(GL_LINE_STRIP);

			for (int j = 0; j < jointLocations.size(); j++) {
				if (j < _source.getNumPoints()) {
					AnimationPoint dot = jointLocations.get(j);
					
					if (dot.getLocation() != null) {			
						gl.glVertex2d(dot.getLocation().x, dot.getLocation().y);
					}
				}
			} 
			gl.glEnd();
		}
		
		// draw dots
		for (int i = 0; i < jointLocations.size(); i++) {
			
			if (i < _source.getNumPoints()) {
				AnimationPoint dot = jointLocations.get(i);
				
				if (dot.getLocation() == null) {
					continue;
				}
				
				dot.getShape().draw(gl, glu, dot, luminance, radius);
			}
		}			        
		
		AnimationShapeDrawable.cleanup(gl, glu);
	}

	@Override
	public void reshape(GLAutoDrawable canvas, int x, int y, int width, int height) {
		_lastLoc = new Point(x, y);
		if (height <= 0) height=1; // fix 0 val

		GL2 gl = (GL2) canvas.getGL();
		GLU glu = new GLU();
		
		// set viewport
		gl.glViewport(x, y, width, height);
		
		// reset projection
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		
		// calculate new ortho projection
		AnimationSequence as = _source != null ? _source.getAnimationSequence() : null;
		Rectangle2D.Float r = as != null ? as.getExtents() : null;
		
		// find center, width, height
		Point2f rCenter = null;
		float rWidth, rHeight;
		if (r != null) {
			rWidth = r.width;
			rHeight = r.height;
			rCenter = new Point2f(r.x + .5f*r.width, r.y + .5f*r.height);
		}
		else {
			rWidth = 5;
			rHeight = 5;
			rCenter = new Point2f(2.5f, 2.5f);
		}
		
		// scale width or height for aspect
		float aspect = Float.valueOf(width) / Float.valueOf(height);
		if (aspect < 1.0f) { 
			rHeight /= aspect;
		}
		else { 
			rWidth *= aspect;
		}
		
		// change these parameters to offsets from center (akin to radius)
		rWidth *= .5f;
		rHeight *= .5f;
		
		// set ortho projection using center and offsets
		glu.gluOrtho2D(rCenter.x - rWidth, rCenter.x + rWidth, rCenter.y - rHeight, rCenter.y + rHeight);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();   
	}

	public long	getStartTime() {
		return _startTime;
	}

	public void setStartTime(long startTime) {
		_startTime = startTime;
		_animatedOnce = false;
	}
	
	/**
	 * Add an animation listener.
	 */
	public void addAnimationListener(AnimationListener listener) {
		if (_listeners == null) _listeners = new ArrayList<AnimationListener>();
		_listeners.add(listener);
	}
	
	/**
	 * Remove an animation listener.
	 */
	public void removeAnimationListener(AnimationListener listener) {
		if (_listeners != null) _listeners.remove(listener);
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
}