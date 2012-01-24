package edu.mcmaster.maplelab.si.datamodel;

import java.util.List;

import edu.mcmaster.maplelab.av.datamodel.AVBlock;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;

public class SIBlock extends AVBlock<SISession, SITrial> {

	protected SIBlock(SISession session, int blockNum, AVBlockType type,
			List<Long> offsets, List<Integer> numPoints) {
		super(session, blockNum, type, offsets, numPoints);
	}

	@Override
	protected SITrial createTrial(AnimationSequence animationSequence,
			boolean isVideo, MediaWrapper<Playable> media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots) {
		return new SITrial(animationSequence, isVideo, media, timingOffset, animationPoints, 
				diskRadius, connectDots);
	}

}
