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

import edu.mcmaster.maplelab.common.*;
import edu.mcmaster.maplelab.common.datamodel.*;


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
    private final boolean _isFullScreen;
    
    /**
     * This is the default constructor
     * @param setup 
     */
    public ExperimentFrame(SimpleSetupScreen setup) {
        super("KubovyLabExperiment", selectScreenConfiguration());
        _isFullScreen = setup.isFullScreen();

        Logger logger = LogContext.getLogger();
        logger.setLevel(Level.WARNING);
        
        Properties props = initProperties(setup.getDataDir());
        if(props == null) return;
        
        _session = createSession(props);
        setup.applySettings(_session);

        final DefaultExceptionHandler exHandler = new DefaultExceptionHandler(_session.isDebug());
        
        _session.execute(new Runnable() {
            public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(exHandler);
            }
        });
        
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    Thread.currentThread().setUncaughtExceptionHandler(exHandler);
                }
            });
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
                setFullScreen(_console, true, null);
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
        
        this.setContentPane(createContent(_session));
        
        if(_session.isDebug()) {
            logger.finer("-------Config-------");
            logger.finer(_session.toPropertiesString());
            logger.finer("-------Config-------");
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        if(_isFullScreen) {
            setFullScreen(this, true, null /*new Dimension(1024, 768)*/);
        }
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
    
    private static void setFullScreen(final Window win, boolean state, final Dimension size) {
        GraphicsConfiguration config = win.getGraphicsConfiguration();
        
        boolean hasFullScreen = false;
        GraphicsDevice device = null;
        
        device = config.getDevice();                
        hasFullScreen = device.isFullScreenSupported();

        if(win instanceof JFrame) {
            JFrame f = (JFrame) win;
            f.setUndecorated(state && hasFullScreen);
            f.setResizable(!(state && hasFullScreen));
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
            
            win.setBounds(bounds);
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
     * Delegate for creating a new session.
     */
    protected abstract T createSession(Properties props);
    
    /**
     * Get the file to the configuration data.
     */
    protected abstract InputStream getConfigData(File dataDir) throws IOException;

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

    private Properties initProperties(File dataDir) {
        Properties props = new Properties();
        
        InputStream in = null;
        try {
            in = getConfigData(dataDir);
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