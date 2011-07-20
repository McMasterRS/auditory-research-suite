package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;
import java.util.Properties;

import edu.mcmaster.maplelab.common.datamodel.Session;

public class TOJSession extends Session<TOJBlock, TOJTrial, L> {
	
	
	public enum ConfigKeys {
		screenWidth,
		screenHeight,
		dataFileName,
		includeVisualStimuli
	}
	
	// setters
	public void setScreenWidth(int width) {
		setProperty(ConfigKeys.screenWidth, width);
	}
	
	public void setScreenHeight(int height) {
		setProperty(ConfigKeys.screenHeight, height);
	}

	public void setDataFileName(String name) {
		setProperty(ConfigKeys.dataFileName, name);
	}
	
	public void includeVisualStimuli(boolean include) {
		setProperty(ConfigKeys.includeVisualStimuli, include);
	}
	
	
	// getters
	public int getScreenWidth() {
		return getInteger(ConfigKeys.screenWidth, 720);
	}
	
	public int getScreenHeight() {
		return getInteger(ConfigKeys.screenHeight, 480);
	}
	
	public String getDataFileName() {
		return getString(ConfigKeys.dataFileName);
	}
	
	public boolean includeVisualStimuli() {
		return getBoolean(ConfigKeys.includeVisualStimuli, true);
	}
	
	
	
	
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
