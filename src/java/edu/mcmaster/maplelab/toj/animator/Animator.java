package edu.mcmaster.maplelab.toj.animator;

import java.awt.geom.Point2D;
import java.sql.Time;
import static javax.media.opengl.GL.*;

import javax.media.opengl.*;
import static javax.media.opengl.glu.GLU.*;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;


import com.sun.opengl.impl.GLUquadricImpl;

import edu.mcmaster.maplelab.common.gui.ExperimentFrame;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

public class Animator implements GLEventListener {

	// fields
	
	// Object _finishedCallback; // add later
	ExperimentFrame _frame;
	Float _diskSize;
	Boolean _connectTheDots;
	// trial  - what does this map to?
	TOJTrial _trial;
	Renderer _renderer;
	
	float _aspectRatio;
	// renderer ?
	// canvas ?
	GLAutoDrawable _canvas;
	
	// constructor
	public Animator(float diskSize, Boolean connectTheDots) {
		_frame = null;
		_diskSize = diskSize;
		_connectTheDots = connectTheDots;
		
		_trial = null; // ?
		
		_aspectRatio = 1;
		// renderer = Renderer(self.diskSize)
		_renderer = new Renderer(_diskSize);
	}

	void setTrial(TOJTrial trial) {
		_trial = trial;
	}
	
	@Override
	public void init(GLAutoDrawable canvas) {
		// assuming [GLAutodrawable canvas] is the analog of [PTCommon.GLUtils.PT3DCanvas canvas]
		if (!(canvas instanceof GLAutoDrawable)) {
			throw new IllegalArgumentException("canvas must be type GLAutoDrawable");
		}
		_canvas = canvas;
		
		GL gl = canvas.getGL();
		
		gl.glClearColor(0, 0, 0, 1); // glblack
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL_LINE_SMOOTH);
	}

	
	@Override
	public void display(GLAutoDrawable d) {
		GL gl = d.getGL();
	
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		if (_trial != null) {
//			elapsedTime = time.time() - self.startTime;
//			if self.trial.isDone(elapsedTime) {
//				self.canvas.AnimatesStop();
//				if (self.finishedCallback != null)
//					self.finishedCallback();
//				return;
//			}
//			else {
//				// TBC...
//			}
		}
	}
	
	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		// maps to method "resize" in python doc?
		
		
	}


	@Override
	public void reshape(GLAutoDrawable canvas, int left, int right, int bottom,
			int top) {
		GL gl = canvas.getGL();
		
		
		gl.glMatrixMode(GL_PROJECTION); // missing library
//		gl.glOrtho2D(0, width, 0, height);
		gl.glMatrixMode(GL_MODELVIEW);	
		
	}
	
	// where to put this?
	private class Renderer {
		// fields
		Point2D _screenSize;
		Float _diskSize;
		float _aspectRatio;
		Boolean _init;
		Boolean _connectTheDots;
		
		// from initDisk method
		int _diskList;
		// from scale method
		float _xScale;
		float _yScale;
		
		// constructor
		public Renderer (Float diskSize) {
			_screenSize = new Point2D.Float(10, 10);
			_diskSize = diskSize;
			_aspectRatio = 1;
			_init = false;
			_connectTheDots = true;
		}
		
		void initDisk() {
			GL gl = _canvas.getGL();
			GLU glu = new GLU();
			
			GLUquadric quad = glu.gluNewQuadric();
			glu.gluQuadricDrawStyle(quad, GLU_FILL);
			
			// construct representative disk
			_diskList = gl.glGenLists(1);
			
			gl.glNewList(_diskList, GL_COMPILE);
			glu.gluDisk(quad, 0, _diskSize, 50, 1);
			gl.glEndList();
			
			glu.gluDeleteQuadric(quad);
			_init = true;
		}
		void scale(GL gl) {
			_xScale = (float) (_screenSize.getX()/10);
			_yScale = _xScale /_aspectRatio;
		//	gl.glScale(_xScale, _yScale, 0);
		}
		
	}
}

