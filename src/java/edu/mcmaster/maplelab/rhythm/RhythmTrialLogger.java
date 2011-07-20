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

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.sound.MidiInterpreter;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Specialization of trial logger for posting Trial data.
 * 
 * @version $Revision$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Nov 3, 2006
 */
public class RhythmTrialLogger extends
    FileTrialLogger<RhythmSession, RhythmBlock, RhythmTrial> {

    public enum Keys {
    	exp_id,
		sub_exp_id,
		exp_build,
    	exp_build_date,
    	ra_id,
    	subject, 
        session, 
        trial_num,
        repetition_num,
        trial_in_repetition,
        block_num, 
        trial_in_block,
        time_stamp, 
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

    public enum FileType {
    	TAP_TEXT,
    	TAP_MIDI,
    	RESPONSE,
    	DEBUG {
    		@Override
    		public boolean needsBlockTrialData() {
    			return true;
    		}
    	},
    	TAP_ALL,
    	RESPONSE_ALL;
    	
    	/**
    	 * Indicate if this file type needs block and trial data
    	 * in order to calculate a file name.
    	 */
    	public boolean needsBlockTrialData() {
    		return false;
    	}
    }

    /** Delegate for recording tapping data. */
    private final TapLogger _tapLogger;

    public RhythmTrialLogger(RhythmSession session, File workingDirectory) throws IOException {
        super(session, workingDirectory);
        
        _tapLogger = new TapLogger(session, workingDirectory);
    }
    
    @Override
    protected File getCollectedOutputFile() {
    	return getOutputFile(FileType.RESPONSE_ALL);
    }
    
    @Override
    protected File createFile() {
    	return getOutputFile(FileType.RESPONSE);
    }

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalToMap(
        RhythmBlock block, RhythmTrial trial, int responseNum, MidiEvent event) {
    	
    	RhythmSession session = getSession();
    	
        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);

        // Meta information
        fields.put(Keys.exp_id, session.getExperimentID());
        fields.put(Keys.sub_exp_id, session.getSubExperimentID());
        fields.put(Keys.exp_build, RhythmExperiment.getBuildVersion());
        fields.put(Keys.exp_build_date, RhythmExperiment.getBuildDate());
        fields.put(Keys.ra_id, session.getRAID());
        fields.put(Keys.subject, String.valueOf(session.getSubject()));
        fields.put(Keys.session, String.valueOf(session.getSession()));
        
        // Calculate trial numbers and parameters
        int trial_in_rep = (block.getNum()-1)*block.getNumTrials() + trial.getNum();
        int overall_trial = (session.getCurrentRepetition()-1)*session.getNumBlocks()*block.getNumTrials() + trial_in_rep;
    	fields.put(Keys.trial_num, String.valueOf(overall_trial));
        fields.put(Keys.repetition_num, String.valueOf(session.getCurrentRepetition()));
        fields.put(Keys.trial_in_repetition, String.valueOf(trial_in_rep));
        fields.put(Keys.block_num, String.valueOf(block.getNum()));
        fields.put(Keys.trial_in_block, String.valueOf(trial.getNum()));
        fields.put(Keys.time_stamp, trial.getTimeStamp());
        fields.put(Keys.baseIOI, String.valueOf(trial.getBaseIOI()));
        fields.put(Keys.offsetDegree, String.valueOf(trial.getOffsetDegree()));
        fields.put(Keys.withTap, String.valueOf(trial.isWithTap()));
        
        // Output subject response information
        Response response = trial.getResponse();
        fields.put(Keys.confidence, String.valueOf(response.getConfidence().ordinal()));
        fields.put(Keys.subjResponse, String.valueOf(
        		RhythmResponseParameters.isProbeToneAccurate(response)));
        fields.put(Keys.responseCorrect, String.valueOf(trial.isResponseCorrect()));
        fields.put(Keys.data_type, DataType.response.name());

        return fields;
    }
    
    /**
     * Get the output file for the given file type.  Not valid for file types
     * needing block and trial information to calculate a file name.
     */
    public File getOutputFile(FileType type) throws UnsupportedOperationException {
    	return getOutputFile(getSession(), type, null, null);
    }
    
    /**
     * Get the output file for the given file type.  Not valid for file types
     * needing block and trial information to calculate a file name.
     */
    public static File getOutputFile(RhythmSession rs, FileType type) throws UnsupportedOperationException {
    	if (type.needsBlockTrialData()) {
    		throw new UnsupportedOperationException(
    				String.format("File type %s needs block and trial information " +
    						"to calculate a file name.", type));
    	}
    	return getOutputFile(rs, type, null, null);
    }
    
    public File getOutputFile(FileType type, RhythmBlock block, RhythmTrial trial) {
    	return getOutputFile(getSession(), type, block, trial);
    }

    /**
     * Get the output file for the given file type.  Creates directories as
     * needed.
     */
    public static File getOutputFile(RhythmSession rs, FileType type, RhythmBlock block, RhythmTrial trial) {
    	String fName = null;
    	switch(type) {
	    	case TAP_ALL:
	    		fName = String.format("ex%s-taps.txt", rs.getExperimentID());
	    		break;
	    	case RESPONSE_ALL:
	    		fName = String.format("ex%s-responses.txt", rs.getExperimentID());
	    		break;
	    	case TAP_TEXT:
	    		fName = String.format("sub%s-sess%s-ex%s-subex%s-%s-taps.txt", rs.getSubject(), rs.getSession(),
	    				rs.getExperimentID(), rs.getSubExperimentID(), getTimeStamp());
	    		break;
	    	case TAP_MIDI:
	    		fName = String.format("sub%s-sess%s-ex%s-subex%s-b%s-t%s-%s-taps.mid", rs.getSubject(), rs.getSession(),
	    				rs.getExperimentID(), rs.getSubExperimentID(), block.getNum(), trial.getNum(), getTimeStamp());
	    		break;
	    	case RESPONSE:
	    		fName = String.format("sub%s-sess%s-ex%s-subex%s-%s-responses.txt", rs.getSubject(), rs.getSession(),
	    				rs.getExperimentID(), rs.getSubExperimentID(), getTimeStamp());
	    		break;
	    	case DEBUG: 
	    		fName = String.format("sub%s-sess%s-ex%s-subex%s-%s-debug.log", rs.getSubject(), rs.getSession(),
	    				rs.getExperimentID(), rs.getSubExperimentID(), getTimeStamp());
	    		break;
    	}
    	return new File(getOutputDirectory(rs, type), fName);
    }
    
    /**
     * Get the output sub-directory for the given file type, starting
     * from the given base output directory.  Creates directories as
     * needed.
     */
    private static File getOutputDirectory(RhythmSession rs, FileType type) {
    	switch(type) {
	    	case TAP_ALL:
	    	case RESPONSE_ALL:
	    		return getCombinedFileDirectory(rs);
	    	case TAP_TEXT:
	    	case TAP_MIDI:
	    	case RESPONSE:
	    	case DEBUG: 
	    	default:
	    		return getSingleFileDirectory(rs);
    	}
    }
    
    /**
     * Get the directory in which single output files should be placed.
     */
    private static File getSingleFileDirectory(RhythmSession rs) {
    	String dirName = String.format("%s-Individual" + File.separator + 
    			"Experiment %s" + File.separator + "Subject %s", rs.getExperimentBaseName(),
    			rs.getExperimentID(), rs.getSubject());
    	File dir = new File(getBaseOutputDirectory(), dirName);
    	if (!dir.exists()) dir.mkdirs();
    	return dir;
    }
    
    /**
     * Get the directory in which combined output files should be placed.
     */
    private static File getCombinedFileDirectory(RhythmSession rs) {
    	File dir = new File(getBaseOutputDirectory(), 
    			String.format("%s-Composite", rs.getExperimentBaseName()));
    	if (!dir.exists()) dir.mkdirs();
    	return dir;
    }
    
    @Override
    public void submit(RhythmBlock block, RhythmTrial trial) throws IOException {
        super.submit(block, trial);
        _tapLogger.submit(block, trial);
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
    private static class TapLogger extends FileTrialLogger<RhythmSession, RhythmBlock, RhythmTrial> {

    	private enum TapKeys {
    		exp_id,
    		sub_exp_id,
    		exp_build,
        	exp_build_date,
        	ra_id,
        	subject,
            session,
            midi_dev_id,
            midi_dev_name,
            midi_dev_version,
            midi_dev_vendor,
            trial_num,
            repetition_num,
            trial_in_repetition,
            block_num, 
            trial_in_block, 
            time_stamp,
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

        private enum TapType {
            sync, critical, participant
        }
        
        private enum OpType { 
        	note_on, note_off
        }

        private TapType _type;
        private int _tick;

        public TapLogger(RhythmSession session, File workingDirectory) throws IOException {
            super(session, workingDirectory);
        }
        
        @Override
        protected File getCollectedOutputFile() {
        	return RhythmTrialLogger.getOutputFile(getSession(), FileType.TAP_ALL);
        }
        
        @Override
        protected File createFile() {
        	return RhythmTrialLogger.getOutputFile(getSession(), FileType.TAP_TEXT);
        }
        
        /**
         * Create a file for midi output for the given block and trial.
         */
        private File createMidiFile(RhythmBlock block, RhythmTrial trial) {
        	return RhythmTrialLogger.getOutputFile(getSession(), FileType.TAP_MIDI, block, trial);
        }
        
        @Override
        protected EnumMap<? extends Enum<?>, String> marshalToMap(
            RhythmBlock block, RhythmTrial trial, int responseNum, MidiEvent event) {
            EnumMap<TapKeys, String> fields = new EnumMap<TapKeys, String>(
                TapKeys.class);
            
            RhythmSession session = getSession();
            
            fields.put(TapKeys.exp_id, session.getExperimentID());
            fields.put(TapKeys.sub_exp_id, session.getSubExperimentID());
            fields.put(TapKeys.exp_build, RhythmExperiment.getBuildVersion());
            fields.put(TapKeys.exp_build_date, RhythmExperiment.getBuildDate());
            fields.put(TapKeys.ra_id, session.getRAID());
            fields.put(TapKeys.subject, String.valueOf(session.getSubject()));
            fields.put(TapKeys.session, String.valueOf(session.getSession()));
            
            int id = session.getMIDIInputDeviceID();
            Info info = MidiSystem.getMidiDeviceInfo()[session.getMIDIInputDeviceID()];
            
            fields.put(TapKeys.midi_dev_id, String.valueOf(id));
            fields.put(TapKeys.midi_dev_name, info.getName());
            fields.put(TapKeys.midi_dev_version, info.getVersion());
            fields.put(TapKeys.midi_dev_vendor, info.getVendor());
         
            // Calculate trial numbers
            int trial_in_rep = (block.getNum()-1)*block.getNumTrials() + trial.getNum();
            int overall_trial = (session.getCurrentRepetition()-1)*session.getNumBlocks()*block.getNumTrials() + trial_in_rep;
        	fields.put(TapKeys.trial_num, String.valueOf(overall_trial));
            fields.put(TapKeys.repetition_num, String.valueOf(session.getCurrentRepetition()));
            fields.put(TapKeys.trial_in_repetition, String.valueOf(trial_in_rep));
            fields.put(TapKeys.block_num, String.valueOf(block.getNum()));
            fields.put(TapKeys.trial_in_block, String.valueOf(trial.getNum()));
            fields.put(TapKeys.time_stamp, trial.getTimeStamp());
            fields.put(TapKeys.baseIOI, String.valueOf(trial.getBaseIOI()));
            fields.put(TapKeys.offsetDegree, String.valueOf(trial.getOffsetDegree()));
            fields.put(TapKeys.withTap, String.valueOf(trial.isWithTap()));
            
            MidiMessage m = event.getMessage();
            OpType ot = MidiInterpreter.getOpcode(m) == ShortMessage.NOTE_ON ? 
            		OpType.note_on : OpType.note_off;
            
            fields.put(TapKeys.opcode, ot.name());
            fields.put(TapKeys.channel, String.valueOf(MidiInterpreter.getChannel(m)));
            fields.put(TapKeys.key, String.valueOf(MidiInterpreter.getKey(m)));
            fields.put(TapKeys.velocity, String.valueOf(MidiInterpreter.getVelocity(m)));
            fields.put(TapKeys.type, _type.toString());

            String beat = _type != TapType.participant ? String.valueOf(_tick) : "-";
            String tap = _type == TapType.participant ? String.valueOf(_tick) : "-";
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
        public void submit(RhythmBlock block, RhythmTrial trial) throws IOException {
            Sequence recording = trial.getRecording();
            if (recording == null) return;
            
            writeMidiFile(recording, block, trial);
            writeTextFile(recording, block, trial);
        }
        
        /**
         * Write the designated midi file to contain tracks for experiment and participant events.
         */
        private void writeMidiFile(Sequence recording, RhythmBlock block, RhythmTrial trial) throws IOException {
        	MidiSystem.write(recording, 1, createMidiFile(block, trial));
		}

		/**
         * Write a text file containing a table of all block and trial information.
         */
        private void writeTextFile(Sequence recording, RhythmBlock block, RhythmTrial trial) throws IOException {
            File textFile = getFile();

            boolean addHeader = !textFile.exists();

            FileWriter out = null;
            try {
                out = new FileWriter(textFile, true);
                if (addHeader) {
                    writeHeader(EnumSet.allOf(TapKeys.class), out);
                }

                Track[] tracks = recording.getTracks();
                for (int i = 0; i < tracks.length; i++) {
                    Track track = tracks[i];
                    _type = i == 0 ? TapType.sync : TapType.participant;
                    for (int j = 0; j < track.size(); j++) {
                        // Saving current event data as class members is a
                        // hack to allow us to reuse the marshalToMap
                        // method, just because it's conceptually clean
                        MidiEvent currEvent = track.get(j);
                        
                        if (!MidiInterpreter.isNoteOnEvent(currEvent) && 
                        		(!getSession().recordNoteOffEvents() || !MidiInterpreter.isNoteOffEvent(currEvent))) {
                        	continue;
                    	}
                        
                        _tick = (int) currEvent.getTick();
                        
                        // Nasty hack to figure out if this might be the last
                        // note on event for beat.
                        if(i == 0 && j >= track.size() - 3) {
                            _type = TapType.critical;
                        }

                        EnumMap<? extends Enum<?>, String> map = marshalToMap(
                            block, trial, 0, currEvent);
                        writeRow(map, out);
                    }
                }
            }
            finally {
                if (out != null) out.close();
            }
        }
    }
}
