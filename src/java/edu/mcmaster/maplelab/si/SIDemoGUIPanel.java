package edu.mcmaster.maplelab.si;

import edu.mcmaster.maplelab.av.AVDemoGUIPanel;
import edu.mcmaster.maplelab.av.datamodel.AVBlockType;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

/**
 * Demo panel for SI experiment.
 * 
 * @author bguseman
 */
public class SIDemoGUIPanel extends AVDemoGUIPanel<SITrial> {

	public SIDemoGUIPanel(AVSession<?, SITrial, ?> session) {
		super(session);
	}

	@Override
	protected SITrial createTrial(AVBlockType type, AnimationSequence animationSequence,
			MediaWrapper<Playable> media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots, Long mediaDelay) {
		return new SITrial(type, animationSequence, media, timingOffset,
				animationPoints, diskRadius, connectDots, mediaDelay);
	}
}
