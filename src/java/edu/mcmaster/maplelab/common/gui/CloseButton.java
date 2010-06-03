/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: CloseButton.java 474 2009-03-20 17:53:30Z bhocking $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * Convenience specialization of JButton which sends a window close event
 * to the button's container when clicked.
 * 
 * @version $Revision: 474 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Dec 20, 2007
 */
public class CloseButton extends JButton {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -5576266340739379433L;

	/**
     * Default ctor. Button will have text "Close".
     */
    public CloseButton() {
        super(new CloseWindowAction());
    }
    
    /**
     * Text providing ctor.
     * 
     * @param text string to show in label area of button.
     */
    public CloseButton(String text) {
        this();
        setText(text);
    }
    
    /**
     * Creates an OK/Cancel pair (Cancel exits)
     * 
     * @param hostDialog Where to create the close Panel
     * @param l ActionListener for close (OK) button
     */
    public static void createClosePanel(JDialog hostDialog, ActionListener l) {
        CloseButton close = new CloseButton("OK");
        JPanel closePanel = new JPanel();
        closePanel.add(close);
        hostDialog.getRootPane().setDefaultButton(close);
        if (l!=null) close.addActionListener(l);
        
        JButton b = new JButton("Cancel");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        closePanel.add(b);
        hostDialog.getContentPane().add(closePanel, BorderLayout.SOUTH);
    }
    
    /**
     * Send a WINDOW_CLOSING event to the given Window/Frame.
     *
     * @param window Window to send event to.
     */
    public static void sendCloseEvent(Window window) {
        WindowEvent event = new WindowEvent(
            window, WindowEvent.WINDOW_CLOSING);
        window.dispatchEvent(event);
    }
    
    public static class CloseWindowAction extends AbstractAction {
        /**
		 * Automatically generated serial version UID
		 */
		private static final long serialVersionUID = 6594295363033600346L;
		public CloseWindowAction() {
            super("Close");
        }
        public void actionPerformed(ActionEvent e) {
            Component c = (Component) e.getSource();
            Window win = SwingUtilities.windowForComponent(c);
            sendCloseEvent(win);
        }
    }
}
