package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.util.Properties;

import edu.mcmaster.maplelab.common.datamodel.Session;

public class TOJSession extends Session<TOJBlock, TOJTrial, L> {

	protected TOJSession(Properties props) {
		super(props);
		// TODO Auto-generated constructor stub
	}

	@Override
	public File getDebugLogFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExperimentBaseName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toPropertiesString() {
		// TODO Auto-generated method stub
		return null;
	}

}
