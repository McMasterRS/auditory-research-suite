/*
* Copyright (C) 2006 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: Instructions.java 468 2008-12-04 20:42:37Z bhocking $
*/
package edu.mcmaster.maplelab.common.gui;



/**
 * Container for displaying instructions or other info.
 * 
 * 
 * @version $Revision: 468 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class Instructions extends BasicStep {

    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -7643372937718112227L;

	/**
     * This is the default constructor
     */
    public Instructions() {
        this(true);
    }
    
    public Instructions(boolean showPrevNext) {
        super(showPrevNext);
        initialize();
    }

    private void initialize() {
        setTitleText("Instructions");
        setInstructionText("Instructions go here.");
    }
}
