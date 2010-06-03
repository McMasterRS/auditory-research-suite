package edu.mcmaster.maplelab.common.util;
import javax.swing.SwingUtilities;

/**
 * An abstract class that you subclass to perform
 * GUI-related work in a dedicated thread.
 * For instructions on using this class, see
 * http://java.sun.com/products/jfc/swingdoc-current/threads2.html
 *
 * Additional status reporting features and delayed starting features added
 * by: <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 *
 */
public abstract class CustomSwingWorker<T> implements ProgressListener {
    private static ThreadGroup DEF_GROUP = new ReportingThreadGroup("WorkerThreads");

    /** Count of workers created. */
    private static int _count = 1;
    /** The value created from the construction stage.
     *  see getValue(), setValue()*/
    private T value = null;

    /** Currently active progress notification. Used to keep the event queue
     *  from filling up with updates. */
    protected EventThreadProgressNotifier _notifier = new EventThreadProgressNotifier();

    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }

    /** Thread running this worker. */
    protected ThreadVar _threadVar = null;

    /**
     * Get the value produced by the worker thread, or null if it
     * hasn't been constructed yet.
     */
    protected synchronized T getValue() {
        return value;
    }

    /**
     * Set the value produced by worker thread
     */
    protected synchronized void setValue(T x) {
        value = x;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */
    public abstract T construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() { // do nothing
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to abort what it's doing.
     */
    public void interrupt() {
        Thread t = _threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        _threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.
     * Returns null if either the constructing thread or
     * the current thread was interrupted before a value was produced.
     *
     * @return the value created by the <code>construct</code> method
     */
    public T get() {
        while (true) {
            Thread t = _threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    /**
     *  Run in the current thread rather than a separate thread.
     *
     */
    public void runInCurrentThread() {
        setValue(construct());
        finished();
    }

    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public void start() {
        final Runnable doFinished = new Runnable() {
           public void run() {
               finished();
               _notifier.progressComplete();
           }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                try {
                    setValue(construct());
                }
                finally {
                    _threadVar.clear();
                }

                SwingUtilities.invokeLater(doFinished);
            }
        };

        Thread t = new Thread(DEF_GROUP, doConstruct);
        t.setPriority(Thread.MIN_PRIORITY);
        _threadVar = new ThreadVar(t);

        String name = getClass().getName() + " (CustomSwingWorker-" + _count++ + ")";
        t.setName(name);
        t.start();
    }

    /**
     * Add a party interested in progress updates. Listeners are guaranteed
     * to have their methods called in the AWTEventThread.
     */
    public void addProgressListener(ProgressListener l) {
        // Pass to delegate.
        _notifier.addProgressListener(l);
    }

    /**
     * Remove an instance registered for progress updates.
     */
    public void removeProgressListener(ProgressListener l) {
        // Handled by delegate.
        _notifier.removeProgressListener(l);
    }

    /**
     * Method for subclasses to notify any interested listeners that the
     * progress message has changed. The listeners will be notified
     * via the AWTEventThread.
     *
     *
     * @param Progress message.
     */
    public void messageChanged(String message) {
        _notifier.messageChanged(message);

    }

    /**
     * Method for subclasses to notify any interested listeners that
     * the progress percentage has changed. The listeners will be notified
     * via the AWTEventThread.
     *
     * @param percentage Percent complete (between [0, 100]).
     */
    public void percentChanged(int percentage) {
        _notifier.percentChanged(percentage);
    }

    /**
     * Method for subclasses to notify interested listeners that the
     * operation is complete. The listeners will be notified
     * via the AWTEventThread.
     *
     * {@inheritDoc}
     * @see org.swri.common.ui.ProgressListener#progressComplete()
     */
    public void progressComplete() {
        _notifier.progressComplete();
    }

    /**
     * Called when the extent of the progress is indeterminate, but ongoing.
     * {@inheritDoc}
     * @see org.swri.common.ui.ProgressListener#progressUnknown()
     */
    public void progressUnknown() {
        _notifier.progressUnknown();
    }

    /**
     * Test code.
     */
    public static void main(String[] args) {
    }
 }
