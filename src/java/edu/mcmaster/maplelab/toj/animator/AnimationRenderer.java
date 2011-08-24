/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.animator;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_LINE_STRIP;
import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_PROJECTION;
import static javax.media.opengl.GL.GL_SMOOTH;

import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * AnimationRenderer animates an AnimationSequence.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimationRenderer implements GLEventListener { 

	// fields
	private boolean _connectTheDots = true;
	private TOJTrial _trial = null;

	private int _currentFrame = 0;  
	private long _startTime;				// ms
	private boolean _animatedOnce = false; // set to true when 1 stroke is animated

	// constructor
	public AnimationRenderer(boolean connectTheDots) {
		_connectTheDots = connectTheDots;
	}

	/** Set the current trial to animate. Doing so implies starting at the first frame. */
	public void setTrial(TOJTrial trial) {
		_trial = trial;
		_currentFrame = 0;
		_animatedOnce = false;
	}

	public void setCurrentFrame(int frameNum) {
		if(_trial == null) {
			throw new IllegalStateException("Must have an active trial to set the current frame.");
		}
		final int numFrames = _trial.getAnimationSequence().getNumFrames();
		if(frameNum < 0 || frameNum > numFrames) {
			throw new IllegalArgumentException(String.format("Frame number must be between 0 and %d", numFrames - 1));
		}
		_currentFrame = frameNum;
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
		gl.glTranslatef(-6.6f,-4.5f,-8.0f);						// TODO: translate to (roughly) origin
		
		int totalFrames = _trial.getAnimationSequence().getNumFrames();

		long currentTime = (System.currentTimeMillis() - getStartTime());			// animate only once
		
		if (currentTime > _trial.getAnimationSequence().getFrameAtIndex(totalFrames - 1).getTime()) {
			_animatedOnce = true;
		}
		displayFrame(gl, _trial.getAnimationSequence().getFrameAtTime(currentTime));
	
	} 

	// display 1 frame
	public void displayFrame(GL gl, AnimationFrame frame) {

		if(_trial != null) {
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
	}

	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
	}

	@Override
	public void reshape(GLAutoDrawable canvas, int x, int y, int width,
			int height) {
		GL gl = canvas.getGL();
		GLU glu = new GLU();

		if(height==0)height=1;
		gl.glViewport(0, 0, width, height);                       // Reset The Current Viewport And Perspective Transformation
		gl.glMatrixMode(GL_PROJECTION);                           // Select The Projection Matrix
		gl.glLoadIdentity();                                      // Reset The Projection Matrix
//		glu.gluPerspective(45.0f, width / height, 0.1f, 100.0f);  // Calculate The Aspect Ratio Of The Window
		glu.gluPerspective(45.0f, 1.3, 0.1f, 100.0f);  			// Calculate The Aspect Ratio Of The Window

//		gl.glOrtho(-10, 10, -10, 10, -1, 100);
		
		gl.glMatrixMode(GL_MODELVIEW);                            // Select The Modelview Matrix
		gl.glLoadIdentity();   
	}

	public long	getStartTime() {
		return _startTime;
	}

	public void setStartTime(long _startTime) {
		this._startTime = _startTime;
	}
}