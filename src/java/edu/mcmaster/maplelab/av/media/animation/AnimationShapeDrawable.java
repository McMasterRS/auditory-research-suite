/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.media.animation;

import static javax.media.opengl.GL2.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Vector3f;

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
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap, float radius) {
			final int slices = 32;
			final int stacks = 32;
								
			// draw sphere
			glu.gluSphere(_circle, radius, slices, stacks); 
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
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap, float radius) {
			gl.glBegin(GL_LINES);

			gl.glVertex2d(0, radius);
			gl.glVertex2d(0, -radius);
			gl.glVertex2d(-radius, 0);
			gl.glVertex2d(radius, 0);
			
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
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap, float radius) {
			gl.glBegin(GL_QUADS);

			gl.glVertex2d(-radius, -radius);
			gl.glVertex2d(-radius, radius);
			gl.glVertex2d(radius, radius);
			gl.glVertex2d(radius, -radius);
			
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
		public void drawShape(GL2 gl, GLU glu, AnimationPoint ap, float radius) {
			gl.glBegin(GL_QUADS);

			gl.glVertex2d(0, -radius);
			gl.glVertex2d(-radius, 0);
			gl.glVertex2d(0, radius);
			gl.glVertex2d(radius, 0);
			
			gl.glEnd();
		}
		@Override
		protected void clear(GL2 gl, GLU glu) {
		}
	};
	
	public void draw(GL2 gl, GLU glu, AnimationPoint dot, Double lum, float radius) {
		draw(gl, glu, dot, lum, radius, 1.0f);
	}
	
	public void draw(GL2 gl, GLU glu, AnimationPoint dot, Double lum, float radius, float aspect) {
		gl.glPushMatrix();
		
		if (dot.getLocation() != null) {			
			gl.glTranslated(dot.getLocation().x, dot.getLocation().y, 0);						
		}
		
		// color each dot individually
		// XXX: be careful here - for some reason the scaling must be done
		// at this level - does OpenGL access the actual data locations internally?
		if (dot.getColor() != null) {
			Vector3f colorVector = new Vector3f(dot.getColor());
			float scale = (lum != null ? lum.floatValue() : 1f) / 255f;
			colorVector.scale(scale);
			
			gl.glColor3f(colorVector.x, colorVector.y, colorVector.z);
			
			// get current color values (debug)
			/*int[] col = new int[4];
			gl.glGetIntegerv(GL2.GL_CURRENT_COLOR, col, 0);
			System.out.printf("curr int: %d, %d, %d, %d%n", col[0], col[1], col[2], col[3]);
			float[] col2 = new float[4];
			gl.glGetFloatv(GL2.GL_CURRENT_COLOR, col2, 0);
			System.out.printf("curr float: %f, %f, %f, %f%n", col2[0], col2[1], col2[2], col2[3]);*/
		}
		
		// get size of each dot
		Double size = dot.getSize() != null ? dot.getSize() : 1.0f;
		gl.glScaled(size/aspect, size, size);

		// draw the specific shape
		drawShape(gl, glu, dot, radius);
		
		gl.glPopMatrix(); 
	}
	
	// abstract methods
	protected abstract void init(GL2 gl, GLU glu);
	protected abstract void drawShape(GL2 gl, GLU glu, AnimationPoint ap, float radius);
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
