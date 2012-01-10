/*
 * Copyright (c) 2012 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.av.animation;

import java.util.logging.Level;

import javax.media.opengl.*;
import javax.swing.JFrame;

import edu.mcmaster.maplelab.common.LogContext;


/**
 * Test panel for testing A/V synchronization.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Jan 9, 2012
 */
public class AudioLatchedAnimationPanel extends AnimationPanel {
    
    public AudioLatchedAnimationPanel() {
        super(new AudioLatchedAnimator());
    }
    
    
    private static class AudioLatchedAnimator implements GLEventListener {

        @Override
        public void display(GLAutoDrawable draw) {
            
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
            AudioLatchedAnimationPanel p = new AudioLatchedAnimationPanel();
            
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
