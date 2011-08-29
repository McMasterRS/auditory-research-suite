/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.animator;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.io.File;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.sun.opengl.util.Animator;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.SoundClip;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * Creates a panel to be used for animation rendering.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimatorPanel extends JPanel {
	private final GLJPanel _canvas;
	private final AnimationRenderer _animator;
    
    static {
        // Put JOGL version information into system properties to 
        // assist in debugging.
        Package joglPackage = Package.getPackage("javax.media.opengl");
        System.setProperty("jogl.specification.version", joglPackage.getSpecificationVersion());
        System.setProperty("jogl.implementation.version", joglPackage.getImplementationVersion());
        
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }
    
    public AnimatorPanel(AnimationRenderer animator) {
    	this(animator, null);
    }
   
    public AnimatorPanel(AnimationRenderer animator, Dimension dim) {

        super(new BorderLayout());

        setBorder(UIManager.getDefaults().getBorder("ProgressBar.border"));

        GLCapabilities caps = new GLCapabilities();

        _canvas = new GLJPanel(caps);
        _canvas.setName("glCanvas");
        _animator = animator;
        _canvas.addGLEventListener(_animator);
        
 		Animator trigger = new Animator(_canvas);
 		trigger.setPrintExceptions(true);
 		trigger.start();
 		
        add(_canvas, BorderLayout.CENTER);
        if (dim != null) {
        	setPreferredSize(dim);
        	setMinimumSize(dim);
        }
        else setPreferredSize(new Dimension(640, 480));
        
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("mac") >= 0 || osName.indexOf("darwin") >= 0) {
        	// Without this call we get a canvas width or height == 0 error.
            _canvas.setSize(getPreferredSize());
        }
    }
    
    /**
     * {@inheritDoc} 
     * @see java.awt.Component#setCursor(java.awt.Cursor)
     */
    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        _canvas.setCursor(cursor);
    }

    /**
     * Attempt to make errors generated when 3D not available a little nicer.
     * {@inheritDoc} 
     * @see java.awt.Component#addNotify()
     */
    @Override
    public void addNotify() {
        try {
            super.addNotify();
        }
        catch (UnsatisfiedLinkError ex) {
        	LogContext.getLogger().warning("Sorry, 3D model rendering has not been enabled " +
        			"for this platform.");
        	ex.printStackTrace();
        }
    } 

    /**     
     * {@inheritDoc} 
     * @see java.awt.Component#repaint()
     */
    @Override
    public void repaint() {
        super.repaint();
        // Force a repaint on GLCanvas.
        if(_canvas != null) {
            // NB: canvas might be null during component instantiation due
            // to some brain dead code in swing.
            _canvas.repaint();
        }
    }

    public void repaintLater() {
        //invalidate();
        EventQueue.invokeLater(new Runnable() {
           public void run() {
               repaint();
           }
        });
    }
    
    /**
     * Overridden to provide appropriate printing behavior.
     */
    @Override
    public void print(Graphics g) {
        _canvas.display();
        super.print(g);
    }
    
    /**
     * Example panel. Animate an example file (E short) and play a tone (D long).
     */
    public static void main(String[] args) {
        try {
            JFrame f = new JFrame(AnimatorPanel.class.getName());
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            AnimationRenderer ani = new AnimationRenderer(true); // connect the dots
            
            final AnimatorPanel view = new AnimatorPanel(ani);

            f.getContentPane().add(view, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            
    		File file = new File("/Users/Catherine/Workspace/Maple/auditory-research-suite/datafiles/examples/vis/es_.txt");
    		
			AnimationSequence animation = AnimationParser.parseFile(file);
			
			String filename = NotesEnum.D.toString().toLowerCase() + "_" +  DurationEnum.LONG.toString().toLowerCase().substring(0,1) + ".wav";
			//System.out.printf("sound clip filename = %s\n", filename);

			File dir = new File("/Users/Catherine/Workspace/Maple/auditory-research-suite/datafiles/examples/aud");

			final Playable audio = SoundClip.findPlayable(filename, dir);
			
			Runnable r = new Runnable() {
				@Override
				public void run() {
					audio.play();
				}
			} ;
			
            TOJTrial trial = new TOJTrial(animation, 
					true, audio, (long) 1, 5, 0.3f);

    		long currentTime = System.currentTimeMillis();
            ani.setTrial(trial);
               
    		ani.setStartTime(currentTime); 
    		SwingUtilities.invokeLater(r);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }	
    }
}
