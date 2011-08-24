package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
//import java.nio.file;

import edu.mcmaster.maplelab.common.datamodel.AVBlock;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.SoundClip;
import edu.mcmaster.maplelab.toj.animator.AnimationParser;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;

public class TOJBlock extends AVBlock<TOJSession, TOJTrial> {

	ArrayList<TOJTrial> _trials = null;
	
	protected TOJBlock(TOJSession session, int blockNum, AVBlockType type) {
		super(session, blockNum, type);
		
		_trials = new ArrayList<TOJTrial>();

		// create trials from session
	
		if (type == AVBlockType.AUDIO_VIDEO) {
			for (NotesEnum p : session.getPitches()) {
				for (String td : session.getToneDurations()) {
					// pitch and tone duration will give filename
					// parse file and get animation sequence

					String filename = p.toString().toLowerCase() + "_" +  td.toString().toLowerCase() + ".wav"; // path or file name?
					
					File dir = session.getExpectedAudioSubDir();

					Playable audio = SoundClip.findPlayable(filename, dir);
					
					for (String sd : session.getStrikeDurations()) {
						filename = p.toString().toLowerCase() + sd.toString().toLowerCase() + "_.txt";
						dir = session.getExpectedVisualSubDir();
						
						try {
							AnimationSequence aniSeq = AnimationParser.parseFile(new File(dir, filename));
							for (Float so : session.getSoundOffsets()) {
								// look into sound objects
								for (int pts : session.getNumAnimationPoints()) {
									TOJTrial trial = new TOJTrial(aniSeq, false, audio, so, pts, 0.3f);
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
			for (NotesEnum p : session.getPitches()) {
				for (String td : session.getToneDurations()) {
					
					String filename = p.toString() + "_" +  td.toString() + ".wav"; // path or file name?
					File dir = new File(session.getDataDir(), "aud");
					
					Playable audio = SoundClip.findPlayable(filename, dir);
					
					for (String sd : session.getStrikeDurations()) {
						filename = p.toString() + sd.toString() + "_.txt";
						dir = new File(session.getDataDir(), "vis");
						
						try {
							AnimationSequence aniSeq = AnimationParser.parseFile(new File(dir, filename));

							TOJTrial trial = new TOJTrial(aniSeq, false, audio, 0f, 0, 0.3f);
							_trials.add(trial);

						}
						catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
		
		else if (type == AVBlockType.VIDEO_ONLY) {
			for (NotesEnum p : session.getPitches()) {

				for (String sd : session.getStrikeDurations()) {
					String filename = p.toString() + sd.toString() + "_.txt";
					File dir = new File(session.getDataDir(), "vis");

					try {
						AnimationSequence aniSeq = AnimationParser.parseFile(new File(dir, filename));
						for (int pts : session.getNumAnimationPoints()) {
							TOJTrial trial = new TOJTrial(aniSeq, false, null, 0f, pts, 0.3f); // can audio be null?
							_trials.add(trial);
						}
					}
					catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}								
				}
			}
		}
	}


	@Override
	public List<TOJTrial> getTrials() {
		return _trials;
	}
}
