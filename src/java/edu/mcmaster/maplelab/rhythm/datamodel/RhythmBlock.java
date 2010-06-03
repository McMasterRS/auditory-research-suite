/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id: RhythmBlock.java 474 2009-03-20 17:53:30Z bhocking $
 */
package edu.mcmaster.maplelab.rhythm.datamodel;

import java.util.*;

import edu.mcmaster.maplelab.common.datamodel.Block;

/**
 * Specialization of Block for Pitch Magnitude experiment.
 * @version  $Revision: 474 $
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Sep 7, 2006
 */
public class RhythmBlock extends Block<RhythmSession, RhythmTrial> {
    public RhythmBlock(RhythmSession session, int blockNum, boolean withTap, int baseIOI) {
        super(session, blockNum);
        
        _trials = new ArrayList<RhythmTrial>();
        
        List<Float> offsetDegrees = session.getOffsetDegrees();
        
        for(Float offset : offsetDegrees) {
            RhythmTrial t = new RhythmTrial(baseIOI, offset, withTap);
            _trials.add(t);
        }
        
        Collections.shuffle(_trials);
        
        assignTrialNumbers();
    }

    /**
     * {@inheritDoc}  
     * @see  edu.mcmaster.maplelab.common.datamodel.Block#getTrials()
     * @uml.property  name="trials"
     */
    @Override
    public List<RhythmTrial> getTrials() {
        return _trials;
    }
}
