/*
* Copyright (C) 2006 University of Virginia
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

import java.util.*;

import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.sound.Pitch;
import edu.mcmaster.maplelab.common.util.MathUtils;
import edu.mcmaster.maplelab.rhythm.RhythmExperiment;
import edu.mcmaster.maplelab.rhythm.RhythmTrialLogger;

/**
 * Context data for the experiment session.
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class RhythmSession extends Session<RhythmTrialManager, RhythmTrial, RhythmTrialLogger> {
	
    // NB: These are in camel case so that the "name()" matches 
    // properties file values.
    /**
     * @version   $Revision$
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    public enum ConfigKeys {
        trialSpecificationStyle,
        trialNotePattern,
        primaryPitch,
        primaryVelocity,
        primaryDuration,
        secondaryPitch,
        secondaryVelocity,
        secondaryDuration,
        tertiaryPitch,
        tertiaryVelocity,
        tertiaryDuration,
        probePitch,
        probeVelocity,
        probeDuration,
        silenceDuration,
        gmBank,
        baseIOIs,
        highPitch,
        lowPitch,
        offsetDegrees,
        randomizeAcrossRepetitions,
        playbackMeasures,
        beatsPerMeasure,
        silenceMultiplier,
        toneSynthID,
        tapInputDevID,
        tapSynthID,
        tapTestSound,
        computerKeyInput,
        recordNoteOff,
        suppressionWindow,
        subjectTapSound,
        subjectTapVelocity,
        recordActualTapVelocity,
        subjectTapNote,
        recordActualTapNote,
        subjectTapGain,
        subjectNoTapGain,
        subjectTapGM
    }
    
    /**
	 * For specifying the style of constructing trial rhythms.
     */	
    public enum TrialSpecStyle {
    	HighLowPitches,
    	PatternWithNotes,
    	MIDIFile
    }
    
    /**
     * Default ctor.
     * @param props Initial values
     */
    public RhythmSession(Properties props) {
        super(props);
    }
    
    public boolean randomizeAcrossRepetitions() {
    	return getBoolean(ConfigKeys.randomizeAcrossRepetitions, false);
    }
    
    @Override
    public String getExperimentBaseName() {
    	return RhythmExperiment.EXPERIMENT_BASENAME;
    }

    /**
     * Get the experiment identifier (database key.
     */
    @Override
    public int getExperimentDBKey() {
        return getSubject() * 10000 + getSession();
    }     
    
    /**
     * Flag to indicate that tones will be played in the
     * background during tap testing on the test panel.
     * 
     * XXX: not currently used - see RhythmExperiment.main for options for use
     */
    public boolean playTapTestSound() {
    	return getBoolean(ConfigKeys.tapTestSound, false);
    }
    
    /**
     * Flag to indicate if computer keyboard taps should be allowed.
     */
    public boolean allowComputerKeyInput() {
    	return getBoolean(ConfigKeys.computerKeyInput, true);
    }
    
    /**
     * Flag to indicate if feedback sounds for subject taps should be played.
     */
    public boolean playSubjectTaps() {
    	return getBoolean(ConfigKeys.subjectTapSound, false);
    }
    
    /**
     * Fractional gain [0.0, 1.0] for subject tap feedback notes.
     */
    public float subjectTapGain() {
    	return getClampedFloat(ConfigKeys.subjectTapGain, 1.0f, 0.0f, 1.0f);
    }
    
    /**
     * Fractional gain [0.0, 1.0] for subject tap feedback notes DURING NON-TAP TRIALS.
     */
    public float subjectNoTapGain() {
    	Float retval = getFloat(ConfigKeys.subjectNoTapGain, -1.0f);
    	if (retval < 0) return subjectTapGain();
    	else return MathUtils.clamp(retval, 0.0f, 1.0f);
    }
    
    /**
     * Velocity at which to play notes for subject tap feedback.
     * -1 indicates that the actual velocity should be used.
     */
    public int subjectTapVelocity() {
    	return getInteger(ConfigKeys.subjectTapVelocity, -1);
    }
    
    /**
     * Indicate if the actual tap velocity (if available) should be
     * recorded even if different from the velocity played.
     */
    public boolean recordActualTapVelocity() {
    	return getBoolean(ConfigKeys.recordActualTapVelocity, true);
    }
    
    /**
     * General midi bank to play for subject tap feedback.
     */
    public int subjectTapGM() {
    	return getInteger(ConfigKeys.subjectTapGM, 1);
    }
    
    /**
     * Note to play for subject tap feedback.
     */
    public int subjectTapNote() {
    	String pitch = getString(ConfigKeys.subjectTapNote, "C5");
        return Pitch.fromString(pitch).toMidiNoteNumber();
    }
    
    /**
     * Indicate if the actual tap note (if available) should be
     * recorded even if different from the note played.
     */
    public boolean recordActualTapNote() {
    	return getBoolean(ConfigKeys.recordActualTapNote, true);
    }
    
    /**
     * Flag to indicate if note-off events should be recorded.
     */
    public boolean recordNoteOffEvents() {
    	return getBoolean(ConfigKeys.recordNoteOff, false);
    }
    
    /**
     * Get the period of time (in milliseconds) within which
     * to ignore repeat note-on events.
     */
    public long getSuppressionWindow() {
    	return getLong(ConfigKeys.suppressionWindow, 0);
    }
    
    /**
	 * Get the Trial Specification Style, defaults to using {@link TrialSpecStyle.PatternWithNotes}.
	 */
    public TrialSpecStyle getTrialSpecificationStyle() {
    	String style = getString(ConfigKeys.trialSpecificationStyle, TrialSpecStyle.PatternWithNotes.name());
    	return TrialSpecStyle.valueOf(style);
    }
    
    /* -- Getters for TrialSpecStyle.HighLowPitches -- */
    
    /**
     * Get the upper (stressed) pitch.
     * (Used in HighLowPitches style specification)
     */
    public Pitch getHighPitch() {
        String pitch = getString(ConfigKeys.highPitch, "C5");
        return Pitch.fromString(pitch);
    }
    
    /**
     * get the lower (unstressed) pitch.
     * (Used in HighLowPitches style specification)
     */
    public Pitch getLowPitch() {
        String pitch = getString(ConfigKeys.lowPitch, "G4");
        return Pitch.fromString(pitch);
    }
    
    /**
     * Number of measures played (sounded)
     * (Used in HighLowPitches style specification)
     */
    public int getPlaybackMeasures() {
        return getInteger(ConfigKeys.playbackMeasures, 3);
    }
    
    /**
     * Number of beats per measures (first beat is the stressed one)
     * (Used in HighLowPitches style specification)
     */
    public int getBeatsPerMeasure() {
        return getInteger(ConfigKeys.beatsPerMeasure, 4);        
    }
    
    /**
     * Number of IOI units of silence after sounded measures (not including offset)
     * (Used in HighLowPitches style specification)
     */
    public int getSilenceMultiplier() {
        return getInteger(ConfigKeys.silenceMultiplier, 3);
    }
    
    /* -- Getters for TrialSpecStyle.PatternWithNotes -- */
    
    public String getTrialNotePattern() {
    	return getString(ConfigKeys.trialNotePattern, "");
    }
    
    /* Primary Note getters */
    
    public Pitch getPrimaryPitch() {
    	String pitch = getString(ConfigKeys.primaryPitch, "G4");
    	return Pitch.fromString(pitch);
    }
    
    public int getPrimaryVelocity() {
    	int velocity = getInteger(ConfigKeys.primaryVelocity, 64);
    	return MathUtils.clamp(velocity, 0, 127); // velocity must be in [0,127]
    }
    
    public float getPrimaryDuration() {
    	return getFloat(ConfigKeys.primaryDuration, 1.0f);
    }
    
    /* Secondary Note getters */
    
    public Pitch getSecondaryPitch() {
    	String pitch = getString(ConfigKeys.secondaryPitch, "G4");
    	return Pitch.fromString(pitch);
    }
    
    public int getSecondaryVelocity() {
    	int velocity = getInteger(ConfigKeys.secondaryVelocity, 64);
    	return MathUtils.clamp(velocity, 0, 127); // velocity must be in [0,127]
    }
    
    public float getSecondaryDuration() {
    	return getFloat(ConfigKeys.secondaryDuration, 1.0f);
    }
    
    /* Tertiary Note getters */
    
    public Pitch getTertiaryPitch() {
    	String pitch = getString(ConfigKeys.tertiaryPitch, "G4");
    	return Pitch.fromString(pitch);
    }
    
    public int getTertiaryVelocity() {
    	int velocity = getInteger(ConfigKeys.tertiaryVelocity, 64);
    	return MathUtils.clamp(velocity, 0, 127); // velocity must be in [0,127]
    }
    
    public float getTertiaryDuration() {
    	return getFloat(ConfigKeys.tertiaryDuration, 1.0f);
    }
    
    /* Probe Note getters */
    
    public Pitch getProbePitch() {
    	// Try defaulting probe pitch to primary pitch, if unspecified.
    	String pitch = getString(ConfigKeys.probePitch, getString(ConfigKeys.primaryPitch, "G4"));
    	return Pitch.fromString(pitch);
    }
    
    public int getProbeVelocity() {
    	// Try defaulting probe velocity to primary velocity, if unspecified.
    	int velocity = getInteger(ConfigKeys.probeVelocity, getPrimaryVelocity());
    	return MathUtils.clamp(velocity, 0, 127); // velocity must be in [0,127]
    }
    
    public float getProbeDuration() {
    	// Try defaulting probe duration to primary duration, if unspecified.
    	return getFloat(ConfigKeys.probeDuration, getFloat(ConfigKeys.primaryDuration, 1.0f));
    }
    
    /* Silence duration getter */
    
    public float getSilenceDuration() {
    	return getFloat(ConfigKeys.silenceDuration, 1.0f);
    }
    
    /**
     * Get the general midi bank number to select for playback.
     * 
     * @return midi bank number [1,128]
     */
    public short getGMBank() {
        String  gmBank = getString(ConfigKeys.gmBank, "13");
        return Short.parseShort(gmBank);
    }
    
    /**
     * List of integers specifying the inter-onset time between notes in milliseconds
     */
    public List<Integer> getBaseIOIs() {
        List<Integer> retval = getIntegerList(ConfigKeys.baseIOIs, new Integer[]{400});
        
        if(isSpeedMode()) {
            for(int i = 0, len = retval.size(); i < len; i++) {
                retval.set(i, retval.get(i)/10);
            }
        }
        
        return retval;
    }
    
    /**
     * List of floats representing percentage of baseIOI that probe tone should be offset.
     */
    public List<Float> getOffsetDegrees() {
        return getFloatList(ConfigKeys.offsetDegrees, new Float[]{0.1f});        
    }
    
    public int getSynthDevID() {
        return getInteger(ConfigKeys.toneSynthID, 0);
    }
    
    public void setSynthDevID(int id) {
        setProperty(ConfigKeys.toneSynthID, id);
    }
    
    public int getTapInputDevID() {
        return getInteger(ConfigKeys.tapInputDevID, 0);
    }
    
    public void setTapInputDevID(int id) {
        setProperty(ConfigKeys.tapInputDevID, id);
    }
    
    public int getTapSynthDevID() {
        return getInteger(ConfigKeys.tapSynthID, 0);
    }
    
    public void setTapSynthDevID(int id) {
        setProperty(ConfigKeys.tapSynthID, id);
    }
     
     /**
      * {@inheritDoc} 
      * @see edu.mcmaster.maplelab.common.datamodel.Session#toPropertiesString()
      */
     @Override
     public String toPropertiesString() {
         String retval = toPropertiesStringWithEnum(EnumSet.allOf(Session.ConfigKeys.class));
         retval += toPropertiesStringWithEnum(EnumSet.allOf(ConfigKeys.class));
         return retval;
     }

	@Override
	public DemoGUIPanel<?, RhythmTrial> getExperimentDemoPanel() {
		return null;
	}

	@Override
	protected RhythmTrialManager initializeTrialManager(boolean warmup) {
		return new RhythmTrialManager(this, warmup);
	}
}
