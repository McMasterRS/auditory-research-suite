package edu.mcmaster.maplelab.si;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Set;

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.si.datamodel.SIBlock;
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

public class SITrialLogger extends FileTrialLogger<SISession, SIBlock, SITrial> {

	public SITrialLogger(SISession session, File workingDirectory) throws IOException {
		super(session, workingDirectory);
	}

	@Override
	protected void loadAdditionalFileTypes() {
		// TODO Auto-generated method stub
		
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
	protected Set<? extends Enum<?>> getGeneralDataHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EnumMap<? extends Enum<?>, String> marshalGeneralDataToMap(
			SIBlock block, SITrial trial) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Set<? extends Enum<?>> getTrialDataHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(
			SIBlock block, SITrial trial) {
		// TODO Auto-generated method stub
		return null;
	}

}
