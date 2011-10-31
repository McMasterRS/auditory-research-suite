package edu.mcmaster.maplelab.av.media;

import static edu.mcmaster.maplelab.av.media.MediaParams.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.LogContext;


public class PlayableMedia {
	
	public enum MediaType {
		AUDIO(new MediaParams[]{frequency, spectrum, envelopeDuration}, "%s-%s-%s") {
			@Override
			protected List<String> getFileExtensions(AVSession<?, ?, ?> session) {
				return session.getAudioFileExtensions();
			}
			@Override
			protected File getDirectory(AVSession<?, ?, ?> session) {
				return session.getExpectedAudioSubDir();
			}
			@Override
			public List<PlayableMedia> getUniqueMedia(AVSession<?, ?, ?> session) {
				List<PlayableMedia> retval = new ArrayList<PlayableMedia>();
				for (String f : frequency.getSessionParameters(session)) {
					for (String s : spectrum.getSessionParameters(session)) {
						for (EnvelopeDuration ed : envelopeDuration.getSessionParameters(session)) {
							retval.add(createMedia(session, f, s, ed));
						}
					}
				}
				return retval;
			}
			@Override
			protected Playable findPlayable(String filename, File directory, Float volume) {
				return SoundClip.findPlayable(filename,directory, volume);
			}
		},
		VIDEO(new MediaParams[]{pitch, visualDuration, audioDuration}, "%s%s%s") {
			@Override
			protected List<String> getFileExtensions(AVSession<?, ?, ?> session) {
				return session.getVideoFileExtensions();
			}
			@Override
			protected File getDirectory(AVSession<?, ?, ?> session) {
				return session.getExpectedVideoSubDir();
			}
			@Override
			public List<PlayableMedia> getUniqueMedia(AVSession<?, ?, ?> session) {
				List<PlayableMedia> retval = new ArrayList<PlayableMedia>();
				for (NotesEnum ne : pitch.getSessionParameters(session)) {
					for (DurationEnum vd : visualDuration.getSessionParameters(session)) {
						for (DurationEnum ad : audioDuration.getSessionParameters(session)) {
							retval.add(createMedia(session, ne, vd, ad));
						}
					}
				}
				return retval;
			}
			@Override
			protected Playable findPlayable(String filename, File directory, Float volume) {
				return QTVideoClip.findPlayable(filename,directory, volume);
			}
		},
		LEGACY_AUDIO(new MediaParams[]{pitch, audioDuration}, "%s_%s") {
			@Override
			protected List<String> getFileExtensions(AVSession<?, ?, ?> session) {
				return session.getAudioFileExtensions();
			}
			@Override
			protected File getDirectory(AVSession<?, ?, ?> session) {
				return session.getExpectedAudioSubDir();
			}
			@Override
			public List<PlayableMedia> getUniqueMedia(AVSession<?, ?, ?> session) {
				List<PlayableMedia> retval = new ArrayList<PlayableMedia>();
				for (NotesEnum ne : pitch.getSessionParameters(session)) {
					for (DurationEnum de : audioDuration.getSessionParameters(session)) {
						retval.add(createMedia(session, ne, de));
					}
				}
				return retval;
			}
			@Override
			protected Playable findPlayable(String filename, File directory, Float volume) {
				return SoundClip.findPlayable(filename,directory, volume);
			}
		};

		private final MediaParams<?>[] _params;
		private final String _fileFormat;
		
		private MediaType(MediaParams<?>[] params, String fileFormat) {
			_params = params;
			_fileFormat = fileFormat;
		}
		
		protected MediaParams<?>[] getParamTypes() {
			return _params;
		}
		
		protected String getFileFormat() {
			return _fileFormat;
		}
		
		protected File getFile(AVSession<?, ?, ?> session, Object...params) {
			File dir = getDirectory(session);
			Object[] stringVals = new String[params.length];
			MediaParams<?>[] mps = getParamTypes();
			for (int i = 0; i < params.length; i++) {
				stringVals[i] = mps[i].getString(params[i]);
			}
			String filename = String.format(getFileFormat(), stringVals);
			
			// find the right extension
			File f = null;
			for (String ext : getFileExtensions(session)) {
				f = new File(dir, filename + "." + ext);
				if (f.exists()) break;
			}
			return f;
		}
		
		protected Playable loadPlayable(AVSession<?, ?, ?> session, Object... params) {
			File dir = getDirectory(session);
			Object[] stringVals = new String[params.length];
			MediaParams<?>[] mps = getParamTypes();
			for (int i = 0; i < params.length; i++) {
				stringVals[i] = mps[i].getString(params[i]);
			}
			String filename = String.format(getFileFormat(), stringVals);
			float volume = session.getPlaybackGain();
			
			Playable retval = null;
			List<String> extensions = getFileExtensions(session);
			for (int i = 0; retval == null && i < extensions.size(); i++) {
				File f = new File(dir, filename + "." + extensions.get(i));
				retval = findPlayable(f.getName(), f.getParentFile(), volume);
			}
			
			if (retval == null) {
				LogContext.getLogger().warning(
						String.format("File %s not found.", filename));
			}
			return retval;
		}
		
		public File getExpectedFile(AVSession<?, ?, ?> session, Object... params) {
			File dir = getDirectory(session);
			Object[] stringVals = new String[params.length];
			MediaParams<?>[] mps = getParamTypes();
			for (int i = 0; i < params.length; i++) {
				stringVals[i] = mps[i].getString(params[i]);
			}
			
			File retval = null;
			String filename = String.format(getFileFormat(), stringVals);
			List<String> extensions = getFileExtensions(session);
			for (int i = 0; retval == null && i < extensions.size(); i++) {
				retval = new File(dir, filename + "." + extensions.get(i));
				if (!retval.exists()) retval = null;
			}
			return retval;
		}
		
		public PlayableMedia createMedia(AVSession<?, ?, ?> session, Object...params) {
			return new PlayableMedia(this, session, params);
		}
		
		public PlayableMedia createDemoMedia(File file, Float volume) {
			return new PlayableMedia(this, file, volume);
		}
		
		protected abstract List<String> getFileExtensions(AVSession<?, ?, ?> session);
		protected abstract File getDirectory(AVSession<?, ?, ?> session);
		protected abstract Playable findPlayable(String filename, File directory, Float volume);
		public abstract List<PlayableMedia> getUniqueMedia(AVSession<?, ?, ?> session);
	}
	
	private final MediaType _type;
	private final Playable _playable;
	private final Map<MediaParams<?>, Object> _paramMap;
	
	private PlayableMedia(MediaType type, AVSession<?, ?, ?> session, Object[] params) {
		_paramMap = new HashMap<MediaParams<?>, Object>();
		MediaParams<?>[] paramTypes = type.getParamTypes();
		if (paramTypes.length != params.length) {
			throw new IllegalArgumentException("Invalid number of parameters");
		}
		
		for (int i = 0; i < params.length; i++) {
			setValue(paramTypes[i], params[i]);
		}
		_type = type;
		_playable = _type.loadPlayable(session, params);
	}
	
	private PlayableMedia(MediaType type, File file, Float volume) {
		_paramMap = new HashMap<MediaParams<?>, Object>();
		_type = type;
		_playable = _type.findPlayable(file.getName(), file.getParentFile(), volume);
	}
	
	public Playable getPlayable() {
		return _playable;
	}
	
	public MediaType getType() {
		return _type;
	}
	
	public String getName() {
		return getPlayable().name();
	}
	
	public <T extends Object> T getValue(MediaParams<T> key) {
		return key.cast(_paramMap.get(key));
	}
	
	private <T extends Object> void setValue(MediaParams<T> key, Object value) {
		_paramMap.put(key, value);
	}
	
	
}
