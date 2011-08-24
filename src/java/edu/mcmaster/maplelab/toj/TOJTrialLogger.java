/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import javax.sound.midi.MidiEvent;

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.toj.datamodel.TOJBlock;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;
/**
 * TOJ specific extension of FileTrialLogger.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class TOJTrialLogger extends FileTrialLogger<TOJSession, TOJBlock, TOJTrial> {

	public TOJTrialLogger(TOJSession session, File workingDirectory,
			boolean separateOutput, boolean deleteTempFile) throws IOException {
		super(session, workingDirectory, separateOutput, deleteTempFile);
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
