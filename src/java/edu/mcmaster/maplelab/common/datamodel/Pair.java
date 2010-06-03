/*
* Copyright (C) 2008 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/

package edu.mcmaster.maplelab.common.datamodel;


/**
 * A 2-Tuple container. 
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Jan 14, 2008
 */
public class Pair<T1, T2> {
    protected T1 _first;
    protected T2 _last;

    public Pair(T1 first, T2 last) {
        _first = first;
        _last = last;
    }

    /**
     * Get first element in pair.
     */
    public T1 getFirst() {
        return _first;
    }

    /**
     * Get second element in pair.
     * 
     * @return
     */
    public T2 getLast() {
        return _last;
    }
}
