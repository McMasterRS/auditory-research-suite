package edu.mcmaster.maplelab.common.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.sound.sampled.*;

import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Buffer;
import com.jogamp.openal.sound3d.Source;
import com.jogamp.openal.util.ALut;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.ResourceLoader;
import edu.mcmaster.maplelab.common.datamodel.Session;

/**
 * Wrapper around a {@link Clip}
 *
 */
public class SoundClip implements Playable {
	static {
		AudioSystem3D.init();
        ALut.alutInit();
	}
	
    private static Map<String, Playable> _soundCache = new HashMap<String, Playable>();
    private final Clip _clip;
    private final Source _source;
    private float _sourceVol = 1.0f;
    private final String _name;
    private int _desiredDur = -1;
    private ArrayList<PlayableListener> _listeners = new ArrayList<PlayableListener>();
    
    public SoundClip(String name, Source source) {
    	_name = name;
    	_source = source;
    	_clip = null;
    }

    public SoundClip(String name, Clip clip) {
        _name = name;
        _clip = clip;
        _source = null;
    }

    public void setDesiredDuration(int desiredDur) {
        _desiredDur = desiredDur;
    }

    public int getClipDuration() {
    	if (_source != null) {
        	/* Frequency is in terms of samples/second.
        	 * Size is in terms of bytes.
        	 * Bit-depth is sample size in bits (bits/sample).
        	 * We want bytes * bits/byte * sample/bits * seconds/sample * milliseconds/second
        	 */
    		Buffer b = _source.getBuffer();
    		double bytes = b.getSize();
    		double bitsPerByte = 8;
    		double samplePerBits = 1d / (double) b.getBitDepth();
    		double secondsPerSample = 1d / (double) b.getFrequency();
    		double millisPerSec = 1000;
    		return (int) (bytes * samplePerBits * bitsPerByte * millisPerSec * secondsPerSample);
    	}
    	
        long dur = _clip.getMicrosecondLength();
        return dur != AudioSystem.NOT_SPECIFIED ? (int) (dur / 1000) : 0;
    }

    public int duration() {
        if (_desiredDur > 0) return _desiredDur;
        return getClipDuration();
    }

    public void play() {
    	if (_source != null) {
    		_source.rewind();
    		_source.play();
    	}
    	else {
            _clip.setFramePosition(0);
            _clip.start();
    	}
    	
        
        // The call _clip.drain() below takes too long, messing up synchronization.
        // Just using Thread.sleep() gives us the accuracy we want. Required
        // to support blocking semantics of method.
        Session.sleep(duration());
        
        // if desired duration was less than actual, we have to stop early
        if (_source != null) _source.stop();
        else _clip.stop();
        
        synchronized (_listeners) {
            if (!_listeners.isEmpty()) {
            	for (PlayableListener pl : _listeners) {
            		pl.playableEnded(null);
            	}
            }
        }
    }

    public String name() {
        return _name;
    }
    
    @Override
    public void setVolume(float volume) {
    	if (_source != null) {
    		_source.setGain(volume);
    		return;
    	}
    	
    	FloatControl c = null;
    	try {
    		c = (FloatControl) _clip.getControl(FloatControl.Type.VOLUME);
    	}
    	catch (IllegalArgumentException e) { 
    		try {
        		c = (FloatControl) _clip.getControl(FloatControl.Type.MASTER_GAIN);
        	}
        	catch (IllegalArgumentException ex) { } // no-op
    	} 
    	
    	float val = c.getMinimum() + volume*(c.getMaximum()-c.getMinimum()); 
    	val = val - (val % c.getPrecision());
    	if (c != null) c.setValue(val);
    }
    
    @Override
    public void setMute(boolean mute) {
    	if (_source != null) {
    		if (mute) {
        		_sourceVol = _source.getGain();
        		_source.setGain(0);
    		}
    		else {
    			_source.setGain(_sourceVol);
    		}
    		return;
    	}
    	
    	BooleanControl b = null;
    	try {
    		b = (BooleanControl) _clip.getControl(BooleanControl.Type.MUTE);
    	}
    	catch (IllegalArgumentException e) { } // no-op
        
    	if (b != null) b.setValue(mute);
    }

    /**
     * Get the auditory stimulus with the given name.
     *
     * @param name key name used in config file.
     * @return translated Playable type.
     */
    public static Playable findPlayable(String filename, File directory) {
    	return findPlayable(filename, directory, 1.0f);
    }
    
    public static Playable findPlayable(String filename, File directory, float volume) {
    	return findPlayable(filename, directory, -1, volume);
    }
    
    public static Playable findPlayable(String filename, File directory, int desiredDur, float volume) {
        Playable p = _soundCache.get(filename);
        if (p == null) {
            if (filename != null) {
                Clip clip = null;
                Source source = null;
                try {
                    InputStream input = ResourceLoader.findAudioData(directory, filename);
                    if(input == null) {
                        throw new FileNotFoundException("Couldn't find " + filename);
                    }
                    
                    // load the source
                    source = AudioSystem3D.loadSource(input);
                    
                    if (source == null) {
                    	// if source load failed, try clip
                    	input = ResourceLoader.findAudioData(directory, filename);
                        AudioInputStream stream = AudioSystem.getAudioInputStream(input);
                        AudioFormat format = stream.getFormat();
                        LogContext.getLogger().fine(String.format("%s -> %s", format, filename));
                        clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, format));
                        clip.open(stream);
                    }
                }
                catch (Exception ex) {
                    LogContext.getLogger().log(Level.SEVERE, "Couldn't load audio resource " + filename, ex);
                    return null;
                }
                
                if (source != null) {
                	p = new SoundClip(filename, source);
                    if (desiredDur > 0) {
                        ((SoundClip) p).setDesiredDuration(desiredDur);
                    }
                    
                    // don't change the volume unless necessary
                    if (Float.compare(1.0f, volume) != 0) p.setVolume(volume);
                    
                    _soundCache.put(filename, p);
                }
                else if (clip != null)  {
                    p = new SoundClip(filename, clip);
                    if (desiredDur > 0) {
                        ((SoundClip) p).setDesiredDuration(desiredDur);
                    }
                    
                    // don't change the volume unless necessary
                    if (Float.compare(1.0f, volume) != 0) p.setVolume(volume);
                    
                    _soundCache.put(filename, p);
                    
                    //(new Thread(new SoundPreparer((SoundClip) p))).start();
                }
            }
        }
        return p;
    }

    @Override
    public String toString() {
        return name();
    }

	@Override
	public void addListener(PlayableListener listener) {
		synchronized (_listeners) {
			if (_source != null) _listeners.add(listener);
			else _clip.addLineListener(listener);
		}
	}

	@Override
	public void removeListener(PlayableListener listener) {
		synchronized (_listeners) {
			if (_source != null) _listeners.remove(listener);
			else _clip.removeLineListener(listener);
		}
	}
	
	/**
	 * Class that plays a sound clip once silently at load time. Intended to avoid 
	 * initialization "pop" on first play.  This is a bit of a hack and somewhat 
	 * imperfect - it would be better to find another way to deal w/ this issue.  
	 * Using this method, there is still a large "pop" before the first trial, 
	 * but no more for individual sound files played during a trial.
	 */
	private static class SoundPreparer implements Runnable {
		private final SoundClip _soundClip;

		public SoundPreparer(SoundClip clip) {
			_soundClip = clip;
		}
		
		@Override
		public void run() {
			_soundClip.setMute(true);
			_soundClip.play();
			_soundClip.setMute(false);
		}
		
	}
}
