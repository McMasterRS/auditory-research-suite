/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.media.animation;

import javax.media.opengl.GLAutoDrawable;

import edu.mcmaster.maplelab.av.ScheduleEvent;
import edu.mcmaster.maplelab.av.Scheduled;

/**
 * @author bguseman
 *
 */
public class ScheduledAnimationTrigger implements AnimationTrigger, Scheduled {
	private GLAutoDrawable _canvas;
	private Long _callAheadNanos;
	private boolean _running = false;
	
	public void setRenderCallAhead(Long callAheadNanos) {
		_callAheadNanos = callAheadNanos != null ? callAheadNanos : 0;
	}

	/**
	 * @see edu.mcmaster.maplelab.av.Scheduled#markTime(edu.mcmaster.maplelab.av.ScheduleEvent)
	 */
	@Override
	public void markTime(ScheduleEvent e) {
		if (_running) {
			//System.out.println("trigger: " + System.nanoTime());
			_canvas.display();
		}
	}

	/**
	 * @see edu.mcmaster.maplelab.av.Scheduled#alarm(edu.mcmaster.maplelab.av.ScheduleEvent)
	 */
	@Override
	public void alarm(ScheduleEvent e) {
		// no-op
	}

	/**
	 * @see edu.mcmaster.maplelab.av.media.animation.AnimationTrigger#setCanvas(GLAutoDrawable)
	 */
	@Override
	public void setCanvas(GLAutoDrawable canvas) {
		_canvas = canvas;
	}

	/**
	 * @see edu.mcmaster.maplelab.av.media.animation.AnimationTrigger#start()
	 */
	@Override
	public void start() {
		_running = true;
	}

	/**
	 * @see edu.mcmaster.maplelab.av.media.animation.AnimationTrigger#stop()
	 */
	@Override
	public void stop() {
		_running = false;
	}
	
	/**
	 * Force a rendering update.  Useful for initialization.
	 */
	public void forceDisplay() {
		if (_canvas != null) _canvas.display();
	}
	@Override
	public long callAheadNanoTime() {
		return _callAheadNanos;
	}

}
