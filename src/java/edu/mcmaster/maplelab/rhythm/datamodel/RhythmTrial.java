/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/
package edu.mcmaster.maplelab.rhythm.datamodel;

import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.logging.Level;

import javax.sound.midi.Sequence;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceResponse;
import edu.mcmaster.maplelab.common.datamodel.Trial;
import edu.mcmaster.maplelab.common.sound.Note;
import edu.mcmaster.maplelab.common.sound.Pitch;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession.TrialSpecStyle;


/**
 * Encapsulation of the trail data.
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Sep 14, 2006
 */
public class RhythmTrial extends Trial<ConfidenceResponse> {
    private final int _baseIOI;
    private final float _baseIOIoffsetDegree;
    private final int _probeDetuneOffset;
    private final boolean _withTap;
    private Sequence _sequence;

    public RhythmTrial(int baseIOI, float offsetDegree, int probeDetuneOffset, boolean withTap) {
        _baseIOI = baseIOI;
        _baseIOIoffsetDegree = offsetDegree;
        _probeDetuneOffset = probeDetuneOffset;
        _withTap = withTap;
    }

    /**
     * Inter-onset interval in milliseconds.
     */
    public int getBaseIOI() {
        return _baseIOI;
    }

    
    /**
     * Fraction of baseIOI critcal tone is offset from natural position.
     */
    public float getBaseIOIOffsetDegree() {
        return _baseIOIoffsetDegree;
    }

    /**
     * Pitch bend detune for probe tone. Measured in cents (hundredths of semitones). 
     */
    public int getProbeDetuneOffset() {
    	return _probeDetuneOffset;
    }
    
    /**
     * Whether user should tap along with playback.
     */
    public boolean isWithTap() {
        return _withTap;
    }

    /**
     * Set the tap recording data.
     */
    public void setRecording(Sequence sequence) {
        _sequence = sequence;
    }
    
    /**
     * Get the tap recording data, if any.
     */
    public Sequence getRecording() {
        return _sequence;
    }
    
    /**
     * Generate the playback rhythm sequence.
     * Uses legacy note specification if true.
     * @throws InvalidPropertiesFormatException 
     */
    public List<Note> generateSequence(RhythmSession session, TrialSpecStyle style) 
    		throws InvalidPropertiesFormatException {
    	List<Note> retval = null;
    	switch (style) {
    	case HighLowPitches:
    		retval = generateSequenceUsingHighLowPitches(session);
    		break;
    	case PatternWithNotes:
    		retval = generateSequenceUsingPatternWithNotes(session);
    		break;
    	case MIDIFile:
    		throw new InvalidPropertiesFormatException("Unrecognized Trial Specification Style.");
    	default:
    		retval = generateSequenceUsingPatternWithNotes(session);
    	}
    	return retval;
    }

    /**
     * Generate a List of Notes using highPitch, lowPitch, playbackMeasures, beatsPerMeasure,
     * baseIOI, and silenceMultiplier.
     */
    private List<Note> generateSequenceUsingHighLowPitches(RhythmSession session) {
        List<Note> retval = new ArrayList<Note>();
        
        Pitch highPitch = session.getHighPitch();
        Pitch lowPitch = session.getLowPitch();
        
        int measures = session.getPlaybackMeasures();
        int bpm = session.getBeatsPerMeasure();
      
        for(int i = 0; i < measures; i++) {
            retval.add(new Note(highPitch, 64, getBaseIOI()));
            for(int j = 1; j < bpm; j++) {
                retval.add(new Note(lowPitch, 64, getBaseIOI()));
            }
        }
        
        retval.add(new Note(highPitch, 64, getBaseIOI()));
        int silence = (int)((session.getSilenceMultiplier() + getBaseIOIOffsetDegree()) * getBaseIOI());
        retval.add(new Note(silence));
        
        Pitch probe = highPitch.detune(getProbeDetuneOffset());
        retval.add(new Note(probe, 64, getBaseIOI()));
        
        LogContext.getLogger().fine("Sequence length (inc. lead-in silence): " + computeDuration(retval));
        
        return retval;
    }
    
    /**
     * Generates a List of Notes using Primary, Secondary, Tertiary, Silence, and Probe values of 
     * Pitch, Velocity, and Duration. Also uses baseIOI, parses from trialNotePattern.
     * @param session
     * @throws InvalidPropertiesFormatException
     */
    // New sequence generator to work with Trial specification as per Issue 2
    private List<Note> generateSequenceUsingPatternWithNotes(RhythmSession session) throws InvalidPropertiesFormatException {
    	List<Note> retval = new ArrayList<Note>();
    	
    	// Remove all whitespace and switch to lowercase
    	String trialPattern = session.getTrialNotePattern().replaceAll("\\s", "").toLowerCase();
    	
    	Pitch primaryPitch = session.getPrimaryPitch();
    	int primaryVelocity = session.getPrimaryVelocity();
    	int primaryDuration = Math.round(session.getPrimaryDuration() * getBaseIOI());
    	
    	Pitch secondaryPitch = session.getSecondaryPitch();
    	int secondaryVelocity = session.getSecondaryVelocity();
    	int secondaryDuration = Math.round(session.getSecondaryDuration() * getBaseIOI());
    	
    	Pitch tertiaryPitch = session.getTertiaryPitch();
    	int tertiaryVelocity = session.getTertiaryVelocity();
    	int tertiaryDuration = Math.round(session.getTertiaryDuration() * getBaseIOI());
    	
    	Pitch probePitch = session.getProbePitch().detune(getProbeDetuneOffset());
    	int probeVelocity = session.getProbeVelocity();
    	int probeDuration = Math.round(session.getProbeDuration() * getBaseIOI());
    	
    	int silenceDuration = Math.round(session.getSilenceDuration() * getBaseIOI());
    	
    	for (int i=0; i < trialPattern.length(); i++) {
    		char note = trialPattern.charAt(i);
    		// all characters are already lowercase
    		switch (note) {
    		case 'p':
    			retval.add(new Note(primaryPitch, primaryVelocity, primaryDuration));
    			break;
    		case 's':
    			retval.add(new Note(secondaryPitch, secondaryVelocity, secondaryDuration));
    			break;
    		case 't':
    			retval.add(new Note(tertiaryPitch, tertiaryVelocity, tertiaryDuration));
    			break;
    		case '_':
    			retval.add(new Note(silenceDuration));
    			break;
    		case '*':
    			retval.add(new Note(probePitch, probeVelocity, probeDuration));
    			break;
    		default:
    			LogContext.getLogger().severe("Property \"trialNotePattern\" contains invalid characters." 
    					+ "trialNotePattern: " + trialPattern);
    			throw new InvalidPropertiesFormatException("Property \"trialNotePattern\" contains invalid characters.");
    		}
    	}
    	LogContext.getLogger().fine("Sequence length (inc. lead-in silence): " + computeDuration(retval));
    	return retval;
    }
    
    /**
     * Add up the duration values in the list of notes.
     * @return total duration in milliseconds.
     */
    private int computeDuration(List<Note> notes) {
        int retval = 0;
        for(Note n : notes) {
            retval += n.getDuration();
        }
        return retval;
    }

    /**
     * Convenience method to determine if the user's "accuracy" response 
     * was correct.
     */
    @Override
    public boolean isResponseCorrect() {
        ConfidenceResponse response = getResponse();
        if(response != null) {
            if (RhythmResponseParameters.isProbeToneAccurate(response)) {
                return getBaseIOIOffsetDegree() == 0.0;
            }
            else {
                return getBaseIOIOffsetDegree() != 0.0;
            }
                
        }
        return false;
    }    
	
	@Override
	public String getDescription() {
		String format = "Trial %d:\n\tIOI: %d\n\tIOIoffset: %1.2f\n\tProbeDetuneOffset: %d\n\tTap: %b";
		return String.format(format, getTrialNumber(), getBaseIOI(), getBaseIOIOffsetDegree(),
				getProbeDetuneOffset(), isWithTap());
	}

    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.datamodel.Trial#toString()
     */
    public String toString() {
        return String.format("Trial %d [ioi=%d,IOIoffset=%1.2f,probeDetuneOffset=%d,tap=%b]", getTrialNumber(), 
        		getBaseIOI(), getBaseIOIOffsetDegree(), getProbeDetuneOffset(), isWithTap());
    }


}
