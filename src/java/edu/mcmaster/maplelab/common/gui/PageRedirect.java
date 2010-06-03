/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: PageRedirect.java 471 2008-12-05 15:08:27Z bhocking $
*/
package edu.mcmaster.maplelab.common.gui;

import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Session;


/**
 * Experiment step to redirect the applet container (browser) to a new page.
 * @version  $Revision: 471 $
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  May 10, 2006
 */
public class PageRedirect<S extends Session<?, ?>> extends BasicStep {
    
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = 7871705988240149015L;

	/**
     * @version   $Revision: 471 $
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    protected enum ConfigKeys {
        redirectURL,
        applicationRedirectURL
    }
    
    /**
     * @uml.property  name="session"
     */
    private final S _session;

    public PageRedirect(S session) {
        super(false);
        _session = session;
        setTitleText(null);
    }
    
    /**
     * @return
     * @uml.property  name="session"
     */
    protected S getSession() {
        return _session;
    }
    
    protected String makeRedirectPath() {
        return _session.getString(ConfigKeys.redirectURL, "/");
    }
    
    @Override
    protected void onVisible() {
        Applet applet = _session.getApplet();
        
        if(applet == null) {
            String loc = _session.getString(ConfigKeys.applicationRedirectURL, null);
            setInstructionText(String.format("<html>Please visit this location " +
                    "<a href=\"%1$s\">%1$s</a>.", loc));
            return;
        }
        
        String loc = makeRedirectPath();
        
        LogContext.getLogger().finest("redirect path: " + loc);
        
        URL base = applet.getDocumentBase();
        
        try {
            URL redirect = new URL(base, loc);
            LogContext.getLogger().fine("--> Redirecting: " + redirect);
            applet.getAppletContext().showDocument(redirect);
        }
        catch (MalformedURLException e) {
            LogContext.getLogger().log(Level.SEVERE, 
                String.format("Couldn't redirect to '%s' (relative to %s)", loc, base), e);
        }
    }
}
