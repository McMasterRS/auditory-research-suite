package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.nio.file;

import edu.mcmaster.maplelab.common.datamodel.AVBlock;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.SoundClip;
import edu.mcmaster.maplelab.toj.animator.AnimationParser;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;

public class TOJBlock extends AVBlock<TOJSession, TOJTrial> {
	private static final float DISK_RADIUS = 0.3f;

	ArrayList<TOJTrial> _trials = null;
	
	protected TOJBlock(TOJSession session, int blockNum, AVBlockType type, List<NotesEnum> pitches,
			List<String> tones, List<String> strikes, List<Long> offsets, List<Integer> numPoints) {
		super(session, blockNum, type);
		
		_trials = new ArrayList<TOJTrial>();

		// create trials from session
	
		if (type == AVBlockType.AUDIO_VIDEO) {
			for (NotesEnum p : pitches) {
				for (String td : tones) {
					// pitch and tone duration will give filename
					// parse file and get animation sequence

					String filename = p.toString().toLowerCase() + "_" +  td.toString().toLowerCase() + ".wav"; // path or file name?
					
					File dir = session.getExpectedAudioSubDir();

					Playable audio = SoundClip.findPlayable(filename, dir);
					
					for (String sd : strikes) {
						filename = p.toString().toLowerCase() + sd.toString().toLowerCase() + "_.txt";
						dir = session.getExpectedVisualSubDir();
						
						try {
							AnimationSequence aniSeq = AnimationParser.parseFile(new File(dir, filename));
							for (Long so : offsets) {
								// look into sound objects
								for (int pts : numPoints) {
									TOJTrial trial = new TOJTrial(aniSeq, false, audio, so, pts, DISK_RADIUS);
									_trials.add(trial);
								}
							}
						}
						catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}								
					}
				}
			}
		}
		
		else if (type == AVBlockType.AUDIO_ONLY) {
			for (NotesEnum p : pitches) {
				for (String td : tones) {
					
					String filename = p.toString() + "_" +  td.toString() + ".wav"; // path or file name?
					File dir = session.getExpectedAudioSubDir();
					
					Playable audio = SoundClip.findPlayable(filename, dir);
					
					for (String sd : strikes) {
						filename = p.toString() + sd.toString() + "_.txt";
						dir = session.getExpectedVisualSubDir();
						
						try {
							AnimationSequence aniSeq = AnimationParser.parseFile(new File(dir, filename));

							TOJTrial trial = new TOJTrial(aniSeq, false, audio, (long) 0, 0, DISK_RADIUS);
							_trials.add(trial);

						}
						catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
		// TODO: support true video?
		else if (type == AVBlockType.VIDEO_ONLY) {
			for (NotesEnum p : pitches) {

				for (String sd : strikes) {
					String filename = p.toString() + sd.toString() + "_.txt";
					File dir = session.getExpectedVisualSubDir();

					try {
						AnimationSequence aniSeq = AnimationParser.parseFile(new File(dir, filename));
						for (int pts : numPoints) {
							TOJTrial trial = new TOJTrial(aniSeq, false, null, (long) 0, pts, DISK_RADIUS); // can audio be null?
							_trials.add(trial);
						}
					}
					catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}								
				}
			}
		}
		
		if (getSession().randomizeTrials()) Collections.shuffle(_trials);
		
		for (int i = 0; i < _trials.size(); i++) {
			_trials.get(i).setNum(i+1);
		}
	}


	@Override
	public List<TOJTrial> getTrials() {
		return _trials;
	}
}
