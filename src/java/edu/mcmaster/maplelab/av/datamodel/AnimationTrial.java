package edu.mcmaster.maplelab.av.datamodel;

import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.animation.AnimationSource;
import edu.mcmaster.maplelab.common.datamodel.Trial;

public abstract class AnimationTrial<T> extends Trial<T> implements AnimationSource {

	@Override
	public abstract AnimationSequence getAnimationSequence();

	@Override
	public abstract int getNumPoints();

	@Override
	public abstract float getDiskRadius();

	@Override
	public abstract boolean isConnected();

	@Override
	public abstract boolean isResponseCorrect();

}
