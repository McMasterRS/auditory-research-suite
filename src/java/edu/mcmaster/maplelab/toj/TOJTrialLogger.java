package edu.mcmaster.maplelab.toj;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import javax.sound.midi.MidiEvent;

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.toj.datamodel.TOJBlock;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

public class TOJTrialLogger extends FileTrialLogger<TOJSession, TOJBlock, TOJTrial> {

	public TOJTrialLogger(TOJSession session, File workingDirectory,
			boolean separateOutput, boolean deleteTempFile) throws IOException {
		super(session, workingDirectory, separateOutput, deleteTempFile);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected File createFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected File getCollectedOutputFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EnumMap<? extends Enum<?>, String> marshalToMap(TOJBlock block,
			TOJTrial trial, int responseNum, MidiEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}
