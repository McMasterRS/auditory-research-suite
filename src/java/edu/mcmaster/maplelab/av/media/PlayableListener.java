package edu.mcmaster.maplelab.av.media;

import java.util.EventObject;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

/**
 * Listener class for Playable objects.  Add additional interface implementations
 * as necessary, and send all relevant activity to {@link #playableEnded(EventObject)}.
 * 
 * @author bguseman
 *
 */
public abstract class PlayableListener implements LineListener {

	@Override
	public void update(LineEvent event) {
		if (event.getType() == Type.STOP) playableEnded();
	}
	
	/**
	 * Method called when playback has ended.
	 */
	public abstract void playableEnded();

}
