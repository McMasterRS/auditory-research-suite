/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: InvokeOnAWTQueueRunnable.java 399 2008-01-11 22:20:55Z sfitch $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.EventQueue;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Wrapper for running a Runnable on the event queue.
 * 
 * 
 * @version $Revision: 399 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Sep 19, 2007
 */
public final class InvokeOnAWTQueueRunnable implements Runnable {
    private final Runnable _target;
    private final boolean _waitOnAWT;

    public InvokeOnAWTQueueRunnable(Runnable target, boolean waitOnAWT) {
        _target = target;
        _waitOnAWT = waitOnAWT;
    }

    public final void run() {
        if(EventQueue.isDispatchThread()) {
            _target.run();
        }
        else {
            if(_waitOnAWT) {
                try {
                    EventQueue.invokeAndWait(_target);
                }
                catch (Exception ex) {
                    UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
                    if(handler != null) {
                        handler.uncaughtException(Thread.currentThread(), ex);
                    }
                    else {
                        ex.printStackTrace();
                    }
                }
            }
            else {
                EventQueue.invokeLater(_target);
            }
        }
    }
}
