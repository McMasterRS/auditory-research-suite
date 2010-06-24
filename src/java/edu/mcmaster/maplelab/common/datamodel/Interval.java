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

package edu.mcmaster.maplelab.common.datamodel;

import java.util.ArrayList;
import java.util.Random;


/**
 * Representation of a closed interval, or range.
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Dec 14, 2006
 */
public class Interval<T extends Number> {
    private final T _min;

    private final T _max;

    public Interval(T min, T max) {
        _min = min;
        _max = max;
        
        assert _min.doubleValue() <= _max.doubleValue();
    }
    
    /**
     * Copy constructor
     * 
     * @param toCopy
     */
    public Interval(Interval<T> toCopy) {
        this(toCopy.getMin(), toCopy.getMax());
    }

    /**
     * @return min value.
     */
    public T getMin() {
        return _min;
    }
    
    /**
     * @return max value
     */
    public T getMax() {
        return _max;
    }
    
    /**
     * Slice the interval up into equal parts
     */
    public ArrayList<Double> slice(int numSlices) {
    	ArrayList<Double> retval = new ArrayList<Double>();
    	double spacing = (width())/(numSlices-1);
    	for(int i = 0; i < numSlices; i++) {
    		retval.add(getMin().doubleValue()+spacing*i);    		
    	}
    	return retval;
    }
    
    /**
     * Compute the width of the interval as a double value.
     */
    public double width() {
        return getMax().doubleValue() - getMin().doubleValue();
    }
    
    /**
     * Determine if the given value fall inside the interval.
     */
    public boolean contains(T val) {
        // Why doesn't Number implement compareTo()??
        double dv = val.doubleValue();
        return _min.doubleValue() <= dv && dv <= _max.doubleValue();
    }
    

    /**
     * Select a random double value in the range (including bounds).
     * 
     * @param r Random number generator to use.
     * @return random value in [min,max].
     */
    public double randomInRange(Random r) {
        return getMin().doubleValue() + r.nextDouble() * width();
    }
    
    /**
     * If the given value is inside the interval, return it, otherwise return
     * the interval boundary value that is closest to the value.
     */
    public T clamp(T val) {
        double dv = val.doubleValue();
        
        if(dv < _min.doubleValue()) {
            return _min;
        }
        else if(dv > _max.doubleValue()) {
            return _max;
        }
        else {
            return val;
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s,%s]", getMin(), getMax());
    }

}
