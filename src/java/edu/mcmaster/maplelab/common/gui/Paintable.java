/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: Paintable.java 399 2008-01-11 22:20:55Z sfitch $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.Graphics2D;

/**
 * Any instance suppporting a <code>paint(Graphics2D)</code> method.
 *
 *
 * @version $Revision: 399 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Nov 29, 2007
 */
public interface Paintable {
    void paint(Graphics2D g);
    void paintBorder(Graphics2D g);
    void setVisible(boolean visible);
    double getRadius();
}
