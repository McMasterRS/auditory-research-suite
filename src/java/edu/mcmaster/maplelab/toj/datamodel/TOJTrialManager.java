package edu.mcmaster.maplelab.toj.datamodel;

import edu.mcmaster.maplelab.av.datamodel.AVBlockType;
import edu.mcmaster.maplelab.av.datamodel.AVTrialManager;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;

/**
 * @author bguseman
 *
 */
public class TOJTrialManager extends AVTrialManager<TOJSession, TOJTrial> {

	public TOJTrialManager(TOJSession session, boolean warmup) {
		super(session, warmup);
	}

	@Override
	protected TOJTrial createTrial(AVBlockType type,
			AnimationSequence animationSequence, MediaWrapper<Playable> media,
			Long timingOffset, int animationPoints, float diskRadius,
			boolean connectDots, Long mediaDelay) {
		
		return new TOJTrial(type, animationSequence, media, timingOffset, animationPoints,
				diskRadius, connectDots, mediaDelay);
	}

}
