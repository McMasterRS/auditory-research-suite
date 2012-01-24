package edu.mcmaster.maplelab.toj.datamodel;

import java.util.List;

import edu.mcmaster.maplelab.av.datamodel.AVBlock;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;

public class TOJBlock extends AVBlock<TOJSession, TOJTrial> {
	
	protected TOJBlock(TOJSession session, int blockNum, AVBlockType type, List<Long> offsets, 
			List<Integer> numPoints) {
		
		super(session, blockNum, type, offsets, numPoints);
	}

	@Override
	protected TOJTrial createTrial(AnimationSequence animationSequence,
			boolean isVideo, MediaWrapper<Playable> media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots) {
		return new TOJTrial(animationSequence, isVideo, media, timingOffset, animationPoints, 
				diskRadius, connectDots);
	}
}
