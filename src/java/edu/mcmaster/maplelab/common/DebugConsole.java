/*
 * Copyright (C) 2006-2008 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id:DebugConsole.java 399 2008-01-11 22:20:55Z sfitch $
 */
package edu.mcmaster.maplelab.common;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.util.Scanner;
import java.util.logging.*;

import javax.swing.*;

/**
 * Simple frame displaying log messages.
 * @version  $Revision:399 $
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  May 10, 2006
 */
public class DebugConsole extends JFrame {

    private JPanel jContentPane = null;
    private JTextArea console = null;
    private JScrollPane consoleScrollPane = null;

    /**
     * This is the default constructor
     */
    public DebugConsole() {
        super(selectScreenConfiguration());
        initialize();
    }

    /**
     * If running in a multi-screen environment, select the secondary
     * screen for the console home.
     */
    private static GraphicsConfiguration selectScreenConfiguration() {
        GraphicsConfiguration retval = null;
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        
        if(gs != null && gs.length > 0) {
            int index = gs.length > 1 ? 1 : 0;
            retval = gs[index].getDefaultConfiguration();
        }
        
        return retval;
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(640, 300);
        this.setContentPane(getJContentPane());
        this.setTitle("Debugging Console");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        try {
            LogContext.getLogger().addHandler(new LogHandler());
        }
        catch(AccessControlException ex) {
            LogContext.getLogger().warning("Couldn't attach console to logger");
        }        
    }
    
    /**
     * This method initializes jContentPane
     * @return  javax.swing.JPanel
     * @uml.property  name="jContentPane"
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jTextArea	
     * @return  javax.swing.JTextArea
     * @uml.property  name="console"
     */
    private JTextArea getConsole() {
        if (console == null) {
            console = new JTextArea();
            console.setWrapStyleWord(true);
            console.setLineWrap(true);
            console.setEditable(false);
            console.setTabSize(4);
            console.setFont(new Font("monospaced", Font.PLAIN, 12));
        }
        return console;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if (consoleScrollPane == null) {
            consoleScrollPane = new JScrollPane();
            consoleScrollPane.setViewportView(getConsole());
        }
        return consoleScrollPane;
    }
    
    private class LogHandler extends Handler {
        public LogHandler() {
        }
        @Override
        public void close() throws SecurityException {
            DebugConsole.this.setVisible(false);
        }

        @Override
        public void flush() {
            getConsole().append("\n");
        }

        @Override
        public void publish(final LogRecord record) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    putText(record, record.getMessage());
                    
                    if(record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                        Throwable thrown = record.getThrown();
                        if(thrown != null) {
                            StringWriter buf = new StringWriter();
                            thrown.printStackTrace(new PrintWriter(buf));
                            putText(record, buf.toString());
                        }
                        
                        if(!DebugConsole.this.isVisible()) {
                            DebugConsole.this.setVisible(true);
                        }
                    }
                    
                    JScrollBar sb = getJScrollPane().getVerticalScrollBar();
                    sb.setValue(sb.getMaximum());
                }

                /**
                 * 
                 * @param record
                 * @param console
                 */
                private void putText(final LogRecord record, String text) {
                    Level level = record.getLevel();
                    String levelStr;
                    if(level == Level.FINE) {
                        levelStr = "DBG->";
                    }
                    else if(level == Level.FINER) {
                        levelStr = "DBG-->";
                    }
                    else if(level == Level.FINEST) {
                        levelStr = "DBG--->"; 
                    }
                    else {
                        levelStr = record.getLevel().toString();                        
                    }
                    
                    JTextArea console = getConsole();
                    
                    if(text == null) {
                        text = "Null text from " + record.getSourceClassName() + "." + record.getSourceMethodName();
                    }
                    
                    Scanner scan = new Scanner(text);
                    while(scan.hasNextLine()) {
                        String msg = String.format("%7s: %s%n", levelStr, scan.nextLine());
                        console.append(msg);
                    }
                }
            });
        }
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
