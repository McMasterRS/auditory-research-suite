/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;

/**
 * TOJ specific implementation of Trial.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class TOJTrial extends AVTrial<ConfidenceResponse> {

	public TOJTrial(AnimationSequence animationSequence, boolean isVideo, PlayableMedia media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots) {
		
		super(animationSequence, isVideo, media, timingOffset, animationPoints, diskRadius, connectDots);
	}

	@Override
	public boolean isResponseCorrect() {
		ConfidenceResponse response = getResponse();
        if (response != null) {
        	// if offset==0, either is correct
            if (TOJResponseParameters.isDotFirst(response)) {
                return getOffset() >= 0;
            }
            else {
                return getOffset() <= 0;
            }
                
        }
        return false;
	}
}
