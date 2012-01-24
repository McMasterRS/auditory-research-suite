/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */

package edu.mcmaster.maplelab.toj;

import edu.mcmaster.maplelab.av.AVDemoGUIPanel;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * This class creates a TOJDemoGUIPanel, which allows the user to run a TOJ demo without the setup screen.
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class TOJDemoGUIPanel extends AVDemoGUIPanel<TOJTrial> {

	public TOJDemoGUIPanel(AVSession<?, TOJTrial, ?> session) {
		super(session);
	}

	@Override
	protected TOJTrial createTrial(AnimationSequence animationSequence,
			boolean isVideo, MediaWrapper<Playable> media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots) {
		return new TOJTrial(animationSequence, isVideo, media, timingOffset,
				animationPoints, diskRadius, connectDots);
	}
	
}
