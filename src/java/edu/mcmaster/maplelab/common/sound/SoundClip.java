package edu.mcmaster.maplelab.common.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.sound.sampled.*;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.ResourceLoader;
import edu.mcmaster.maplelab.common.datamodel.Session;

/**
 * Wrapper around a {@link Clip}
 *
 */
public class SoundClip implements Playable {
    private static Map<String, Playable> _soundCache = new HashMap<String, Playable>();
    private final Clip _clip;
    private final String _name;
    private int _desiredDur = -1;

    public SoundClip(String name, Clip clip) {
        _name = name;
        _clip = clip;
    }

    public void setDesiredDuration(int desiredDur) {
        _desiredDur = desiredDur;
    }

    public int getClipDuration() {
        long dur = _clip.getMicrosecondLength();
        return dur != AudioSystem.NOT_SPECIFIED ? (int) (dur / 1000) : 0;
    }

    public int duration() {
        if (_desiredDur > 0) return _desiredDur;
        return getClipDuration();
    }

    public void play() {
        _clip.setFramePosition(0);
        _clip.start();
        
        // The call _clip.drain() below takes too long, messing up synchronization.
        // Just using Thread.sleep() gives us the accuracy we want. Required
        // to support blocking semantics of method.
        Session.sleep(duration());
    }

    public String name() {
        return _name;
    }
    
    /**
     * Get the underlying clip object.
     */
    private Clip getClip() {
    	return _clip;
    }
    
    @Override
    public void setVolume(float volume) {
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
                try {
                    InputStream input = ResourceLoader.findAudioData(directory, filename);
                    if(input == null) {
                        throw new FileNotFoundException("Couldn't find " + filename);
                    }
                    AudioInputStream stream = AudioSystem.getAudioInputStream(input);
                    AudioFormat format = stream.getFormat();
                    LogContext.getLogger().fine(String.format("%s -> %s", format, filename));
                    clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, format));
                    clip.open(stream);
                }
                catch (Exception ex) {
                    LogContext.getLogger().log(Level.SEVERE, "Couldn't load audio resource " + filename, ex);
                    return null;
                }

                if(clip != null)  {
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
		_clip.addLineListener(listener);
	}

	@Override
	public void removeListener(PlayableListener listener) {
		_clip.removeLineListener(listener);
	}
	
	/**
	 * Class that plays a sound clip once silently at load time . Intended to avoid 
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
