package edu.mcmaster.maplelab.av.media;

import java.util.List;

import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

/**
 * Class for enumerating all parameters contributing to media object creation.
 * 
 * @author bguseman
 *
 * @param <T> type of parameter
 */
public abstract class MediaParams<T> {
	public static final MediaParams<String> frequency = new MediaParams<String>("frequency") {
		@Override
		public List<String> getSessionParameters(AVSession<?, ?, ?> session) {
			return session.getFrequencies();
		}
		@Override
		public String getStringValue(String paramValue) {
			return paramValue;
		}
		@Override
		public String cast(Object o) {
			return (o instanceof String) ? (String) o : null;
		}
	};
	public static final MediaParams<String> spectrum = new MediaParams<String>("spectrum") {
		@Override
		public List<String> getSessionParameters(AVSession<?, ?, ?> session) {
			return session.getSpectra();
		}
		@Override
		public String getStringValue(String paramValue) {
			return paramValue;
		}
		@Override
		public String cast(Object o) {
			return (o instanceof String) ? (String) o : null;
		}
	};
	public static final MediaParams<EnvelopeDuration> envelopeDuration = 
							new MediaParams<EnvelopeDuration>("envelopeDuration") {
		@Override
		public List<EnvelopeDuration> getSessionParameters(AVSession<?, ?, ?> session) {
			return session.getEnvelopeDurations();
		}
		@Override
		public String getStringValue(EnvelopeDuration paramValue) {
			return paramValue.toString();
		}
		@Override
		public EnvelopeDuration cast(Object o) {
			return (o instanceof EnvelopeDuration) ? (EnvelopeDuration) o : null;
		}
	};
	public static final MediaParams<NotesEnum> pitch = new MediaParams<NotesEnum>("pitch") {
		@Override
		public List<NotesEnum> getSessionParameters(AVSession<?, ?, ?> session) {
			return session.getPitches();
		}
		@Override
		public String getStringValue(NotesEnum paramValue) {
			return paramValue.name().toLowerCase();
		}
		@Override
		public NotesEnum cast(Object o) {
			return (o instanceof NotesEnum) ? (NotesEnum) o : null;
		}
	};
	public static final MediaParams<DurationEnum> audioDuration = 
								new MediaParams<DurationEnum>("audioDuration") {
		@Override
		public List<DurationEnum> getSessionParameters(AVSession<?, ?, ?> session) {
			return session.getAudioDurations();
		}
		@Override
		public String getStringValue(DurationEnum paramValue) {
			return paramValue.codeString();
		}
		@Override
		public DurationEnum cast(Object o) {
			return (o instanceof DurationEnum) ? (DurationEnum) o : null;
		}
	};
	public static final MediaParams<DurationEnum> visualDuration = 
								new MediaParams<DurationEnum>("visualDuration") {
		@Override
		public List<DurationEnum> getSessionParameters(AVSession<?, ?, ?> session) {
			return session.getVisualDurations();
		}
		@Override
		public String getStringValue(DurationEnum paramValue) {
			return paramValue.codeString();
		}
		@Override
		public DurationEnum cast(Object o) {
			return (o instanceof DurationEnum) ? (DurationEnum) o : null;
		}
	};
	
	private final String _name;
	
	private MediaParams(String name) {
		_name = name;
	};
	public abstract List<T> getSessionParameters(AVSession<?, ?, ?> session);
	protected abstract String getStringValue(T paramValue);
	public abstract T cast(Object o);
	public String getString(Object o) {
		T val = cast(o);
		return val != null ? getStringValue(val) : null;
	}
	@Override
	public String toString() {
		return _name;
	}
}
