package edu.mcmaster.maplelab.common.sound;

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
     * Get the auditory stimulus with the given name.
     *
     * @param name key name used in config file.
     * @return translated Playable type.
     */
    public static Playable findPlayable(String name, Session<?, ?> session) {
        return findPlayable(name, session, -1);
    }
    public static Playable findPlayable(String name, Session<?, ?> session, int desiredDur) {
        Playable p = _soundCache.get(name);
        if(p == null) {
            String fileName = session.getString(name);
            if (fileName == null) fileName = name;
            if (fileName != null) {
                Clip clip = null;
                try {
                    InputStream input = ResourceLoader.findAudioData(session.getDataDir(), fileName);
                    if(input == null) {
                        throw new FileNotFoundException("Couldn't find " + fileName);
                    }
                    AudioInputStream stream = AudioSystem.getAudioInputStream(input);
                    AudioFormat format = stream.getFormat();
                    LogContext.getLogger().fine(String.format("%s -> %s", format, fileName));
                    clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, format));
                    clip.open(stream);
                }
                catch (Exception ex) {
                    LogContext.getLogger().log(Level.SEVERE, "Couldn't load audio resource " + fileName, ex);
                    return null;
                }

                if(clip != null)  {
                    p = new SoundClip(name, clip);
                    if (desiredDur > 0) {
                        ((SoundClip) p).setDesiredDuration(desiredDur);
                    }
                    _soundCache.put(name, p);
                }
            }
        }
        return p;
    }

    @Override
    public String toString() {
        return name();
    }
}
