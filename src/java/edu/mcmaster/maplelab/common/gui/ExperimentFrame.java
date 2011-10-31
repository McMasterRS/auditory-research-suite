/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/
package edu.mcmaster.maplelab.common.gui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import edu.mcmaster.maplelab.common.*;
import edu.mcmaster.maplelab.common.datamodel.*;
import edu.mcmaster.maplelab.toj.TOJExperiment;


/**
 * Abstract base class for experiment applet containers.
 * @version         $Revision$
 * @author         <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since         May 10, 2006
 */
public abstract class ExperimentFrame
    <T extends Session<Q, R, L>,  Q extends Block<T, R>, R extends Trial<?>, L extends TrialLogger <Q, R> > 
    extends JFrame {
	
	static {
        // Not sure this is still needed.
        System.setProperty("apple.laf.useScreenMenuBar", "true");     
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
    }

    private static DisplayMode _originalDisplayMode;
    
    private T _session;
    private DebugConsole _console;
    private boolean _isFullScreen;
    private Container _rootContainer;
    
    /**
     * This is the default constructor
     * @param setup 
     */
    public ExperimentFrame(SimpleSetupScreen<T> setup) {
        super("Experiment", selectScreenConfiguration());

        Logger logger = LogContext.getLogger();
        logger.setLevel(Level.WARNING);
        
        Properties props = initProperties(setup);
        if(props == null) return;
        
        _session = createSession(props);
        setup.applySettings(_session);
        _isFullScreen = _session.isFullScreen() && !_session.isDemo();

        final DefaultExceptionHandler exHandler = new DefaultExceptionHandler(_session.isDebug());
        
        _session.execute(new Runnable() {
            public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(exHandler);
            }
        });
        
        // Run the exception handler on the EDT, which we might 
        // already be on (probably are)
        Runnable exceptHandlerSetup = new Runnable() {
            public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(exHandler);
            }
        };
        try {
        	if(EventQueue.isDispatchThread()) {
        		exceptHandlerSetup.run();
        	}
        	else {
        		EventQueue.invokeAndWait(exceptHandlerSetup);
        	}
        }
        catch (Throwable ex) {
            // Shouldn't happen...
            ex.printStackTrace();
        }
        
        _session.setTrialLogger(initTrialLogger(setup.getDataDir()));
        
        if(_session.isDebug()) {
            _console = new DebugConsole(_session);
            // If the console sits on a different monitor and we are in full screen mode,
            // then attempt to make the console full screen too.
            if(_isFullScreen && this.getGraphicsConfiguration() != _console.getGraphicsConfiguration()) {
            	setFullScreen(_console, true, null, getFullScreenBackGround(), true);
            }
            else {
                _console.setVisible(true);
            }
            logger.setLevel(Level.ALL);
            logger.finest("java.version="+System.getProperty("java.version"));
            
            logger.finest("-------System Properties-------");
            
            try {
                writeProperties(logger, System.getProperties());
            }
            catch(Throwable ex) {
                logger.warning(ex.toString());
            }
        }        
        Container c = createContent(_session); // MUST be called!
        if (_session.isDemo()) {
        	setContentPane(_session.getExperimentDemoPanel());
        }
        else {
        	setContentPane(c);
        }
        
        if(_session.isDebug()) {
            logger.finer("-------Config-------");
            logger.finer(_session.toPropertiesString());
            logger.finer("-------Config-------");
        }
       
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        if (_isFullScreen) {
        	_rootContainer = getContentPane();
            setFullScreen(this, true, null, getFullScreenBackGround(), false);
        }
        else {
        	_rootContainer = this;
        }
    }
    
    /**
     * Set the experiment view size.
     */
    public void setExperimentSize(int width, int height) {
    	_rootContainer.setSize(width, height);
    	_rootContainer.setPreferredSize(new Dimension(width, height));
    }
    
    /**
     * Get the graphics configuration for the primary screen.
     */
    private static GraphicsConfiguration selectScreenConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
       
        if(gs != null && gs.length > 0) {
            return gs[0].getDefaultConfiguration();
        }
        
        return null;
    }

    @Override
    public void pack() {
        // Calling pack creates the peer and also interferes with full screen 
        // scaling, so we ignore it when in full screen mode.
        if(!_isFullScreen) {
            super.pack();
        }
    }
    
    public boolean isFullScreen() {
    	return _isFullScreen;
    }
    
    public boolean isDemo() {
    	return _session.isDemo();
    }
    
    /**
     * 
     * @param win window to make fullscreen
     * @param state fullscreen state
     * @param size "fullscreen" size
     * @param background background color for area w/ no content
     * @param expandContents expand the contents to fill the entire screen (rather than 
     * 						 using just the initial size of the contents)
     */
    private static void setFullScreen(final Window win, boolean state, final Dimension size, 
    		Color background, boolean expandContents) {
    	
        GraphicsConfiguration config = win.getGraphicsConfiguration();
        
        boolean hasFullScreen = false;
        GraphicsDevice device = null;
        
        device = config.getDevice();                
        hasFullScreen = device.isFullScreenSupported();

        if(win instanceof JFrame) {
            JFrame f = (JFrame) win;
            f.setUndecorated(state && hasFullScreen);
            f.setResizable(!(state && hasFullScreen));
            
            if (!expandContents) {
                // set an intermediate panel as the content pane so that
                // we can control background color and size
                Container c = f.getContentPane();
                JPanel p = new JPanel(new MigLayout("insets 0, fill", "[center]", "[center]"));
                p.setBackground(background);
                f.setContentPane(p);
                p.add(c);
            }
        }
        
        if (hasFullScreen) {
            // Full-screen mode
            device.setFullScreenWindow(state ? win : null);
            
            Rectangle bounds = config.getBounds();
            
            if(size != null && device.isDisplayChangeSupported()) {
              _originalDisplayMode  = device.getDisplayMode();
              device.setDisplayMode(new DisplayMode(size.width, size.height,
                  _originalDisplayMode.getBitDepth(), 
                  _originalDisplayMode.getRefreshRate()));                
              bounds = new Rectangle(bounds.x, bounds.y, size.width, size.height);
            }
            
            win.setBounds(config.getBounds());
            win.validate();
        } 
    }
    /**
     * Get the graphics configuration for the primary screen.
     */
//    private static GraphicsConfiguration selectScreenConfiguration() {
//        GraphicsConfiguration retval = null;
//        
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice[] gs = ge.getScreenDevices();
//       
//        if(gs != null && gs.length > 0) {
//            GraphicsDevice mainDev = gs[0];
//            
//            if(mainDev.isDisplayChangeSupported()) {
//                _originalDisplayMode  = mainDev.getDisplayMode();
//
//                mainDev.setDisplayMode(new DisplayMode(1024, 768, 
//                    _originalDisplayMode.getBitDepth(), _originalDisplayMode.getRefreshRate()));
//            }
//            
//            retval = mainDev.getDefaultConfiguration();
//        }
//        
//        return retval;
//    }
    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", title);
    }
    
    /**
     * Get the global session object.
     */
    public T getSession() {
        return _session;
    }
    
    /**
     * Called to create the major components of the experiment.
     * 
     * @param session same session created by createSession(Properties).
     * @return Container to add to applet.
     */
    protected abstract Container createContent(T session);
    
    /**
     * Get the background color in fullscreen mode.
     */
    protected abstract Color getFullScreenBackGround();

    /**
     * Delegate for creating a new session.
     */
    protected abstract T createSession(Properties props);

    @SuppressWarnings("unchecked") // for SortedSet.addAll()
    private void writeProperties(Logger logger, Properties properties) {
        SortedSet<String> sortedKeys = new TreeSet<String>();

        sortedKeys.addAll((Collection) properties.keySet());
        
        Iterator<String> it = sortedKeys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = properties.getProperty(key);
            logger.finest(String.format("%s=%s", key, value));
        }
    }      

    private Properties initProperties(SimpleSetupScreen<T> setup) {
        Properties props = new Properties();
        InputStream in = null;
        try {
        	String name = setup.getPrefsPrefix() + ".properties";
            File f = new File(setup.getDataDir(), name);
            if (f.exists()) {
                in = new FileInputStream(f);              
            }
            else {
                in = getClass().getResourceAsStream(name);
            }
            props.load(in);
        }
        catch (Exception ex) {
            logError(ex, "Error reading configuration file");
            return null;
        }
        finally {
            if(in != null)  try { in.close(); } catch (IOException e) {}
        }
        
        return props;
    }
    
    /**
     * Called when the trial logger should be created.
     * 
     * @param dataDir user specified data directory.
     */
    protected abstract L initTrialLogger(File dataDir);
    
    protected void logError(Throwable ex, String msg, Object... args) {
        msg = String.format(msg, args);
        LogContext.getLogger().log(Level.SEVERE, msg, ex);
        add(new JLabel(msg));
    }

} 