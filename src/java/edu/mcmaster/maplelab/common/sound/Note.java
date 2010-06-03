/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: Note.java 489 2009-07-06 23:18:19Z bhocking $
*/
package edu.mcmaster.maplelab.common.sound;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiUnavailableException;

/**
 * A pitch with a duration.
 * @version   $Revision: 489 $
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   May 16, 2006
 */
public class Note implements Playable {
    private final Pitch _pitch;
    /**
     * @uml.property  name="duration"
     */
    private final int _duration;
    private final int _label;
    private final float _volume;
    private static int _instrument = -1;
    private static ToneGenerator _player;
    private final boolean _blocks;

    /**
     * Standard ctor.
     *
     * @param pitch pitch to play
     * @param duration duration of playback in milliseconds.
     */
    public Note(Pitch pitch, int duration) {
        this(pitch, duration, -1);
    }

    /**
     * Ctor with label.
     *
     * @param pitch pitch to play
     * @param duration duration of playback in milliseconds.
     * @param label identifying label
     */
    public Note(Pitch pitch, int duration, int label) {
        this(pitch, duration, 1.0f, label);
    }

    /**
     * Ctor with label.
     *
     * @param pitch pitch to play
     * @param duration duration of playback in milliseconds.
     * @param label Identifying integer label (e.g., which beat in a measure)
     * @param volume Value from 0 to 1.984375
     */
    public Note(Pitch pitch, int duration, float volume, int label) {
        this(pitch, duration, volume, label, true);
    }

    public Note(Pitch pitch, int duration, float volume, int label, boolean blocks) {
        _pitch = (volume <= 0) ? null : pitch;
        _duration = duration;
        _label = label;
        _volume = (pitch == null) ? 0 : ToneGenerator.fromIntVolume(ToneGenerator.toIntVolume(volume));
        _blocks = blocks;
    }

    /**
     * Create a rest.
     * @param restDuration duration of rest in milliseconds.
     */
    public Note(int restDuration) {
        this(null, restDuration, 0, -1);
    }

    public static void setInstrument(int instrument) {
        _instrument = instrument;
        _player = null; // So next time it's requested, it'll generate a new one
    }

    public static ToneGenerator getDefaultPlayer() throws MidiUnavailableException {
        if (_player == null) {
            if (_instrument < 0) {
                _player = new ToneGenerator();
            } else {
                _player = new ToneGenerator(_instrument);
            }
        }
        return _player;
    }

    /**
     * @return Pitch specification.
     */
    public Pitch getPitch() {
        return _pitch;
    }

    /**
     * @return Duration in milliseconds.
     */
    public int getDuration() {
        return _duration;
    }

    /**
     * @return Identifying label
     */
    public int getLabel() {
        return _label;
    }

    /**
     * @return Volume, with 1 being "normal" volume, and 2 being maximum
     */
    public float getVolume() {
        return _volume;
    }

    /**
     * Debugging string representation.
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return (getLabel()<0?"":getLabel()) + "(" + (getPitch()==null?"Rest":getPitch()) + ":" + getDuration() + ":" + getVolume() +")";
    }

    public void play() {
        try {
            ToneGenerator player = Note.getDefaultPlayer();
            if (_blocks || _pitch != null) {
                player.play(this, _blocks);
            }
        }
        catch (MidiUnavailableException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    public int duration() {
        return getDuration();
    }

    public String name() {
        return toString();
    }

    /**
     * Test code
     *
     * @param arg Ignored
     */
    public static void main(String[] arg) {
        List<Note> notes = new ArrayList<Note>();
        notes.add(new Note(100));
        notes.add(new Note(new Pitch(NotesEnum.A, 3), 200));
        notes.add(new Note(new Pitch(NotesEnum.AS, 3), 200, 1));
        notes.add(new Note(new Pitch(NotesEnum.B, 3), 200, 1.2f, 2));
        notes.add(new Note(new Pitch(NotesEnum.C, 4), 200, 2f, 3));
        notes.add(new Note(new Pitch(NotesEnum.CS, 4), 200, 2.5f, 4));
        notes.add(new Note(new Pitch(NotesEnum.D, 4), 200, -0.5f, 5));
        for (Note n: notes) {
            System.out.println(n);
            n.play();
        }
    }
}
