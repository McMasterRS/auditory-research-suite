package edu.mcmaster.maplelab.si.datamodel;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.common.datamodel.MultiResponse;

public class SITrial extends AVTrial<MultiResponse> {

	public SITrial(AnimationSequence animationSequence, boolean isVideo, PlayableMedia media, 
			Long timingOffset, int animationPoints, float diskRadius, boolean connectDots) {
		
		super(animationSequence, isVideo, media, timingOffset, animationPoints, diskRadius, connectDots);
	}

	@Override
	public boolean isResponseCorrect() {
		/*MultiResponse responses = getResponse();
		if (responses != null) {
			PlayableMedia media = getMedia();
			DurationEnum audioDur = media.getValue(MediaParams.audioDuration);
			DurationEnum responseDur = SIResponseParameters.getDuration(responses.getResponse(0));
			
			return audioDur != null && audioDur == responseDur;
		}*/
		return false;
	}

}
