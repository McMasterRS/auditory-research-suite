/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the University of
 * Virginia from the National Eye Institute and the National Institute of Deafness and
 * Communicative Disorders. PI: Prof. Michael Kubovy <kubovy@virginia.edu> Distributed
 * under the terms of the GNU Lesser General Public License (LGPL). See LICENSE.TXT that
 * came with this file. $Id: ToneGenerator.java 489 2009-07-06 23:18:19Z bhocking $
 */
package edu.mcmaster.maplelab.common.sound;

import java.util.*;
import java.util.logging.Level;

import javax.sound.midi.*;

import edu.mcmaster.maplelab.common.LogContext;

/**
 * Encapsulation of device used to generating tones from Pitch Stimulus.
 * @version $Revision: 489 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Apr 17, 2006
 */
public class ToneGenerator {

    private static final int MIDI_TRACK_VOLUME_CMD = 127;
    private static final int MIDI_NORMAL_VOLUME = 64;
    private static final int PITCH_BEND_MAX = 16383;
    private static final int PITCH_BEND_NONE = 8192;

    private static final int MIDI_VOLUME_CMD = 7;
    // private static final short MIDI_BANK_MSB_FLUTE = 0;
    // private static final short MIDI_BANK_LSB_FLUTE = 74;
    private static final int MARIMBA = 13;

    private Synthesizer _synthesizer;
    private MidiChannel[] _midiChannels;

    /**
     * Number of pitch bend units per cent. Although not guaranteed, the General midi spec
     * states that a full pitch bend should be +/- two semitones. This has been verified
     * with an external tuner.
     */
    private static final float PITCH_BEND_PER_CENTS = (PITCH_BEND_MAX - PITCH_BEND_NONE) / 200f;

    // Constants for computing and managing timing
    private static final int TICKS_PER_MILLISECOND = 1;
    public static final int TEMPO_BEATS_PER_MINUTE = 60;
    private static final int TICKS_PER_QNOTE = 1000;

    private static ToneGenerator _singleton = null;

    public static ToneGenerator getInstance() throws MidiUnavailableException {
        if (_singleton == null) {
            _singleton = new ToneGenerator();
        }
        return _singleton;
    }

    private Sequencer _sequencer;
//    private Receiver _synthesizerReceiver;
    private SequenceLatch _latch;
    // private short _midiBankLSB = MIDI_BANK_LSB_FLUTE;
    // private short _midiBankMSB = MIDI_BANK_MSB_FLUTE;

    private Map<Track, Integer> _lastDetunes = new HashMap<Track, Integer>();

    public ToneGenerator(int instrument) throws MidiUnavailableException {
        // The default sequencer should already be attached to the default synth.
        _synthesizer = MidiSystem.getSynthesizer();
        _synthesizer.open();
        _midiChannels = _synthesizer.getChannels();
        _synthesizer.loadInstrument(_synthesizer.getAvailableInstruments()[instrument-1]);
//        _synthesizer.loadAllInstruments(_synthesizer.getDefaultSoundbank());
        for (int i = 0; i < _midiChannels.length; i++) {
            _midiChannels[i].programChange(instrument-1);
        }
        _sequencer = MidiSystem.getSequencer();
        _sequencer.addMetaEventListener(_latch = new SequenceLatch());
        _sequencer.open();
    }

    public ToneGenerator() throws MidiUnavailableException {
        this(MARIMBA);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            _sequencer.stop();
            _sequencer.close();
            _synthesizer.close();
        }
        catch (Exception e) {
        }
    }

    /**
     * Get the sequencer used by this.
     */
    public Sequencer getSequencer() {
        return _sequencer;
    }

    /**
     * Set the midi sound.
     *
     * @param msb
     * @param lsb
     */
    public void setMidiBank(short msb, short lsb) {
        // _midiBankMSB = msb;
        // _midiBankLSB = lsb;
    }

    /**
     * Playback a single note. Blocks until playback finishes.
     *
     * @param note note to play
     */
    public void play(Note note) {
        play(note, true);
    }

    public void play(Note note, boolean blocks) {
        List<Note> singleNote = Collections.singletonList(note);
        play(singleNote, blocks);
    }

    /**
     * Create the default sequence configuration.
     */
    public static Sequence defaultSequence() throws InvalidMidiDataException {
        return new Sequence(Sequence.PPQ, TICKS_PER_QNOTE);
    }

    /**
     * Play a series of notes on the default sequencer. Blocks until sequence playback
     * finishes.
     *
     * @param notes notes to play.
     */
    public void play(List<Note> notes) {
        play(notes, true);
    }

    // From http://www.ibm.com/developerworks/library/it/it-0801art38/
    public static void playChannel(MidiChannel channel, int[] notes, int[] velocities,
                                   int[] durations) {
        for (int i = 0; i < notes.length; i++) {
            channel.noteOn(notes[i], velocities[i]);
            try {
                Thread.sleep(durations[i]);
            }
            catch (InterruptedException e) {
            }
        } // for
        for (int i = 0; i < notes.length; i++)
            channel.noteOff(notes[i]);
    } // playChannel


    /**
     * Play a series of notes on the default sequencer. Blocks until sequence playback
     * finishes.
     *
     * @param notes notes to play.
     */
    public Sequence play(List<Note> notes, boolean block) {
        Sequence retval = null;
        try {
            _lastDetunes.clear();
            retval = defaultSequence();
            Track track = retval.createTrack();
            prepareTrack(track);

            int cumulativeDuration = 0;
            int[] cNotes = new int[notes.size()];
            int[] cVelocities = new int[notes.size()];
            int[] cDurations = new int[notes.size()];
            int i = 0;
            for (Note n : notes) {
                int noteDuration = n.getDuration();
                float volume = n.getVolume();
                Pitch ps = n.getPitch();
                if (ps != null && volume > 0) {
                    int midiNote = ps.toMidiNoteNumber();
                    int detune = ps.getDetuneCents();

                    long ticks = toTicks(cumulativeDuration);
                    MidiEvent detuneEvent = createDetuneEvent(track, detune, ticks);
                    if (detuneEvent != null) {
                        track.add(detuneEvent);
                    }
                    track.add(createNoteOnEvent(midiNote, volume, ticks));
                    track.add(createNoteOffEvent(midiNote, toTicks(cumulativeDuration
                            + noteDuration)));

                }
                cNotes[i] = (n.getPitch() != null) ? n.getPitch().toMidiNoteNumber() : 0;
                cVelocities[i] = toIntVolume(n.getVolume());
                cDurations[i] = n.getDuration();
                i++;
                cumulativeDuration += noteDuration;

            }
            _sequencer.setSequence(retval);
            setTempo(_sequencer, TEMPO_BEATS_PER_MINUTE);

            long milli = retval.getMicrosecondLength() / 1000;
            if (block) {
                // Currently, this doesn't work with MIDI instruments. Don't know why.
              _sequencer.start();
            }
            else {
                playChannel(_midiChannels[0], cNotes, cVelocities, cDurations);
            }

            if (block) {
                synchronized (_latch) {
                    // Wait for the sequence to end
                    _latch.wait(milli * 4);
                    _sequencer.stop();
                }
            }
        }
        catch (Exception ex) {
            LogContext.getLogger().log(Level.WARNING, "Unexpected MIDI error", ex);
            MidiUnavailableException wrapper = new MidiUnavailableException("Unexpected MIDI error");
            wrapper.initCause(ex);
        }
        return retval;
    }

    /**
     * Convenience method for stopping the sequencer
     */
    public void stop() {
        _sequencer.stop();
        _sequencer.close();
    }

    /**
     * Convenience method for determining if the sequence is still playing
     *
     * @return Whether it is still playing
     */
    public boolean isRunning() {
        return _sequencer.isRunning();
    }

    // private MetaMessage createTempoEvent() {
    // byte b1, b2, b3;
    // MetaMessage retval = new MetaMessage();
    // byte[] date = new byte[] { 0xff, 0x51, 0x03, b1, b2, b3 };
    // retval.setMessage(i, abyte0, j)
    // }

    private void setTempo(Sequencer sequencer, int tempoBPM) {
        // Per: http://www.jsresources.org/faq_midi.html#tempo_methods
        float current = sequencer.getTempoInBPM();
        float factor = tempoBPM / current;
        sequencer.setTempoFactor(factor);
    }

    /**
     * Compute the number of sequencer ticks.
     *
     * @param milliseconds time in milliseconds
     * @return time in ticks.
     */
    public static long toTicks(long milliseconds) {
        return milliseconds * TICKS_PER_MILLISECOND;
    }

    /**
     * Create the event to start the note playback.
     * @param note midi note (i.e., pitch)
     * @param volume note volume (between 0 and 1.984375)
     * @param ticks When to start the note
     * @return note event
     */
    public static MidiEvent createNoteOnEvent(int note, float volume, long ticks)
            throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage();
        // volume must be between 0 and 2
        msg.setMessage(ShortMessage.NOTE_ON, note, toIntVolume(volume));
        return new MidiEvent(msg, ticks);
    }

    /**
     * Create event to turn note off.
     *
     * @param note note to turn off.
     */
    public static MidiEvent createNoteOffEvent(int note, long ticks)
            throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_OFF, note, 0);

        return new MidiEvent(msg, ticks);
    }

    /**
     * Create the event to adjust the tuning of the note playback following this event.
     * @param track
     *
     * @param detuneCents amount to adjust in cents, where 100 cents = semitone.
     * @return detune event
     */
    private MidiEvent createDetuneEvent(Track track, int detuneCents, long ticks)
            throws InvalidMidiDataException {
        // Apply any detune via the pitch bend controller.
        Integer lastDetune = _lastDetunes.get(track);
        if (lastDetune != null && lastDetune.intValue() == detuneCents) {
            return null;
        }

        _lastDetunes.put(track, detuneCents);

        ShortMessage msg = new ShortMessage();
        int amount = (int) (PITCH_BEND_NONE + PITCH_BEND_PER_CENTS * detuneCents);
        msg.setMessage(ShortMessage.PITCH_BEND, amount % 128, amount / 128);

        return new MidiEvent(msg, ticks);
    }

    /**
     * Prepare the track for tone playback.
     *
     * @param track track to populate with setup events.
     */
    public static void prepareTrack(Track track) throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.CONTROL_CHANGE, MIDI_VOLUME_CMD, MIDI_TRACK_VOLUME_CMD);
        track.add(new MidiEvent(msg, 0));

    }

    public static void setMidiBank(Track track, int midiBankLSB, int midiBankMSB)
            throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.PROGRAM_CHANGE, midiBankLSB, midiBankMSB);
        track.add(new MidiEvent(msg, 0));
    }

    private class SequenceLatch implements MetaEventListener {

        private static final int MIDI_TRACK_END = 47;

        public void meta(MetaMessage meta) {
            // Check for end of track message.
            if (meta.getType() == MIDI_TRACK_END) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }

    /**
     * Debugging code for inspecting sequence.
     */
    public static String toDebugString(Sequencer sequencer) {
        StringBuffer buf = new StringBuffer();

        buf.append("Sequencer:\n");
        buf.append("\ttempo factor = " + sequencer.getTempoFactor() + "\n");
        buf.append("\ttempo BPM = " + sequencer.getTempoInBPM() + "\n");
        buf.append("\ttempo MPQ = " + sequencer.getTempoInMPQ() + "\n");
        buf.append("\ttick length= " + sequencer.getTickLength() + "\n");
        buf.append("\ttick position = " + sequencer.getTickPosition() + "\n");
        buf.append("\tmicrosecond length = " + sequencer.getMicrosecondLength() + "\n");
        buf.append("\tmicrosecond position = " + sequencer.getMicrosecondPosition() + "\n");
        buf.append("\tmaster sync mode = " + sequencer.getMasterSyncMode() + "\n");

        return buf.toString();
    }

    /**
     * Returns a float representation (between 0 and 1.984375) of the integer volume
     *
     * @param volume Integer between 0 and 127 indicating the volume
     * @return
     */
    public static float fromIntVolume(int volume) {
        return ((float) volume) / MIDI_NORMAL_VOLUME;
    }

    /**
     * Returns an integer representation (between 0 and 127) of the float volume. Values
     * below 0 are set to 0, and values above 127 are set to 127.
     *
     * @param volume Float between 0 and 1.984375 indicating the volume
     * @return
     */
    public static int toIntVolume(float volume) {
        int result = Math.round(volume * MIDI_NORMAL_VOLUME);
        return (result > 127) ? 127 : (result < 0) ? 0 : result;
    }

    /**
     * Test code.
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            ToneGenerator tg = new ToneGenerator();

            Note[] notes = new Note[] {
                new Note(new Pitch(NotesEnum.C, 4), 200, 1, 1), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1.2f, 1), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1.4f, 1), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 0.25f, 1), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1, 2), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1, 2), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1, 2), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1, 2), new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4), 200, 1, 2)
            };

            long start = System.currentTimeMillis();
            tg.play(Arrays.asList(notes));
            long end = System.currentTimeMillis();
            System.out.println("Elapsed time: " + (end - start) + " ms");
            System.out.println("Expected time: " + ((9 * 200) + (8 * 250)) + " ms");
            tg.play(Arrays.asList(notes), false);
            start = System.currentTimeMillis();
            while (tg.isRunning()) {
                Thread.sleep(0, 250);
            }
            end = System.currentTimeMillis();
            System.out.println("Elapsed time: " + (end - start) + " ms");
            System.out.println("Expected time: " + ((9 * 200) + (8 * 250)) + " ms");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
