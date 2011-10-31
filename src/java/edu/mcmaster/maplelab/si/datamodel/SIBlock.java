package edu.mcmaster.maplelab.si.datamodel;

import java.util.List;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVBlock;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

public class SIBlock extends AVBlock<SISession, SITrial> {

	protected SIBlock(SISession session, int blockNum, AVBlockType type,
			List<DurationEnum> visDurations, List<NotesEnum> pitches,
			List<Long> offsets, List<Integer> numPoints) {
		super(session, blockNum, type, visDurations, pitches, offsets, numPoints);
	}

	@Override
	protected SITrial createTrial(AnimationSequence animationSequence,
			boolean isVideo, PlayableMedia media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots) {
		return new SITrial(animationSequence, isVideo, media, timingOffset, animationPoints, 
				diskRadius, connectDots);
	}

}
