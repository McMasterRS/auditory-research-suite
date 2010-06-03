/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: LabelFieldConstraints.java 468 2008-12-04 20:42:37Z bhocking $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.*;

import javax.swing.JLabel;


/**
 * Specialization of {@link GridBagConstraints} with convenience methods for
 * laying out label/field ordered forms.
 * 
 * @version $Revision: 468 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Dec 20, 2007
 */
public class LabelFieldConstraints extends GridBagConstraints {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = 7918041402309825166L;

	public LabelFieldConstraints() {
        insets = new Insets(2, 2, 2, 2);
        gridwidth = 1;
        gridheight = 1;
        gridy = RELATIVE;
    }
    
    public LabelFieldConstraints forLabel() {
        anchor = EAST;
        weightx = 0.0;
        weighty = 0.0;
        gridx = 0;
        gridwidth = 1;
        fill = NONE;

        return this;
    }
    
    public LabelFieldConstraints forField() {
        anchor = CENTER;
        weightx = 1.0;
        weighty = 0.0;
        fill = HORIZONTAL;
        gridx = 1;
        gridwidth = 1;

        return this;
    }

    public void addFiller(Container owner) {
        forLabel();
        fill = LabelFieldConstraints.HORIZONTAL;
        weighty = 1;
        owner.add(new JLabel(), this);
    }
}
