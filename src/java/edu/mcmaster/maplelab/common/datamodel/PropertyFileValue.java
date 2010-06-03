/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: PropertyFileValue.java 399 2008-01-11 22:20:55Z sfitch $
*/

package edu.mcmaster.maplelab.common.datamodel;


/**
 * Interface for classes providing a special string representation
 * for string in properties file.
 * 
 * @version $Revision: 399 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Dec 21, 2007
 */
public interface PropertyFileValue {
    String toPropertyValueFormat();
}
