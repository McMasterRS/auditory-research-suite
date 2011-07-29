package edu.mcmaster.maplelab.toj.animator;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_LINE_STRIP;
import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_PROJECTION;
import static javax.media.opengl.GL.GL_SMOOTH;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Level;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

public class AnimationRenderer implements GLEventListener { 


	// fields
	private boolean _connectTheDots = true;
	private TOJTrial _trial = null;

	private float _aspectRatio = 1f;

	private int _currentFrame = 0; // pass these into displayFrame

	// constructor
	public AnimationRenderer(boolean connectTheDots) {
		_connectTheDots = connectTheDots;

		_aspectRatio = 1;
	}

	/** Set the current trial to animate. Doing so implies starting at the first frame. */
	public void setTrial(TOJTrial trial) {
		_trial = trial;
		_currentFrame = 0;
	}

	public void setCurrentFrame(int frameNum) {
		if(_trial == null) {
			throw new IllegalStateException("Must have an active trial to set the current frame.");
		}

		final int numFrames = _trial.getAnimation().getNumFrames();
		if(frameNum < 0 || frameNum > numFrames) {
			throw new IllegalArgumentException(String.format("Frame number must be between 0 and %d", numFrames));
		}
		_currentFrame = frameNum;
	}

	@Override
	public void init(GLAutoDrawable canvas) {
		// assuming [GLAutodrawable canvas] is the analog of [PTCommon.GLUtils.PT3DCanvas canvas]
		System.out.println("init in Animator class called");

		if (!(canvas instanceof GLAutoDrawable)) {
			throw new IllegalArgumentException("canvas must be type GLAutoDrawable");
		}

		GL gl = canvas.getGL();


		gl.glShadeModel(GL_SMOOTH);                            //Enables Smooth Color Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               //This Will Clear The Background Color To Black
		gl.glClearDepth(1.0);                                  //Enables Clearing Of The Depth Buffer
		gl.glEnable(GL_DEPTH_TEST);                            //Enables Depth Testing
		gl.glDepthFunc(GL_LEQUAL);                             //The Type Of Depth Test To Do

		//  	gl.glTranslatef(-6.6f,-4.5f,-8.0f);						// translate to (roughly) origin (fix later)
		//  	gl.glTranslatef(-6.6f,-4.5f,-8.0f);						// translate to (roughly) origin (fix later)

	}



	//new myRunnable class
	//	private class myRunnable implements Runnable {
	//		public void run() {
	//			System.out.println("run from myRunnable class called");
	//			displayFrame(_gl, _currentFrame);
	//			System.out.printf("from run: number of pts in current frame = %d\n", _currentFrame._pointList.size());
	//
	//		}
	//	}


	@Override
	public void display(GLAutoDrawable d) {
System.currentTimeMillis();
		GL gl = d.getGL();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);       //Clear The Screen And The Depth Buffer

		if(_trial == null) {
			return;
		}

		gl.glMatrixMode(GL_MODELVIEW);                               // Make sure we're in the correct matrix mode
		gl.glLoadIdentity();                                         //Reset The View
		gl.glTranslatef(-6.6f,-4.5f,-8.0f);						// TODO: translate to (roughly) origin (fix later)


		AnimationFrame frame = _trial.getAnimation().getFrame(_currentFrame);
		displayFrame(gl, frame); // test displayFrame method
		_currentFrame = (_currentFrame + 1) % _trial.getAnimation().getNumFrames();
				
	} 

	// display 1 frame
	public void displayFrame(GL gl, AnimationFrame frame) {

		if(_trial != null) {
			//GL gl = d.getGL();

			// draw white circle
			// TODO: Can the disk can be instantiated in the init method?
			GLU glu = new GLU();
			GLUquadric circle = glu.gluNewQuadric();
			glu.gluQuadricDrawStyle(circle, GLU.GLU_FILL);
			glu.gluQuadricNormals(circle, GLU.GLU_FLAT);
			glu.gluQuadricOrientation(circle, GLU.GLU_OUTSIDE);
			gl.glColor3f(1f, 1f, 1f);

			//List<Point2D> points = currFrame.getJointLocations();
			List<Point2D> jointLocations = frame.getJointLocations();
			for(Point2D p : jointLocations) {
				gl.glPushMatrix();
				// Draw sphere (possible styles: FILL, LINE, POINT).
				gl.glTranslated(p.getX(),p.getY(), 0.0);						

				float radius = _trial.getDiskRadius();

				final int slices = 32;
				final int stacks = 32;
				glu.gluSphere(circle, radius, slices, stacks); // draw sphere
				gl.glPopMatrix(); 
			}		        
			glu.gluDeleteQuadric(circle);

			// draw connecting lines
			if (_connectTheDots) {
				gl.glBegin(GL_LINE_STRIP);
				//gl.glLineWidth(3f); 		// has no effect?
				for (Point2D p: jointLocations) {
					gl.glVertex2d(p.getX(), p.getY());
				} 
				gl.glEnd();
			}
		}	
	}





	//	@Override
	//	public void display(GLAutoDrawable d) {
	//		final boolean showFriends = false;
	//		
	//		GL gl = d.getGL();
	//		
	//		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);       //Clear The Screen And The Depth Buffer
	//		gl.glMatrixMode(GL_MODELVIEW);                               // Make sure we're in the correct matrix mode
	//        gl.glLoadIdentity();                                         //Reset The View
	//        
	//        if(showFriends) {
	//	        gl.glTranslatef(-1.5f,0.0f,-8.0f);						// Move Left 1.5 Units And Into The Screen 8
	//	        gl.glBegin(GL_TRIANGLES);								// Drawing Using Triangles
	//	        gl.glColor3f(1.0f,0.0f,0.0f);						// Set The Color To Red
	//	        gl.glVertex3f( 0.0f, 1.0f, 0.0f);					// Top
	//	        gl.glColor3f(0.0f,1.0f,0.0f);						// Set The Color To Green
	//	        gl.glVertex3f(-1.0f,-1.0f, 0.0f);					// Bottom Left
	//	        gl.glColor3f(0.0f,0.0f,1.0f);						// Set The Color To Blue
	//	        gl.glVertex3f( 1.0f,-1.0f, 0.0f);					// Bottom Right
	//	        gl.glEnd();											// Finished Drawing The Triangle
	//	        gl.glTranslatef(3.0f,0.0f,0.0f);						// Move Right 3 Units
	//	        gl.glColor3f(0.5f,0.5f,1.0f);							// Set The Color To Blue One Time Only
	//	        gl.glBegin(GL_QUADS);									// Draw A Quad
	//	        gl.glVertex3f(-1.0f, 1.0f, 0.0f);					// Top Left
	//	        gl.glVertex3f( 1.0f, 1.0f, 0.0f);					// Top Right
	//	        gl.glVertex3f( 1.0f,-1.0f, 0.0f);					// Bottom Right
	//	        gl.glVertex3f(-1.0f,-1.0f, 0.0f);					// Bottom Left
	//	        gl.glEnd();							
	//        }        
	//        
	//        if(_trial != null) {
	//        	
	//        	//gl.glTranslatef(-4.35f,-3.26f,-8.0f);					
	//        	gl.glTranslatef(-6.6f,-4.5f,-8.0f);						// translate to (roughly) origin (fix later)
	//
	//        	// test AnimationParser method
	//        	File file = new File("/Users/Catherine/Workspace/Maple/auditory-research-suite/datafiles/examples/vis/es_.txt");
	//        	
	//        	AnimationParser parser = new AnimationParser();
	//        	
	//        	try {
	//        		AnimationSequence animation = parser.parseFile(file);
	//        		
	//        		//for ()
	//        		int currentFrame = 49;
	//        		AnimationFrame currFrame = animation.getFrame(currentFrame);
	//        				
	//        				
	//    	        // draw white circle
	//    	        GLU glu = new GLU();
	//    	        GLUquadric circle = glu.gluNewQuadric();
	//    	        glu.gluQuadricDrawStyle(circle, GLU.GLU_FILL);
	//    	        glu.gluQuadricNormals(circle, GLU.GLU_FLAT);
	//    	        glu.gluQuadricOrientation(circle, GLU.GLU_OUTSIDE);
	//    	        gl.glColor3f(1f, 1f, 1f);
	//    	       
	//    	        //List<Point2D> points = currFrame.getJointLocations();
	//    	        
	//    	        for(Point2D p : currFrame._pointList) {
	//    		        gl.glPushMatrix();
	//    		        // Draw sphere (possible styles: FILL, LINE, POINT).
	//    		        gl.glTranslated(p.getX(),p.getY(), 0.0);						
	//    		        
	//    		        //final float radius = 1.378f;
	//    		        
	//    		        float radius = _trial.getDiskRadius();
	//    		       // System.out.printf("disk radius = %f\n", radius);
	//    		       // float radius = _diskSize;
	//    		       //gl.glTranslatef(_trial._diskLocation.x, _trial._diskLocation.y, 0);
	//    		        
	//    		        final int slices = 32;
	//    		        final int stacks = 32;
	//    		        glu.gluSphere(circle, radius, slices, stacks); // draw sphere
	//    		        gl.glPopMatrix(); 
	//    		    }		        
	//    	        glu.gluDeleteQuadric(circle);
	//
	//    	        // draw connecting lines
	//    	        if (_connectTheDots) {
	//    	        	gl.glBegin(GL_LINE_STRIP);
	//    	        	//gl.glLineWidth(2f); 		// has no effect?
	//    	        	for (Point2D p: currFrame._pointList) {
	//    	        		gl.glVertex2d(p.getX(), p.getY());
	//    	        	} 
	//    	 	        gl.glEnd();
	//    	        }
	//    	       
	//        	} catch (FileNotFoundException ex) {
	//        		ex.printStackTrace();
	//        	}
	//        }
	//	}

	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		// maps to method "resize" in python doc?


	}


	@Override
	public void reshape(GLAutoDrawable canvas, int x, int y, int width,
			int height) {
		GL gl = canvas.getGL();
		GLU glu = new GLU();

		System.out.println("Width : "+width+" Height: "+height);
		if(height==0)height=1;
		gl.glViewport(0, 0, width, height);                       // Reset The Current Viewport And Perspective Transformation
		gl.glMatrixMode(GL_PROJECTION);                           // Select The Projection Matrix
		gl.glLoadIdentity();                                      // Reset The Projection Matrix
		// glu.gluPerspective(45.0f, width / height, 0.1f, 100.0f);  // Calculate The Aspect Ratio Of The Window
		glu.gluPerspective(45.0f, 1.3, 0.1f, 100.0f);  			// Calculate The Aspect Ratio Of The Window

		gl.glMatrixMode(GL_MODELVIEW);                            // Select The Modelview Matrix
		gl.glLoadIdentity();   

		//	
		//		GL gl = canvas.getGL();
		//		
		//		
		//		gl.glMatrixMode(GL_PROJECTION); // missing library
		////		gl.glOrtho2D(0, width, 0, height);
		//		gl.glMatrixMode(GL_MODELVIEW);	
		//		
	}

}

