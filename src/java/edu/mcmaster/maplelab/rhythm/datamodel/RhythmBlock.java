/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id$
 */
package edu.mcmaster.maplelab.rhythm.datamodel;

import java.util.*;

import edu.mcmaster.maplelab.common.datamodel.Block;

/**
 * Specialization of Block for Pitch Magnitude experiment.
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Sep 7, 2006
 */
public class RhythmBlock extends Block<RhythmSession, RhythmTrial> {
    private List<RhythmTrial> _trials;

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
     * Reduce the number of trials in this block to the given number. If number
     * is greater than current number then call is ignored. Used for constructing
     * warmup blocks.
     * TODO: refactor into Block
     * 
     * @param numWarmupTrials number of trials to clip to.
     */
    public void clipTrials(int numWarmupTrials) {
        if(numWarmupTrials < _trials.size()) {
            _trials = _trials.subList(0, numWarmupTrials);
        }
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
