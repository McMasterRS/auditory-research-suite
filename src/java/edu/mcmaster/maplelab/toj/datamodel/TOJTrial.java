/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;

/**
 * TOJ specific implementation of Trial.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class TOJTrial extends AVTrial<ConfidenceResponse> {

	public TOJTrial(AnimationSequence animationSequence, boolean isVideo, MediaWrapper<Playable> media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots, Long mediaDelay) {
		
		super(animationSequence, isVideo, media, timingOffset, animationPoints, 
				diskRadius, connectDots, mediaDelay);
	}

	@Override
	public boolean isResponseCorrect() {
		ConfidenceResponse response = getResponse();
        if (response != null) {
        	// if offset==0, either is correct
            if (TOJResponseParameters.isDotFirst(response)) {
                return getOffsetNanos() >= 0;
            }
            else {
                return getOffsetNanos() <= 0;
            }
                
        }
        return false;
	}
}
