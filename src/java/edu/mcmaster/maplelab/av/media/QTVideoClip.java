package edu.mcmaster.maplelab.av.media;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSArray;
import org.rococoa.cocoa.qtkit.QTKit;
import org.rococoa.cocoa.qtkit.QTMedia;
import org.rococoa.cocoa.qtkit.QTMovie;
import org.rococoa.cocoa.qtkit.QTTime;
import org.rococoa.cocoa.qtkit.QTTrack;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.ResourceLoader;
import edu.mcmaster.maplelab.common.datamodel.Session;

/**
 * Class for encapsulating a Quicktime video clip as a Playable.
 * 
 * @author bguseman
 */
public class QTVideoClip implements Playable {
	static {
        // load library
        @SuppressWarnings("unused")
        QTKit instance = QTKit.instance;
	}
	
	private static final Map<String, Playable> _movieCache = new HashMap<String, Playable>();
	
	public static Playable findPlayable(String filename, File directory, float volume, 
			boolean forceReload) {
        Playable p = forceReload ? null : _movieCache.get(filename);
        if (p == null) {
            if (filename != null) {
            	QTMovie m = null;
            	try {
                	File f = ResourceLoader.findResource(directory, filename);
                    m = QTMovie.movieWithFile_error(f.getAbsolutePath(), null);
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
	
	private final QTMovie _movie;
	private final String _name;
	private List<Float> _volumes = new ArrayList<Float>(1); // expect only 1
    private ArrayList<PlayableListener> _listeners = new ArrayList<PlayableListener>();
	
	private QTVideoClip(String name, QTMovie m) {
		if (m == null) {
			throw new IllegalArgumentException("Movie object may not be null.");
		}

		_name = name;
		_movie = m;
		
		// initialize volumes
		NSArray soundTracks = _movie.tracksOfMediaType(QTMedia.QTMediaTypeSound);
		for (int i = 0; i < soundTracks.count(); i++) {
			QTTrack track = Rococoa.cast(soundTracks.objectAtIndex(i), QTTrack.class);
			_volumes.add(track.volume());
		}
	}

	@Override
	public void play() {
    	play(null);
    }

    public void play(CountDownLatch latch) {
		_movie.gotoBeginning();
    	if (latch != null) {
			try {
				latch.await();
			} 
    		catch (InterruptedException e) {}
    	}
		_movie.play();
		
		Session.sleep(durationMillis());
        
        synchronized (_listeners) {
            if (!_listeners.isEmpty()) {
            	for (PlayableListener pl : _listeners) {
            		pl.playableEnded();
            	}
            }
        }
	}

	@Override
	public int durationMillis() {
		QTTime time = _movie.duration();
		// get milliseconds
		return (int) (1000d * (double) time.timeValue / time.timeScale.doubleValue());
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public void setVolume(float volume) {
		NSArray soundTracks = _movie.tracksOfMediaType(QTMedia.QTMediaTypeSound);
		for (int i = 0; i < soundTracks.count(); i++) {
			QTTrack track = Rococoa.cast(soundTracks.objectAtIndex(i), QTTrack.class);
			track.setVolume(volume);
			_volumes.set(i, volume);
		}
	}

	@Override
	public void setMute(boolean mute) {
		NSArray soundTracks = _movie.tracksOfMediaType(QTMedia.QTMediaTypeSound);
		
		if (mute) {
			for (int i = 0; i < soundTracks.count(); i++) {
				QTTrack track = Rococoa.cast(soundTracks.objectAtIndex(i), QTTrack.class);
				track.setVolume(0);
			}
		}
		else {
			for (int i = 0; i < soundTracks.count(); i++) {
				QTTrack track = Rococoa.cast(soundTracks.objectAtIndex(i), QTTrack.class);
				track.setVolume(_volumes.get(i));
			}
		}
	}
	
	/**
	 * Get the QTMovie object.
	 */
	protected QTMovie getQTMovie() {
		return _movie;
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
