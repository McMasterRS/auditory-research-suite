/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
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
