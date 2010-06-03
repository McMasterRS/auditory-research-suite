/*
 * Copyright (c) 2005-2008 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.common.util;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;


/**
 *  Class encapsulating the work of notifying listeners of progress update,
 *  ensuring the update orrus in the event dispatch thread.
 *
 * @version $Revision: 24064 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Feb 16, 2007
 */
public class EventThreadProgressNotifier implements Runnable, ProgressListener {
    private static final String NULL_MSG = "\u0000";

    /** Queue of update events to process. */
    private Queue<Update> _queue = new ConcurrentLinkedQueue<Update>();

    /** Listeners interested in progress updates. */
    private List<ProgressListener> _progressListeners = Collections.synchronizedList(
        new ArrayList<ProgressListener>());

    public EventThreadProgressNotifier() {}


    /** Method that does the work of notification. Should not be
     * called from external class. */
    public void run() {

        // TODO: consider grabbing the last message and status items.
        assert SwingUtilities.isEventDispatchThread();
        Update curr;
        while((curr = _queue.peek()) != null) {
            _queue.remove();
            // Create a copy as calls to listeners could potentially result in
            // removal from listener list.
            ProgressListener[] copy = _progressListeners.toArray(new ProgressListener[_progressListeners.size()]);
            for(ProgressListener l : copy) {
                assert l != null;

                if(curr.isComplete()) {
                    l.progressComplete();
                }
                else {
                    if(curr.isIndeterminate()) {
                        l.progressUnknown();
                    }
                    if(curr.hasPercentUpdate()) {
                        int p = curr._percent;
                        l.percentChanged(p);
                    }
                    if(curr.hasMessageUpdate()) {
                        String msg = curr._message;
                        l.messageChanged(msg);
                    }
                }
            }
        }
    }


    /** Method to do the notification. */
    private void post(Update update) {
        _queue.offer(update);
        SwingUtilities.invokeLater(this);
    }

    /**
     * {@inheritDoc}
     * @see org.swri.common.ui.ProgressListener#messageChanged(java.lang.String)
     */
    public void messageChanged(String message) {
        post(new Update(message));
    }

    /**
     * {@inheritDoc}
     * @see org.swri.common.ui.ProgressListener#percentChanged(int)
     */
    public void percentChanged(int percentage) {
        // Clamp to range of 0 to 100.
        post(new Update(Math.max(Math.min(0,percentage),100)));
    }


    public void progressUnknown() {
        post(new Update(true));
    }

    /**
     * Post finished notification.
     * {@inheritDoc}
     * @see org.swri.common.ui.ProgressListener#progressComplete()
     */
    public void progressComplete() {
        post(new Update(false));
    }


    /**
     * Add a party interested in progress updates. Listeners are garanteed
     * to have their methods called in the AWTEventThread.
     */
    public void addProgressListener(ProgressListener l) {
        _progressListeners.add(l);
    }

    /**
     * Remove an instance registered for progress updates.
     */
    public void removeProgressListener(ProgressListener l) {
        _progressListeners.remove(l);
    }

    /** Wrapper for progress update data. */
    private static class Update {
        private final String _message;
        private final int _percent;
        private final boolean _indeterminate;

        /**
         * Ctor for complete event.
         */
        public Update(boolean indeterminate) {
            _indeterminate = indeterminate;
            _message = NULL_MSG;
            _percent = Integer.MIN_VALUE;
        }

        /**
         * Ctor for message change.
         *
         * @param message message change.
         */
        public Update(String message) {
            _indeterminate = false;
            _message = message;
            _percent = Integer.MIN_VALUE;
        }

        /**
         * Ctor for percentage update.
         */
        public Update(int percent) {
            _indeterminate = false;
            _message = NULL_MSG;
            _percent = percent;
        }

        public boolean isIndeterminate() {
            return _indeterminate;
        }

        public boolean hasMessageUpdate() {
            return _message != NULL_MSG;
        }

        public boolean hasPercentUpdate() {
            return _percent != Integer.MIN_VALUE;
        }

        public boolean isComplete() {
            return !isIndeterminate() && !hasMessageUpdate() && !hasPercentUpdate();
        }
    }
}
