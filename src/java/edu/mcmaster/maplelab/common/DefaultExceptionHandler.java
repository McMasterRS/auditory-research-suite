package edu.mcmaster.maplelab.common;

import java.util.logging.Level;

import javax.swing.JOptionPane;
    
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final boolean _guiMessageOnException;
    public DefaultExceptionHandler(boolean guiMessageOnException) {
        _guiMessageOnException = guiMessageOnException;
    }
    public void uncaughtException(Thread t, Throwable e) {
        LogContext.getLogger().log(Level.SEVERE, "Uncaught exception in thread " + t.getName(), e);
        
        if(_guiMessageOnException) {
            StringBuilder buf = new StringBuilder();
            for(StackTraceElement frame : e.getStackTrace()) {
                buf.append(frame.toString());
                buf.append("\n");
            }
            JOptionPane.showMessageDialog(null, 
                String.format("Uncaught exception in thread: %s%n%s%n%s", 
                    t.getName(), e, buf.toString()),
                "Uncaught error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
    