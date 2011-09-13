/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.animator;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_LINE_STRIP;
import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_PROJECTION;
import static javax.media.opengl.GL.GL_SMOOTH;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Point2f;

import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * AnimationRenderer animates an AnimationSequence.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimationRenderer implements GLEventListener { 
	private TOJTrial _trial = null;
	private boolean _connectTheDots = true;
	
	private long _startTime;				// ms
	private boolean _animatedOnce = true; // set to true when 1 stroke is animated
	
	private ArrayList<AnimationListener> _listeners;

	/**
	 * Constructor.
	 */
	public AnimationRenderer(boolean connectTheDots) {
		_connectTheDots = connectTheDots;
	}

	/** Set the current trial to animate. Doing so implies starting at the first frame. */
	public void setTrial(TOJTrial trial) {
		_trial = trial;
	}

	@Override
	public void init(GLAutoDrawable canvas) {

		if (!(canvas instanceof GLAutoDrawable)) {
			throw new IllegalArgumentException("canvas must be type GLAutoDrawable");
		}

		GL gl = canvas.getGL();

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
		
		GL gl = d.getGL();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);       //Clear The Screen And The Depth Buffer
		gl.glDisable(GL_DEPTH_TEST);
		
		if(_trial == null || _animatedOnce == true) {
			return;
		}

		gl.glMatrixMode(GL_MODELVIEW);                          // Make sure we're in the correct matrix mode
		gl.glLoadIdentity();                                    //Reset The View
		
		int totalFrames = _trial.getAnimationSequence().getNumFrames();

		long currentTime = (System.currentTimeMillis() - getStartTime());			// animate only once
		
		if (currentTime > _trial.getAnimationSequence().getFrameAtIndex(totalFrames - 1).getTimeInMillis()) {
			_animatedOnce = true;
		}
		displayFrame(gl, _trial.getAnimationSequence().getFrameAtTime(currentTime));
		
		if (_animatedOnce && _listeners != null) {
			for (AnimationListener al : _listeners) {
				al.animationDone();
			}
		}
	} 

	// display 1 frame
	private void displayFrame(GL gl, AnimationFrame frame) {

		// TODO: Can the disk can be instantiated in the init method?
		GLU glu = new GLU();
		GLUquadric circle = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(circle, GLU.GLU_FILL);
		glu.gluQuadricNormals(circle, GLU.GLU_FLAT);
		glu.gluQuadricOrientation(circle, GLU.GLU_OUTSIDE);

		List<AnimationDot> jointLocations = frame.getJointLocations();
		
		// draw connecting lines
		gl.glColor3f(1f, 1f, 1f);
		
		if (_connectTheDots) {
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(GL_LINE_STRIP);

			for (int j = 0; j < jointLocations.size(); j++) {
				if (j < _trial.getNumPoints()) {
					AnimationDot dot = jointLocations.get(j);
					
					if (dot.getLocation() != null) {			
						gl.glVertex2d(dot.getLocation().x, dot.getLocation().y);
					}
				}
			} 
			gl.glEnd();
		}
		
		// draw dots
		for (int i = 0; i < jointLocations.size(); i++) {
			
			if (i < _trial.getNumPoints()) {
				AnimationDot dot = jointLocations.get(i);
				
				if (dot.getLocation() == null) {
					continue;
				}
				
				gl.glPushMatrix();
				
				if (dot.getLocation() != null) {			
					gl.glTranslated(dot.getLocation().x, dot.getLocation().y, 0);						
				}
				
				// color each dot individually
				if (dot.getColor() != null) {
					if (dot.getLuminance() != null) {
						double lum = dot.getLuminance();
						gl.glColor3d(lum*dot.getColor().x, lum*dot.getColor().y, lum*dot.getColor().z);
					}
					else {
						gl.glColor3d(dot.getColor().x, dot.getColor().y, dot.getColor().z);
					}
				}
				
				// get size of each dot
				Double rad = 1.0;
				if (dot.getSize() != null) {
					rad = dot.getSize();
					gl.glScaled(rad, rad, rad);
				}

				final int slices = 32;
				final int stacks = 32;
									
				// draw sphere
				glu.gluSphere(circle, 0.1, slices, stacks); 
				gl.glPopMatrix(); 
			}
		}			        
		glu.gluDeleteQuadric(circle);
	}

	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
	}

	@Override
	public void reshape(GLAutoDrawable canvas, int x, int y, int width,
			int height) {
		if (height==0) height=1; // fix 0 val
		
		GL gl = canvas.getGL();
		GLU glu = new GLU();
		
		// set viewport
		gl.glViewport(x, y, width, height);
		
		// reset projection
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		
		// calculate new ortho projection
		AnimationSequence as = _trial != null ? _trial.getAnimationSequence() : null;
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
}