package edu.mcmaster.maplelab.toj.animator;

import static javax.media.opengl.GL2.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/** !!!!!!!!!! FOR FUTURE REFERENCE !!!!!!!!!!!!!!
 * 
 * GL_POINTS 	Draws points on screen. Every vertex specified is a point.
 * GL_LINES 	Draws lines on screen. Every two vertices specified compose a line.
 * GL_LINE_STRIP 	Draws connected lines on screen. Every vertex specified after first two are connected.
 * GL_LINE_LOOP 	Draws connected lines on screen. The last vertex specified is connected to first vertex.
 * GL_TRIANGLES 	Draws triangles on screen. Every three vertices specified compose a triangle.
 * GL_TRIANGLE_STRIP 	Draws connected triangles on screen. Every vertex specified after first three vertices creates a triangle.
 * GL_TRIANGLE_FAN 	Draws connected triangles like GL_TRIANGLE_STRIP, except draws triangles in fan shape.
 * GL_QUADS 	Draws quadrilaterals (4 Ð sided shapes) on screen. Every four vertices specified compose a quadrilateral. FILLED!
 * GL_QUAD_STRIP 	Draws connected quadrilaterals on screen. Every two vertices specified after first four compose a connected quadrilateral.
 * GL_POLYGON 	Draws a polygon on screen. Polygon can be composed of as many sides as you want.
 */

/**
 * Enumerated list of drawable shapes.
 * 
 * @author bguseman
 */
public enum AnimationShapeDrawable {
	DOT {
		private GLUquadric _circle;
		@Override
		protected void init(GL2 gl, GLU glu) {
			_circle = glu.gluNewQuadric();
			glu.gluQuadricDrawStyle(_circle, GLU.GLU_FILL);
			glu.gluQuadricNormals(_circle, GLU.GLU_FLAT);
			glu.gluQuadricOrientation(_circle, GLU.GLU_OUTSIDE);
		}
		@Override
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap) {
			final int slices = 32;
			final int stacks = 32;
								
			// draw sphere
			glu.gluSphere(_circle, RADIUS, slices, stacks); 
		}
		@Override
		protected void clear(GL2 gl, GLU glu) {
			glu.gluDeleteQuadric(_circle);
		}
	},
	CROSS {
		@Override
		protected void init(GL2 gl, GLU glu) {
		}
		@Override
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap) {
			gl.glBegin(GL_LINES);

			gl.glVertex2d(0, RADIUS);
			gl.glVertex2d(0, -RADIUS);
			gl.glVertex2d(-RADIUS, 0);
			gl.glVertex2d(RADIUS, 0);
			
			gl.glEnd();
		}
		@Override
		protected void clear(GL2 gl, GLU glu) {
		}
	},
	SQUARE {
		@Override
		protected void init(GL2 gl, GLU glu) {
		}
		@Override
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap) {
			gl.glBegin(GL_QUADS);

			gl.glVertex2d(-RADIUS, -RADIUS);
			gl.glVertex2d(-RADIUS, RADIUS);
			gl.glVertex2d(RADIUS, RADIUS);
			gl.glVertex2d(RADIUS, -RADIUS);
			
			gl.glEnd();
		}
		@Override
		protected void clear(GL2 gl, GLU glu) {
		}
	},
	DIAMOND {
		@Override
		protected void init(GL2 gl, GLU glu) {
		}
		@Override
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap) {
			gl.glBegin(GL_QUADS);

			gl.glVertex2d(0, -RADIUS);
			gl.glVertex2d(-RADIUS, 0);
			gl.glVertex2d(0, RADIUS);
			gl.glVertex2d(RADIUS, 0);
			
			gl.glEnd();
		}
		@Override
		protected void clear(GL2 gl, GLU glu) {
		}
	};
	
	private static final double RADIUS = 0.1;
	
	public void draw(GL2 gl, GLU glu, AnimationPoint dot, Double lum) {
		draw(gl, glu, dot, lum, 1.0f);
	}
	
	public void draw(GL2 gl, GLU glu, AnimationPoint dot, Double lum, float aspect) {
		gl.glPushMatrix();
		
		if (dot.getLocation() != null) {			
			gl.glTranslated(dot.getLocation().x, dot.getLocation().y, 0);						
		}
		
		// color each dot individually
		if (dot.getColor() != null) {
			if (lum != null) {
				gl.glColor3d(lum*dot.getColor().x, lum*dot.getColor().y, lum*dot.getColor().z);
			}
			else {
				gl.glColor3d(dot.getColor().x, dot.getColor().y, dot.getColor().z);
			}
		}
		
		// get size of each dot
		Double rad = dot.getSize() != null ? dot.getSize() : 1.0f;
		gl.glScaled(rad/aspect, rad, rad);

		// draw the specific shape
		drawShape(gl, glu, dot);
		
		gl.glPopMatrix(); 
	}
	
	// abstract methods
	protected abstract void init(GL2 gl, GLU glu);
	protected abstract void drawShape(GL2 gl, GLU glu, AnimationPoint ap);
	protected abstract void clear(GL2 gl, GLU glu);
	
	// static methods
	public static void initialize(GL2 gl, GLU glu) {
		for (AnimationShapeDrawable as : values()) {
			as.init(gl, glu);
		}
	}
	public static void cleanup(GL2 gl, GLU glu) {
		for (AnimationShapeDrawable as : values()) {
			as.clear(gl, glu);
		}
	}
}
