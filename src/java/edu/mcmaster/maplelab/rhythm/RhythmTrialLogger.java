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

import edu.mcmaster.maplelab.common.datamodel.FileTrialLogger;
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
        subject, 
        session, 
        block_num, 
        trial_num, 
        baseIOI, 
        offsetDegree, 
        withTap, 
        confidence, 
        accurate
    }

    /** Delegate for recording tapping data. */
    private final TapLogger _tapLogger;

    public RhythmTrialLogger(RhythmSession session, File workingDirectory, String basename) throws IOException {
        super(session, workingDirectory, basename);
        
        _tapLogger = new TapLogger(session, workingDirectory, basename + "-tap");
    }

    @Override
    protected EnumMap<? extends Enum<?>, String> marshalToMap(
        RhythmBlock block, RhythmTrial trial, int responseNum) {

        RhythmSession session = getSession();

        EnumMap<Keys, String> fields = new EnumMap<Keys, String>(Keys.class);

        fields.put(Keys.subject, String.valueOf(session.getSubject()));
        fields.put(Keys.session, String.valueOf(session.getSession()));
        fields.put(Keys.block_num, String.valueOf(block.getNum()));
        fields.put(Keys.trial_num, String.valueOf(trial.getNum()));
        fields.put(Keys.baseIOI, String.valueOf(trial.getBaseIOI()));
        fields.put(Keys.offsetDegree, String.valueOf(trial.getOffsetDegree()));
        fields.put(Keys.withTap, String.valueOf(trial.isWithTap()));
        Response response = trial.getResponse();
        fields.put(Keys.confidence, String.valueOf(response.getConfidence()
            .ordinal()));
        fields.put(Keys.accurate, String.valueOf(response
            .getProbeToneAccurate()));

        return fields;
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
    private static class TapLogger extends
        FileTrialLogger<RhythmSession, RhythmBlock, RhythmTrial> {

        private enum TapKeys {
            session,
            subject,
            trial_num, 
            block_num, 
            baseIOI, 
            offsetDegree,
            type, 
            beat, 
            tap
        }

        private enum TapType {
            sync, critical, participant
        }

        private TapType _type;

        private int _tick;

        public TapLogger(RhythmSession session, File workingDirectory, String basename) throws IOException {
            super(session, workingDirectory, basename);
        }

        @Override
        protected EnumMap<? extends Enum<?>, String> marshalToMap(
            RhythmBlock block, RhythmTrial trial, int responseNum) {
            EnumMap<TapKeys, String> fields = new EnumMap<TapKeys, String>(
                TapKeys.class);

            RhythmSession session = getSession();
            
            fields.put(TapKeys.subject, String.valueOf(session.getSubject()));
            fields.put(TapKeys.session, String.valueOf(session.getSession()));
            fields.put(TapKeys.block_num, String.valueOf(block.getNum()));
            fields.put(TapKeys.trial_num, String.valueOf(trial.getNum()));
            fields.put(TapKeys.baseIOI, String.valueOf(trial.getBaseIOI()));
            fields.put(TapKeys.offsetDegree, String.valueOf(trial.getOffsetDegree()));
            fields.put(TapKeys.type, _type.toString());

            String beat = _type != TapType.participant ? String.valueOf(_tick)
                : "-";
            String tap = _type == TapType.participant ? String.valueOf(_tick)
                : "-";

            fields.put(TapKeys.beat, beat);
            fields.put(TapKeys.tap, tap);

            return fields;
        }

        /**
         * {@inheritDoc}
         * 
         * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#submit(edu.mcmaster.maplelab.common.datamodel.Block,
         *      edu.mcmaster.maplelab.common.datamodel.Trial)
         */
        @Override
        public void submit(RhythmBlock block, RhythmTrial trial)
            throws IOException {
            Sequence recording = trial.getRecording();
            if (recording == null) return;

            File file = getFile();

            boolean addHeader = !file.exists();

            FileWriter out = null;
            try {
                out = new FileWriter(file, true);
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
                        if(currEvent.getMessage().getStatus() != ShortMessage.NOTE_ON) continue;
                        
                        _tick = (int) currEvent.getTick();
                        
                        // Nasty hack to figure out if this might be the last
                        // note on event for beat.
                        if(i == 0 && j >= track.size() - 3) {
                            _type = TapType.critical;
                        }

                        EnumMap<? extends Enum<?>, String> map = marshalToMap(
                            block, trial, 0);
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
