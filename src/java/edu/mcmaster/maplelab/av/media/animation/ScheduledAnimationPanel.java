/*
 * Copyright (c) 2012 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.av.media.animation;

import java.awt.Dimension;
import java.util.logging.Level;

import javax.media.opengl.*;
import javax.swing.JFrame;

import edu.mcmaster.maplelab.av.StimulusScheduler;
import edu.mcmaster.maplelab.common.LogContext;


/**
 * Test panel for testing A/V synchronization.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Jan 9, 2012
 */
public class ScheduledAnimationPanel extends AnimationPanel {
	
	public ScheduledAnimationPanel() {
		this(null);
	}
	
    public ScheduledAnimationPanel(Dimension dim) {
		super(null, dim);
		StimulusScheduler scheduler = StimulusScheduler.getInstance();
		overrideDefaultTrigger(scheduler.getAnimationTrigger());
		setRenderer(scheduler.getAnimationRenderer());
	}
    
    
    
    /*private static class BackgroundSwitchingAnimator implements GLEventListener {
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
            p.setRenderer(new BackgroundSwitchingAnimator());
            
            JFrame f = new JFrame(p.getClass().getName());
            f.getContentPane().add(p);
            f.pack();
            f.setVisible(true);
        }
        catch (Exception ex) {
            LogContext.getLogger().log(Level.SEVERE, "Initialization error.", ex);
        }
    }*/
}
