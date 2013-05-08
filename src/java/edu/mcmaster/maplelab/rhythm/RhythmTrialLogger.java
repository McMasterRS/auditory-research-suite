/*
 * Copyright (C) 2006 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id$
 */
package edu.mcmaster.maplelab.rhythm;

import java.io.*;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.common.datamodel.FileType;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;
import edu.mcmaster.maplelab.common.sound.MidiInterpreter;
import edu.mcmaster.maplelab.common.sound.MultiMidiEvent;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Specialization of trial logger for posting Trial data.
 * 
 * @version $Revision$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Nov 3, 2006
 */
public class RhythmTrialLogger extends
    FileTrialLogger<RhythmSession, RhythmTrial> {

    public enum Keys {
    	baseIOI, 
        offsetDegree, 
        withTap, 
        confidence, 
        subjResponse,
        responseCorrect,
        data_type
    }
    
    public enum DataType {
    	tap_computer, tap_subject, response
    }

    /** Delegate for recording tapping data. */
    private final TapLogger _tapLogger;

    public RhythmTrialLogger(RhythmSession session, File workingDirectory) throws IOException {
        super(session, workingDirectory);
        
        _tapLogger = new TapLogger(session, workingDirectory);
    }

	@Override
	protected void loadAdditionalFileTypes() {
		// none needed
	}
    
    @Override
    protected File getCollectedOutputFile() {
    	return getOutputFile(FileType.get(RESPONSE_ALL_FILE));
    }
    
    @Override
    protected File createFile() {
    	return getOutputFile(FileType.get(RESPONSE_FILE));
    }

	@Override
	protected Set<? extends Enum<?>> getTrialDataHeaders() {
		return EnumSet.allOf(Keys.class);
	}

	@Override
	protected Set<? extends Enum<?>> getGeneralDataHeaders() {
		return null;
	}

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(RhythmTrial trial) {
    	
        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);
        
        // Calculate trial parameters
        fields.put(Keys.baseIOI, String.valueOf(trial.getBaseIOI()));
        fields.put(Keys.offsetDegree, String.valueOf(trial.getOffsetDegree()));
        fields.put(Keys.withTap, String.valueOf(trial.isWithTap()));
        
        // Output subject response information
        ConfidenceResponse response = trial.getResponse();
        fields.put(Keys.confidence, String.valueOf(response.getValue().ordinal()));
        fields.put(Keys.subjResponse, String.valueOf(
        		RhythmResponseParameters.isProbeToneAccurate(response)));
        fields.put(Keys.responseCorrect, String.valueOf(trial.isResponseCorrect()));
        fields.put(Keys.data_type, DataType.response.name());

        return fields;
    }

	@Override
	protected EnumMap<? extends Enum<?>, String> marshalGeneralDataToMap(RhythmTrial trial) {
		return null;
	}
    
    @Override
    public void submit(RhythmTrial trial) throws IOException {
        super.submit(trial);
        _tapLogger.submit(trial);
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        _tapLogger.shutdown();
    }

    /**
     * Delegate logger for recording tap data when available. Uses
     * FileTrialLogger base class mostly out of convenience.
     */
    private static class TapLogger extends FileTrialLogger<RhythmSession, RhythmTrial> {
    	private static final String TAP_TEXT_FILE = "tap_text";
    	private static final String TAP_MIDI_FILE = "tap_midi";
    	private static final String TAP_TEXT_ALL_FILE = "tap_all";

    	private enum TapKeys {
            baseIOI, 
            offsetDegree,
            withTap,
            opcode,
            channel,
            key,
            velocity,
            type, 
            beat, 
            tap,
            data_type
        }
    	
    	private enum MidiKeys {
            midi_dev_id,
            midi_dev_name,
            midi_dev_version,
            midi_dev_vendor,
    	}

        private enum TapType {
            sync, critical, participant
        }
        
        private enum OpType { 
        	note_on, note_off
        }

        private TapType _type;
        private MidiEvent _currEvent;

        public TapLogger(RhythmSession session, File workingDirectory) throws IOException {
            super(session, workingDirectory);
        }

		@Override
		protected void loadAdditionalFileTypes() {
			FileType.create(TAP_TEXT_FILE, "txt", "taps", false, false);
			FileType.create(TAP_MIDI_FILE, "mid", "taps", false, true);
			FileType.create(TAP_TEXT_ALL_FILE, "txt", "taps", true, false);
		}
        
        @Override
        protected File getCollectedOutputFile() {
        	return getOutputFile(getSession(), FileType.get(TAP_TEXT_ALL_FILE));
        }
        
        @Override
        protected File createFile() {
        	return getOutputFile(getSession(), FileType.get(TAP_TEXT_FILE));
        }
        
        /**
         * Create a file for midi output for the given block and trial.
         */
        private File createMidiFile(RhythmTrial trial) {
        	return getOutputFile(getSession(), FileType.get(TAP_MIDI_FILE), trial);
        }
        
        @Override
        protected EnumMap<? extends Enum<?>, String> marshalTrialDataToMap(RhythmTrial trial) {
        	
            EnumMap<TapKeys, String> fields = new EnumMap<TapKeys, String>(TapKeys.class);
            
            fields.put(TapKeys.baseIOI, String.valueOf(trial.getBaseIOI()));
            fields.put(TapKeys.offsetDegree, String.valueOf(trial.getOffsetDegree()));
            fields.put(TapKeys.withTap, String.valueOf(trial.isWithTap()));
            
            MidiMessage m = _currEvent.getMessage();
            OpType ot = MidiInterpreter.getOpcode(m) == ShortMessage.NOTE_ON ? 
            		OpType.note_on : OpType.note_off;
            
            fields.put(TapKeys.opcode, ot.name());
            fields.put(TapKeys.channel, String.valueOf(MidiInterpreter.getChannel(m)));
            fields.put(TapKeys.key, String.valueOf(MidiInterpreter.getKey(m)));
            fields.put(TapKeys.velocity, String.valueOf(MidiInterpreter.getVelocity(m)));
            fields.put(TapKeys.type, _type.toString());

            long tick = _currEvent.getTick();
            String beat = _type != TapType.participant ? String.valueOf(tick) : "-";
            String tap = _type == TapType.participant ? String.valueOf(tick) : "-";
            DataType data = _type == TapType.participant ? DataType.tap_subject :
                    DataType.tap_computer;

            fields.put(TapKeys.beat, beat);
            fields.put(TapKeys.tap, tap);
            fields.put(TapKeys.data_type, data.name());

            return fields;
        }

        /**
         * {@inheritDoc}
         * 
         * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#submit(edu.mcmaster.maplelab.common.datamodel.Block,
         *      edu.mcmaster.maplelab.common.datamodel.Trial)
         */
        @Override
        public void submit(RhythmTrial trial) throws IOException {
            Sequence recording = trial.getRecording();
            if (recording == null) return;
            
            writeMidiFile(recording, trial);
            writeTextFile(recording, trial);
        }
        
        /**
         * Write the designated midi file to contain tracks for experiment and participant events.
         */
        private void writeMidiFile(Sequence recording, RhythmTrial trial) throws IOException {
        	MidiSystem.write(recording, 1, createMidiFile(trial));
		}

		/**
         * Write a text file containing a table of all block and trial information.
         */
        private void writeTextFile(Sequence recording, RhythmTrial trial) throws IOException {
            File textFile = getFile();

            boolean addHeader = !textFile.exists();

            FileWriter out = null;
            try {
                out = new FileWriter(textFile, true);
                if (addHeader) {
                    writeHeader(buildHeaderSets(), out);
                }

                Track[] tracks = recording.getTracks();
                for (int i = 0; i < tracks.length; i++) {
                    Track track = tracks[i];
                    _type = i == 0 ? TapType.sync : TapType.participant;
                    for (int j = 0; j < track.size(); j++) {
                        // Saving current event data as class members is a
                        // hack to allow us to reuse the marshalToMap
                        // method, just because it's conceptually clean
                    	MultiMidiEvent mme = MultiMidiEvent.getMultiEvent(track.get(j));
                    	_currEvent = mme.hasModifiedSource() ? mme.getModifiedSourceEvent() : mme;
                        
                        if (!MidiInterpreter.isNoteOnEvent(_currEvent) && 
                        		(!getSession().recordNoteOffEvents() || !MidiInterpreter.isNoteOffEvent(_currEvent))) {
                        	continue;
                    	}
                        
                        // Nasty hack to figure out if this might be the last
                        // note on event for beat.
                        if(i == 0 && j >= track.size() - 3) {
                            _type = TapType.critical;
                        }

                        writeRow(buildDataSets(trial), out);
                    }
                }
            }
            finally {
                if (out != null) out.close();
            }
        }

		@Override
		protected Set<? extends Enum<?>> getGeneralDataHeaders() {
			return EnumSet.allOf(MidiKeys.class);
		}

		@Override
		protected Set<? extends Enum<?>> getTrialDataHeaders() {
			return EnumSet.allOf(TapKeys.class);
		}

		@Override
		protected EnumMap<? extends Enum<?>, String> marshalGeneralDataToMap(RhythmTrial trial) {
        	
            EnumMap<MidiKeys, String> fields = new EnumMap<MidiKeys, String>(MidiKeys.class);
            
            RhythmSession session = getSession();
            int id = session.getMIDIInputDeviceID();
            Info info = MidiSystem.getMidiDeviceInfo()[session.getMIDIInputDeviceID()];
            
            fields.put(MidiKeys.midi_dev_id, String.valueOf(id));
            fields.put(MidiKeys.midi_dev_name, info.getName());
            fields.put(MidiKeys.midi_dev_version, info.getVersion());
            fields.put(MidiKeys.midi_dev_vendor, info.getVendor());
            
            return fields;
		}
    }
}
