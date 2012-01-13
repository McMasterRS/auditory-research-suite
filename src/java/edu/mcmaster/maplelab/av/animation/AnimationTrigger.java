/**
 * 
 */
package edu.mcmaster.maplelab.av.animation;

import javax.media.opengl.awt.GLJPanel;

/**
 * @author bguseman
 *
 */
public interface AnimationTrigger {
	public void setCanvas(GLJPanel canvas);
	public void start();
	public void stop();
}
