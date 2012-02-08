/**
 * 
 */
package edu.mcmaster.maplelab.av.media.animation;

import javax.media.opengl.GLAutoDrawable;

/**
 * @author bguseman
 *
 */
public interface AnimationTrigger {
	public void setCanvas(GLAutoDrawable canvas);
	public void start();
	public void stop();
}
