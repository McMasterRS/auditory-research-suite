/**
 * 
 */
package edu.mcmaster.maplelab.av.animation;

import javax.media.opengl.awt.GLJPanel;

import edu.mcmaster.maplelab.av.ScheduleEvent;
import edu.mcmaster.maplelab.av.Scheduled;

/**
 * @author bguseman
 *
 */
public class ScheduledAnimationTrigger implements AnimationTrigger, Scheduled {
	private GLJPanel _canvas;
	private boolean _running = false;

	/**
	 * @see edu.mcmaster.maplelab.av.Scheduled#markTime(edu.mcmaster.maplelab.av.ScheduleEvent)
	 */
	@Override
	public void markTime(ScheduleEvent e) {
		if (_running) _canvas.display();
	}

	/**
	 * @see edu.mcmaster.maplelab.av.Scheduled#alarm(edu.mcmaster.maplelab.av.ScheduleEvent)
	 */
	@Override
	public void alarm(ScheduleEvent e) {
		// no-op
	}

	/**
	 * @see edu.mcmaster.maplelab.av.animation.AnimationTrigger#setCanvas(javax.media.opengl.awt.GLJPanel)
	 */
	@Override
	public void setCanvas(GLJPanel canvas) {
		_canvas = canvas;
	}

	/**
	 * @see edu.mcmaster.maplelab.av.animation.AnimationTrigger#start()
	 */
	@Override
	public void start() {
		_running = true;
	}

	/**
	 * @see edu.mcmaster.maplelab.av.animation.AnimationTrigger#stop()
	 */
	@Override
	public void stop() {
		_running = false;
	}

}
