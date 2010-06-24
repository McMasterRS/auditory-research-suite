/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.*;


/**
 * Convenience specialization of JButton which sends a window close event
 * to the button's container when clicked.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Dec 20, 2007
 */
public class CloseButton extends JButton {
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
