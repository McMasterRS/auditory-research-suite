/*
 * Copyright (c) 2009 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.common.util;

/**
 * Interface for classes interested in receiving progress information, usually
 * from a worker thread.
 *
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @version $Revision: 23558 $
 */
public interface ProgressListener {
    /**
     * Called when the progress message has changed.
     */
    void messageChanged(String message);
    /**
     * Called when the progress percentage has changed.
     */
    void percentChanged(int percent);

    /**
     * Called when the extent of the progress is indeterminate, but ongoing.
     */
    void progressUnknown();

    /** Called when the operation is finished. Provides no indication on
     *  success or failure, just that the operation has stopped.
     */
    void progressComplete();

    /** A do-nothing implementation of ProgressListener */
    public static class NullProgressListener implements ProgressListener {
        public void messageChanged(String message) {
            // do nothing
        }

        public void percentChanged(int percent) {
            // do nothing
        }

        public void progressComplete() {
            // do nothing
        }

        public void progressUnknown() {
            // do nothing
        }
    }
}
