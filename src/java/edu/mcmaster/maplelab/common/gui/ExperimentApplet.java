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

import java.awt.Container;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.*;

import edu.mcmaster.maplelab.common.DebugConsole;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;


/**
 * Abstract base class for experiment applet containers.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   May 10, 2006
 */
public abstract class ExperimentApplet<T extends Session> extends JApplet {
    /**
     * @uml.property  name="session"
     */
    private T _session;
    private DebugConsole _console;
    
    /**
     * This is the default constructor
     */
    public ExperimentApplet() {
    }
    
    
    /**
     * Get the global session object.
     * @uml.property  name="session"
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

    
    protected abstract T createSession(Properties props);
    
    /**
     * Applet parameter info. Override in subclass as necessary.
     * {@inheritDoc} 
     * @see java.applet.Applet#getParameterInfo()
     */
    public String[][] getParameterInfo() {
        return new String[][] {
            { "experiment_id", "integer", "database key of current experiment" },
            { "config", "string", "Name of the configuration file" }
        };
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    @SuppressWarnings("unchecked")
    public void init() {
        
        _console = new DebugConsole(_session);
        LogContext.getLogger().setLevel(Level.WARNING);
        
        Properties props = initProperties();
        if(props == null) return;
        
        _session = createSession(props);
        _session.setApplet(this);
        
        if(_session.isDebug()) {
            LogContext.getLogger().setLevel(Level.ALL);
            
            _console = new DebugConsole(_session);
            _console.setVisible(true);
            _console.toBack();
            LogContext.getLogger().finest("java.version="+System.getProperty("java.version"));
        }        
        
        _session.setTrialLogger(initTrialLogger());
        
        this.setContentPane(createContent(_session));
        
        if(_session.isDebug()) {
            LogContext.getLogger().finest("-------Config-------");
            LogContext.getLogger().finest(_session.toPropertiesString());
            LogContext.getLogger().finest("-------Config-------");
        }
        
        // Fix for applet bug where initial screen is sometimes blank.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExperimentApplet.this.invalidate();
                ExperimentApplet.this.repaint();
            }
        });
    }

    private Properties initProperties() {
        String propFile = getParameter("config");
        if(propFile == null) {
            propFile = "experiment.properties";
        }

        Properties props = new Properties();
        
        URL base = getDocumentBase();
        URL experUrl = null;
        try {
            experUrl = new URL(base, propFile);
            props.load(experUrl.openStream());
        }
        catch (MalformedURLException ex) {
            logError(ex, "Couldn't find properties file '%s' at '%s'", propFile, base);
            return null;
        }
        catch (IOException ex) {
            logError(ex, "Error reading '%s'", experUrl);
            return null;
        }
        
        // Add parameters to properties database.
        String[][] paramInfo = getParameterInfo();
        for (int i = 0; i < paramInfo.length; i++) {
            String key = paramInfo[i][0];
            String value = getParameter(key);
            if(value != null) {
                props.put(key, value);
            }
        }
        
        return props;
    }
    
    protected abstract TrialLogger initTrialLogger();
    
    protected void logError(Throwable ex, String msg, Object... args) {
        msg = String.format(msg, args);
        LogContext.getLogger().log(Level.SEVERE, msg, ex);
        add(new JLabel(msg));
    }

}  //  @jve:decl-index=0:visual-constraint="10,2"
