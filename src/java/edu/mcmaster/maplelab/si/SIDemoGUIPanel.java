package edu.mcmaster.maplelab.si;

import edu.mcmaster.maplelab.av.AVDemoGUIPanel;
import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
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
	protected SITrial createTrial(AnimationSequence animationSequence,
			boolean isVideo, PlayableMedia media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots) {
		return new SITrial(animationSequence, isVideo, media, timingOffset,
				animationPoints, diskRadius, connectDots);
	}
}
