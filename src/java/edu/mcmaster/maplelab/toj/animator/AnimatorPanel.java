package edu.mcmaster.maplelab.toj.animator;

import javax.swing.JPanel;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import com.sun.opengl.util.Animator;

import java.awt.geom.Point2D;


import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

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
    
    /** 
     * Default ctor.
     */
    public AnimatorPanel(AnimationRenderer animator) {
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
        setPreferredSize(new Dimension(640, 480));
        
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
               /*_canvas.invalidate();
               getParent().invalidate();
               getParent().validate();
               _canvas.validate();*/
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
    
    public static void main(String[] args) {
        try {
            JFrame f = new JFrame(AnimatorPanel.class.getName());
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            AnimationRenderer ani = new AnimationRenderer(true); // connect the dots
            
            final AnimatorPanel view = new AnimatorPanel(ani);

            f.getContentPane().add(view, BorderLayout.CENTER);
            f.pack();
            f.setVisible(true);
            
            
    		File file = new File("/Users/Catherine/Workspace/Maple/auditory-research-suite/datafiles/examples/vis/es_.txt");
    		
    		AnimationParser parser = new AnimationParser();

			AnimationSequence animation = parser.parseFile(file);
			System.out.printf("total animation time for this seq = %f\n", (float)animation.getTotalAnimationTime());
			
            TOJTrial trial = new TOJTrial(animation, 
					NotesEnum.C, true, DurationEnum.LONG, DurationEnum.NORMAL, 1, 5, 0.3f);

    		long currentTime = System.currentTimeMillis();
            ani.setTrial(trial);
            ani.setCurrentFrame(0);
    		ani.setStartTime(currentTime); 

            
            
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        
        
        
//        
//        
//		
//		//Timer timer = new Timer();
//		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//		
//		long delay = (long)0.1;
//		long cumulativeDelay = 0;
//		
//		myRunnable newRunnable = new myRunnable();
//		//Thread thread = new Thread(newRunnable);
//		
//		int i;
//		//int timeoutTime = 11;
//		boolean firstPass;
//		
//		do {
//			for (i = 0; i < ((animation._aniFrames).size()); i++) {
//				System.out.println("loop entered");
//				AnimationFrame currFrame = animation.getFrame(i);
//				_currentFrame = currFrame;
//				//timer.schedule(displayFrame(gl, currFrame), 1000);
//				cumulativeDelay += delay;
//				scheduler.schedule(newRunnable, cumulativeDelay, TimeUnit.SECONDS);
//			}
//			firstPass = false;
//		} while ((firstPass = true));
//		System.out.printf("number of frames to be animated = %d\n", i);
//
//
		
    }
}
