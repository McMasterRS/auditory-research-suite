/*
 * Copyright (c) 2012 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.av.media.animation;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import edu.mcmaster.maplelab.av.ScheduleEvent;
import edu.mcmaster.maplelab.av.Scheduled;
import edu.mcmaster.maplelab.av.Scheduler;
import edu.mcmaster.maplelab.common.LogContext;


/**
 * Test panel for testing A/V synchronization.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Jan 9, 2012
 */
public class ScheduledAnimationPanel extends AnimationPanel {
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		int r = gs.getDisplayMode().getRefreshRate();
		System.out.println(r);
	}
	
	private final Scheduler _scheduler;
	
    public ScheduledAnimationPanel() {
        super(new BackgroundSwitchingAnimator());
        _scheduler = new Scheduler(123333334, TimeUnit.NANOSECONDS, 5);
        ScheduledAnimationTrigger trigger = new ScheduledAnimationTrigger();
        overrideDefaultTrigger(trigger);
        _scheduler.schedule(trigger);
        start();
        _scheduler.start();
    }
    
    private static class BackgroundSwitchingAnimator implements GLEventListener {
    	private boolean _switch = true;
    	
        @Override
        public void display(GLAutoDrawable draw) {
        	GL2 gl = draw.getGL().getGL2();
        	
        	if (_switch) {
                gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        	}
        	else {
                gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        	}
        	gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        	_switch = !_switch;
        }

        @Override
        public void dispose(GLAutoDrawable draw) {
        }

        @Override
        public void init(GLAutoDrawable draw) {
            GL2 gl = draw.getGL().getGL2();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            
        }
    }
    
    public static void main(String[] args) {
    	try { 
            ScheduledAnimationPanel p = new ScheduledAnimationPanel();
            
            JFrame f = new JFrame(p.getClass().getName());
            f.getContentPane().add(p);
            f.pack();
            f.setVisible(true);
        }
        catch (Exception ex) {
            LogContext.getLogger().log(Level.SEVERE, "Initialization error.", ex);
        }
    }
}
