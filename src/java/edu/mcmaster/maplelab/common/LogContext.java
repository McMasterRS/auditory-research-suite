/*
* Copyright (C) 2006 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/
package edu.mcmaster.maplelab.common;

import java.security.AccessControlException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Wrapper around getting a consistent logger.
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class LogContext {
    private static final String LOG = "edu.mcmaster.maplelab";
    
    private static Logger _logger = null;

    /**
     * Convenience method doing the same thing as LogContext.getLogger();
     * 
     * @return Logger to use for this scope.
     */
    
    public synchronized static Logger getLogger() {
        if(_logger == null) {
            // Select the logger for which we have permission to control.
            try {
                LogManager.getLogManager().checkAccess();
                _logger = Logger.getLogger(LOG);
            }
            catch(AccessControlException ex) {
                _logger = Logger.getAnonymousLogger();
            }            
        }
        return _logger;
    }    
}

