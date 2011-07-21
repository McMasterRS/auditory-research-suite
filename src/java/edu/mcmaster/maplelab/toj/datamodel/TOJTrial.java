package edu.mcmaster.maplelab.toj.datamodel;

import java.io.File;

import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.Trial;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

public class TOJTrial extends Trial<Response> {
	private final NotesEnum _pitch;
	private final boolean _isVideo;
	private final File _videoFile;
	private final File _audioFile;
	private final File _animationFile;
	private final int _offset;
	private final int _numPoints;
	
	public TOJTrial(File dataDir, NotesEnum pitch, boolean isVideo, DurationEnum toneDuration, 
			DurationEnum strikeDuration, int timingOffset, int animationPoints) {
		_pitch = pitch;
		_isVideo = isVideo;
		_offset = timingOffset;
		_numPoints = animationPoints;
		
		//TODO: determine files
		_videoFile = null;
		_audioFile = null;
		_animationFile = null;
	}
}
