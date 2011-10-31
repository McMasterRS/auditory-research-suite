package edu.mcmaster.maplelab.av.media;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.ResourceLoader;
import edu.mcmaster.maplelab.common.datamodel.Session;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;

@SuppressWarnings("deprecation")
public class QTVideoClip implements Playable {
	static {
		try {
			QTSession.open();
		} 
		catch (QTException e) {
			LogContext.getLogger().severe("Could not initialize QuickTime environment.");
		}
	}
	
	private static final Map<String, Playable> _movieCache = new HashMap<String, Playable>();
	
	public static Playable findPlayable(String filename, File directory, float volume) {
        Playable p = _movieCache.get(filename);
        if (p == null) {
            if (filename != null) {
            	Movie m = null;
            	try {
                	File f = ResourceLoader.findResource(directory, filename);
                    OpenMovieFile omf = OpenMovieFile.asRead(new QTFile (f));
                    m = Movie.fromFile(omf);
            	}
            	catch (Exception e) {
            		LogContext.getLogger().log(Level.SEVERE, "Couldn't load video resource " + filename, e);
                    return null;
            	}
                
                if (m != null) {
                	p = new QTVideoClip(filename, m);
                	// don't change the volume unless necessary
                    if (Float.compare(1.0f, volume) != 0) p.setVolume(volume);
                	_movieCache.put(filename, p);
                }
            }
        }
        
        return p;
    }
	
	public static Component makeComponent(Playable clip) {
		Component retval = null;
		if (clip instanceof QTVideoClip) {
			QTVideoClip qtc = (QTVideoClip) clip;
			try {
				retval = QTFactory.makeQTComponent(qtc._movie).asComponent();
			} 
			catch (QTException e) {
				LogContext.getLogger().severe("Could not initialize clip component.");
			}
		}
		else {
			LogContext.getLogger().severe(String.format("Playable %sis not a " +
					"valid video clip", clip != null ? clip.name() + " " : ""));
		}
		
		return retval;
	}
	
	private final Movie _movie;
	private final String _name;
	private float _volume;
    private ArrayList<PlayableListener> _listeners = new ArrayList<PlayableListener>();
	
	private QTVideoClip(String name, Movie m) {
		if (m == null) {
			throw new IllegalArgumentException("Movie object may not be null.");
		}

		_name = name;
		_movie = m;
	}

	@Override
	public void play() {
		try {
			_movie.setTimeValue(0);
			_movie.start();
			
			Session.sleep(duration());
	        
	        synchronized (_listeners) {
	            if (!_listeners.isEmpty()) {
	            	for (PlayableListener pl : _listeners) {
	            		pl.playableEnded(null);
	            	}
	            }
	        }
		} 
		catch (StdQTException e) {
			LogContext.getLogger().severe("Video playback failed: " + name());
		}
		
	}

	@Override
	public int duration() {
		try {
			// XXX: DO NOT use getDuration!
			return _movie.getTimeBase().getStopTime() / 1000;
		} 
		catch (StdQTException e) {
			return 0;
		}
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public void setVolume(float volume) {
		try {
			_movie.setVolume(volume);
		} 
		catch (StdQTException e) { }
	}

	@Override
	public void setMute(boolean mute) {
		
		try {
			if (mute) {
				_volume = _movie.getVolume();
				_movie.setVolume(0);
			}
			else {
				_movie.setVolume(_volume);
			}
		} 
		catch (StdQTException e) { }
	}
	
	public int getWidth() {
		int retval = 0;
		try {
			retval =  _movie.getBounds().getWidth();
		} 
		catch (StdQTException e) { }
		
		return retval;
	}
	
	public int getHeight() {
		int retval = 0;
		try {
			retval =  _movie.getBounds().getHeight();
		} 
		catch (StdQTException e) { }
		
		return retval;
	}

	@Override
	public void addListener(PlayableListener listener) {
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	@Override
	public void removeListener(PlayableListener listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}
}
