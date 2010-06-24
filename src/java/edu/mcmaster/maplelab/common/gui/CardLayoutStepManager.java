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

import java.awt.CardLayout;

import javax.swing.JComponent;

/**
 * Step manager implementation that adapts to a CardLayout.
 * @version       $Revision:$
 * @author       <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since       Jan 24, 2007
 * @uml.dependency   supplier="edu.mcmaster.maplelab.common.gui.BasicStep"
 */
public class CardLayoutStepManager implements StepManager {
    private final CardLayout _layout;
    private final JComponent _container;
    private int _step = 0;

    /**
     * Standard ctor
     * 
     * @param container container using CardLayout as layout manager.
     */
    public CardLayoutStepManager(JComponent container) {
        _container = container;
        if(! (_container.getLayout() instanceof CardLayout)) {
            throw new ClassCastException("Layout of provided container must be CardLayout");
        }
        
        _layout = (CardLayout) container.getLayout();
    }
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.StepManager#next()
     */
    public void next() {
        _step++;
        _layout.next(_container);
    }
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.StepManager#hasNext()
     */
    public boolean hasNext() {
        return _step < _container.getComponentCount();
    }

    /**
     * 
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.StepManager#previous()
     */
    public void previous() {
        _step--;
        _layout.previous(_container);
    }
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.StepManager#hasPrevious()
     */
    public boolean hasPrevious() {
        return _step > 0;
    }        
}