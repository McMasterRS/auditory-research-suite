/**
 * 
 */
package edu.mcmaster.maplelab.av.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.media.MediaParams.MediaParamValue;
import edu.mcmaster.maplelab.av.media.animation.AnimationParser;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.LogContext;

/**
 * @author bguseman
 *
 */
public abstract class MediaType<T extends MediaSource> {
	
	private enum ParameterKeys {
		videoParams,
		videoFileFormat,
		animationParams,
		animationFileFormat,
		audioParams,
		audioFileFormat
	}
	
	public static MediaType<?>[] values() {
		return new MediaType<?>[]{AUDIO, VIDEO, ANIMATION};
	}
	
	public static final MediaType<Playable> AUDIO = new MediaType<Playable>("audio", 
			ParameterKeys.audioParams, ParameterKeys.audioFileFormat) {
		@Override
		protected List<String> getFileExtensions(AVSession<?, ?, ?> session) {
			return session.getAudioFileExtensions();
		}
		@Override
		protected File getDirectory(AVSession<?, ?, ?> session) {
			return session.getAudioDirectory();
		}
		@Override
		protected Playable findMediaObject(String filename, File directory, AVSession<?, ?, ?> session) {
			float volume = session.getPlaybackGain();
			return SoundClip.findPlayable(filename, directory, volume);
		}
	};
	public static final MediaType<Playable> VIDEO = new MediaType<Playable>("video", 
				ParameterKeys.videoParams, ParameterKeys.videoFileFormat) {
		@Override
		protected List<String> getFileExtensions(AVSession<?, ?, ?> session) {
			return session.getVideoFileExtensions();
		}
		@Override
		protected File getDirectory(AVSession<?, ?, ?> session) {
			return session.getVideoDirectory();
		}
		@Override
		protected Playable findMediaObject(String filename, File directory, AVSession<?, ?, ?> session) {
			float volume = session.getPlaybackGain();
			return QTVideoClip.findPlayable(filename, directory, volume);
		}
	};
	public static final MediaType<AnimationSequence> ANIMATION = 
				new MediaType<AnimationSequence>("animation", ParameterKeys.animationParams, 
						ParameterKeys.animationFileFormat) {
		@Override
		protected List<String> getFileExtensions(AVSession<?, ?, ?> session) {
			return session.getAnimationFileExtensions();
		}
		@Override
		protected File getDirectory(AVSession<?, ?, ?> session) {
			return session.getAnimationDirectory();
		}
		@Override
		protected AnimationSequence findMediaObject(String filename, File directory, AVSession<?, ?, ?> session) {
			float aspect = session.getAnimationPointAspect();
			File f = new File(directory, filename);
			try {
				return AnimationParser.parseFile(f, aspect);
			} 
			catch (FileNotFoundException e) {
				LogContext.getLogger().warning(String.format(
						"Could not find %s media file %s", this.name(), f.getAbsolutePath()));
				return null;
			}
		}
	};
	
	private final String _name;
	private final ParameterKeys _paramsKey;
	private final ParameterKeys _fileFormatKey;
	
	private MediaType(String name, ParameterKeys paramsKey, ParameterKeys fileFormatKey) {
		_name = name;
		_paramsKey = paramsKey;
		_fileFormatKey = fileFormatKey;
	}
	
	public String name() {
		return _name;
	}
	
	/**
	 * Get the file name constructed by using the given parameter values and the
	 * file specification extracted from the session properties.  The number of
	 * parameter values must match the number expected by the specification.
	 */
	protected String getFileName(AVSession<?, ?, ?> session, Collection<MediaParamValue> actualParams) {
		List<String> params = getParams(session);
		String format = session.getString(_fileFormatKey, null);
		if (params == null || format == null) {
			LogContext.getLogger().warning(String.format(
					"File specification properties not found for %s media.", this.name()));
			return null;
		}
		if (params.size() != actualParams.size()) {
			LogContext.getLogger().warning(String.format(
					"Incorrect number of parameters for %s file specification.", this.name()));
			return null;
		}
		
		String name = format;
		for (MediaParamValue val : actualParams) {
			String replace = "${" + val.paramName() + "}";
			name = name.replace(replace, val.paramValue());
		}
		
		return name;
	}
	
	/**
	 * Get the file name constructed by using the given parameter values and the
	 * file specification extracted from the session properties.  The number of
	 * parameter values must match the number expected by the specification.
	 */
	protected String getFileName(AVSession<?, ?, ?> session, MediaParamValue...actualParams) {
		return getFileName(session, Arrays.asList(actualParams));
	}
	
	/**
	 * Load the Playable object from the given file, using the extension and 
	 * directory specified in the session properties.
	 */
	protected T loadMediaObject(AVSession<?, ?, ?> session, String fileName) {
		File dir = getDirectory(session);
		
		T retval = null;
		List<String> extensions = getFileExtensions(session);
		for (int i = 0; retval == null && i < extensions.size(); i++) {
			File f = new File(dir, fileName + "." + extensions.get(i));
			retval = findMediaObject(f.getName(), f.getParentFile(), session);
		}
		
		if (retval == null) {
			LogContext.getLogger().warning(
					String.format("File %s not found.", fileName));
		}
		return retval;
	}
	
	/**
	 * Get the file expected from using the given parameter values and the
	 * appropriate file extension to construct a file.
	 */
	public File getExpectedFile(AVSession<?, ?, ?> session, Collection<MediaParamValue> params) {
	    File retval = null;
	    File dir = getDirectory(session);
	    
	    if (dir == null || !dir.exists()) {
	        LogContext.getLogger().severe("Data directory missing: " + dir);
	        return null;
	    }
	    
		String filename = getFileName(session, params);
		List<String> extensions = getFileExtensions(session);
		for (int i = 0; retval == null && i < extensions.size(); i++) {
			retval = new File(dir, filename + "." + extensions.get(i));
			if (!retval.exists()) retval = null;
		}
		
		return retval;
	}
	

	/**
	 * Provide the file name basename with full path, based on the parameter values. For reporting errors. No
	 * extension is included.
	 */
    public String getExpectedFilename(AVSession<?, ?, ?> session, Collection<MediaParamValue> params) {
        String filename = getFileName(session, params);
//        int idx = filename.lastIndexOf('.');
//        if(idx >= 0) {
//            filename = filename.substring(0, idx);
//        }
        return new File(getDirectory(session), filename).toString();
    }	
	
	/**
	 * Get the file expected from using the given parameter values and the
	 * appropriate file extension to construct a file.
	 */
	public File getExpectedFile(AVSession<?, ?, ?> session, MediaParamValue... params) {
		return getExpectedFile(session, Arrays.asList(params));
	}
	
	/**
	 * Build all possible parameter combinations for this media type.
	 */
	public List<Map<String, MediaParamValue>> buildParameterMaps(AVSession<?, ?, ?> session) {
		List<Map<String, MediaParamValue>> retval = null;
		List<Map<String, MediaParamValue>> working = new ArrayList<Map<String, MediaParamValue>>();
		
		for (String param : getParams(session)) {
			retval = new ArrayList<Map<String, MediaParamValue>>();
			MediaParams mp = MediaParams.getAvailableValues(param);
			if (working.size() == 0) {
				for (MediaParamValue val : mp.getValues()) {
					Map<String, MediaParamValue> valList = new HashMap<String, MediaParamValue>();
					valList.put(val.paramName(), val);
					retval.add(valList);
				}
			}
			else {
				for (Map<String, MediaParamValue> valList : working) {
					for (MediaParamValue val : mp.getValues()) {
						Map<String, MediaParamValue> newValList = new HashMap<String, MediaParamValue>();
						newValList.putAll(valList);
						newValList.put(val.paramName(), val);
						retval.add(newValList);
					}
				}
			}
			
			working = retval;
		}
		
		return retval;
	}
	
	/**
	 * Get the parameters associated with this media type.
	 */
	public List<String> getParams(AVSession<?, ?, ?> session) {
		return session.getStringList(_paramsKey, (String[]) null);
	}
	
	public MediaWrapper<T> createMedia(AVSession<?, ?, ?> session, Collection<MediaParamValue> values) {
		T p = loadMediaObject(session, getFileName(session, values));
		return p != null ? new MediaWrapper<T>(this, p) : null;
	}
	
	public MediaWrapper<T> createDemoMedia(File file, AVSession<?, ?, ?> session) {
		return new MediaWrapper<T>(this, file, session);
	}
	
	protected abstract List<String> getFileExtensions(AVSession<?, ?, ?> session);
	protected abstract File getDirectory(AVSession<?, ?, ?> session);
	protected abstract T findMediaObject(String filename, File directory, AVSession<?, ?, ?> session);
	
	public static class MediaWrapper<T extends MediaSource> {
		private final MediaType<T> _type;
		private final T _mediaObject;
		
		private MediaWrapper(MediaType<T> type, File file, AVSession<?, ?, ?> session) {
			this(type, type.findMediaObject(file.getName(), file.getParentFile(), session));
		}
		
		private MediaWrapper(MediaType<T> type, T mediaObject) {
			_type = type;
			_mediaObject = mediaObject;
		}
		
		public T getMediaObject() {
			return _mediaObject;
		}
		
		public MediaType<T> getType() {
			return _type;
		}
		
		public String getName() {
			return _mediaObject != null ? _mediaObject.name() : "";
		}
	}


}
