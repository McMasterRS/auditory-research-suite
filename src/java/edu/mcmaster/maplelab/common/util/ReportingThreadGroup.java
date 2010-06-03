/*
 * Copyright (c) 2004-2005 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.common.util;

import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;


/**
 * Thread group that reports uncaught exceptions via the
 * ExceptionMessageDialog class.
 *
 * @version $Revision: 21873 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Oct 6, 2003
 */
public class ReportingThreadGroup extends ThreadGroup {

    /**
     * @inheritdoc
     */
    public ReportingThreadGroup(String name) {
        super(name);
    }


    /**
     * @inheritdoc
     */
    public ReportingThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    /**
     * @inheritdoc
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LogContext.getLogger().log(Level.SEVERE,
            "Uncaught exception", e);
    }
}
