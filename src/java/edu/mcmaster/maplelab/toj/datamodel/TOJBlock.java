package edu.mcmaster.maplelab.toj.datamodel;

import java.util.List;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVBlock;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

public class TOJBlock extends AVBlock<TOJSession, TOJTrial> {
	
	protected TOJBlock(TOJSession session, int blockNum, AVBlockType type, List<DurationEnum> visDuration, 
			List<NotesEnum> pitches, List<String> frequencies, List<String> spectrums, 
			List<String> envDurations, List<DurationEnum> audDurations, List<String> videoExtensions,
			List<Long> offsets, List<Integer> numPoints) {
		
		super(session, blockNum, type, visDuration, pitches, frequencies, spectrums, envDurations,
				audDurations, videoExtensions, offsets, numPoints);
	}

	@Override
	protected TOJTrial createTrial(AnimationSequence animationSequence,
			boolean isVideo, PlayableMedia media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots) {
		return new TOJTrial(animationSequence, isVideo, media, timingOffset, animationPoints, 
				diskRadius, connectDots);
	}
}
