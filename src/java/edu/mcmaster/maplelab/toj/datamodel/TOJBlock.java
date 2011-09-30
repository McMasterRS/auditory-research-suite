package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.nio.file;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.AVBlock;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.SoundClip;
import edu.mcmaster.maplelab.toj.animator.AnimationParser;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;

public class TOJBlock extends AVBlock<TOJSession, TOJTrial> {
	private static final float DISK_RADIUS = 0.3f;

	ArrayList<TOJTrial> _trials = null;
	
	protected TOJBlock(TOJSession session, int blockNum, AVBlockType type, List<String> strikes, 
			List<NotesEnum> pitches, List<String> frequencies, List<String> spectrums, 
			List<String> envDurations, List<Long> offsets, List<Integer> numPoints) {
		
		super(session, blockNum, type);
		
		_trials = new ArrayList<TOJTrial>();
		
		float animationAspect = session.getAnimationPointAspect();
		
		List<String> audioFileNames = generateAudioFileNames(frequencies, spectrums, envDurations);

		// create trials from session
		if (type == AVBlockType.AUDIO_VIDEO) {
			for (String strikeDur : strikes) {
				for (NotesEnum pitch : pitches) {
					String filename = pitch.toString().toLowerCase() + strikeDur.toLowerCase() + "_.txt";
					File dir = session.getExpectedVisualSubDir();
					AnimationSequence aniSeq = null;
					try {
						aniSeq = AnimationParser.parseFile(new File(dir, filename), animationAspect);
					}
					catch (FileNotFoundException fne) {
						LogContext.getLogger().warning(String.format("Animation file %s not found.", filename));
					}
					
					for (String audioName : audioFileNames) {
						dir = session.getExpectedAudioSubDir();
						Playable audio = SoundClip.findPlayable(audioName, dir, session.getPlaybackGain());
						
						for (Long so : offsets) {
							// look into sound objects
							for (int pts : numPoints) {
								TOJTrial trial = new TOJTrial(aniSeq, false, audio, so, pts, 
										DISK_RADIUS, session.connectDots());
								_trials.add(trial);
							}
						}
					}
				}
			}
		}
		else if (type == AVBlockType.AUDIO_ONLY) {
			for (String filename : audioFileNames) {
				File dir = session.getExpectedAudioSubDir();
				Playable audio = SoundClip.findPlayable(filename, dir, session.getPlaybackGain());
				
				for (Long so : offsets) {
					// look into sound objects
					TOJTrial trial = new TOJTrial(null, false, audio, so, 0, 
							DISK_RADIUS, session.connectDots());
					_trials.add(trial);
				}
			}
		}
		// TODO: support true video?
		else if (type == AVBlockType.VIDEO_ONLY) {
			for (String strikeDur : strikes) {
				for (NotesEnum pitch : pitches) {
					String filename = pitch.toString().toLowerCase() + strikeDur.toLowerCase() + "_.txt";
					File dir = session.getExpectedVisualSubDir();
					AnimationSequence aniSeq = null;
					try {
						aniSeq = AnimationParser.parseFile(new File(dir, filename), animationAspect);
					}
					catch (FileNotFoundException fne) {
						LogContext.getLogger().warning(String.format("Animation file %s not found.", filename));
					}
					
					for (Long so : offsets) {
						// look into sound objects
						TOJTrial trial = new TOJTrial(aniSeq, false, null, so, 0, 
								DISK_RADIUS, session.connectDots());
						_trials.add(trial);
					}
				}
			}
		}
		
		// randomize and number trials
		if (session.randomizeTrials()) Collections.shuffle(_trials);
		for (int i = 0; i < _trials.size(); i++) {
			_trials.get(i).setNum(i+1);
		}
	}
	
	/**
	 * Generate a list of audio file names to be used in the trials.
	 */
	private List<String> generateAudioFileNames(List<String> frequencies, List<String> spectrums, 
			List<String> envDurations) {
		List<String> retval = new ArrayList<String>();
		for (String freq : frequencies) {
			for (String spec : spectrums) {
				for (String envDur : envDurations) {
					retval.add(String.format("%s-%s-%s.wav", freq, spec, envDur));
				}
			}
		}
		
		return retval;
	}
	
	public void logTrials() {
		LogContext.getLogger().fine(String.format("\n----- Block %d : %d trials -----", 
				getNum(), _trials.size()));
		for (TOJTrial t : _trials) {
			LogContext.getLogger().fine(t.getDescription());
		}
		LogContext.getLogger().fine(String.format("-----------------------------------\n", _trials.size()));
	}


	@Override
	public List<TOJTrial> getTrials() {
		return _trials;
	}
}
