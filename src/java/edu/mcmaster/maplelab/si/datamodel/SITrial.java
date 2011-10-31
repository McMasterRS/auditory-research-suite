package edu.mcmaster.maplelab.si.datamodel;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.MediaParams;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.Response;

public class SITrial extends AVTrial<Response[]> {

	public SITrial(AnimationSequence animationSequence, boolean isVideo, PlayableMedia media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots) {
		
		super(animationSequence, isVideo, media, timingOffset, animationPoints, diskRadius, connectDots);
	}

	@Override
	public boolean isResponseCorrect() {
		Response[] responses = getResponse();
		if (responses != null) {
			PlayableMedia media = getMedia();
			DurationEnum actualDur = media.getValue(MediaParams.audioDuration);
			DurationEnum responseDur = SIResponseParameters.getDuration(responses[0]);
			return actualDur != null && actualDur == responseDur;
		}
		return false;
	}

}
