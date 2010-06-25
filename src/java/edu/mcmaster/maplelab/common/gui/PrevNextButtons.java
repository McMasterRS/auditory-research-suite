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
package edu.mcmaster.maplelab.common.gui;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;


/**
 * Wrapper around layout and control of buttons for prev/next operation.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   May 10, 2006
 */
public class PrevNextButtons extends JPanel {

    private JPanel jPanel = null;
    /**
     * @uml.property  name="prev"
     */
    private JButton prev = null;
    /**
     * @uml.property  name="next"
     */
    private JButton next = null;
    /**
     * @uml.property  name="jSeparator"
     */
    private JSeparator jSeparator = null;
    private StepManager _steps;

    /**
     * This is the default constructor
     */
    public PrevNextButtons() {
        super();
        initialize();
        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                updateVisible();
            }
        });
        
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                updateVisible();
            }
        });
    }
    
    public void addNotify() {
        super.addNotify();
        updateVisible();
    }
    
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        updateVisible();
    }
    
    private void updateVisible() {
        if(_steps != null) {
            getPrev().setEnabled(_steps.hasPrevious());
            JButton next = getNext();
            next.setVisible(_steps.hasNext());
            if(next.isVisible()) {
                JRootPane rootPane = SwingUtilities.getRootPane(next);
                if(rootPane != null) {
                    rootPane.setDefaultButton(next);
                }
            }
        }
    }
    
    public void setStepManager(StepManager steps) {
        _steps = steps;
    }
    
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
        getPrev().setEnabled(enabled);
        getNext().setEnabled(enabled);
        updateVisible();
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJSeparator(), java.awt.BorderLayout.NORTH);
        this.add(getCenter(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getCenter() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.add(getPrev(), null);
            jPanel.add(getNext(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton	
     * @return  javax.swing.JButton
     * @uml.property  name="prev"
     */
    private JButton getPrev() {
        if (prev == null) {
            prev = new JButton();
            prev.setText("Previous");
            prev.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
            prev.setIcon(new ImageIcon(getClass().getResource("previous.png")));
            prev.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if(_steps != null) {
                        _steps.previous();
                    }
                }
            });
        }
        return prev;
    }

    /**
     * This method initializes jButton1	
     * @return  javax.swing.JButton
     * @uml.property  name="next"
     */
    private JButton getNext() {
        if (next == null) {
            next = new JButton();
            next.setText("Next");
            next.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
            next.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
            next.setIcon(new ImageIcon(getClass().getResource("next.png")));
            next.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if(_steps != null) {
                        _steps.next();
                    }
                }
            });
        }
        return next;
    }

    /**
     * This method initializes jSeparator	
     * @return  javax.swing.JSeparator
     * @uml.property  name="jSeparator"
     */
    private JSeparator getJSeparator() {
        if (jSeparator == null) {
            jSeparator = new JSeparator();
        }
        return jSeparator;
    }

}
