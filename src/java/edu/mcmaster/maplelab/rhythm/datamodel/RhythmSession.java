/*
* Copyright (C) 2006 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: RhythmSession.java 399 2008-01-11 22:20:55Z sfitch $
*/
package edu.mcmaster.maplelab.rhythm.datamodel;

import java.util.*;

import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.sound.Pitch;

/**
 * Context data for the experiment session.
 * 
 * 
 * @version $Revision: 399 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since May 10, 2006
 */
public class RhythmSession extends Session<RhythmBlock, RhythmTrial> {

    // NB: These are in camel case so that the "name()" matches 
    // properties file values.
    /**
     * @version   $Revision: 399 $
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    public enum ConfigKeys {
        highPitch,
        lowPitch,
        gmBank,
        baseIOIs,
        offsetDegrees,
        playbackMeasures,
        beatsPerMeasure,
        silenceMultiplier, 
        preStimulusSilence,
        speedMode,
        midiDevID
    }

    /**
     * Default ctor.
     * @param props Initial values
     */
    public RhythmSession(Properties props) {
        super(props);
    }
    
    /**
     * Get the experiment identifier (database key.
     */
    @Override
    public int getExperimentID() {
        return getSubject() * 10000 + getSession();
    }     
    
     /**
     * Flag to indicate dividing IOIs by 10 to speed through stimulus for
     * testing.
     */
    public boolean isSpeedMode() {
        boolean retval = false;
        Object val = getProperty(ConfigKeys.speedMode);
        if(val instanceof String) {
            retval = Boolean.parseBoolean((String)val);
        }
        return retval;
    }
    
    /**
     * Get the upper (stressed) pitch.
     */
    public Pitch getHighPitch() {
        String pitch = getString(ConfigKeys.highPitch, "C5");
        return Pitch.fromString(pitch);
    }
    
    /**
     * get the lower (unstressed) pitch.
     */
    public Pitch getLowPitch() {
        String pitch = getString(ConfigKeys.lowPitch, "G4");
        return Pitch.fromString(pitch);
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

    /**
     * Number of measures played (sounded)
     */
    public int getPlaybackMeasures() {
        return getInteger(ConfigKeys.playbackMeasures, 3);
    }
    
    /**
     * Number of beats per measures (first beat is the stressed one)
     */
    public int getBeatsPerMeasure() {
        return getInteger(ConfigKeys.beatsPerMeasure, 4);        
    }
    
    /**
     * Number of milliseconds to wait before playback of stimulus.
     */
    public int getPreStimulusSilence() {
        return getInteger(ConfigKeys.preStimulusSilence, 2000);
    }    
    
    /**
     * Number of IOI units of silence after sounded measures (not including offset)
     */
    public int getSilenceMultiplier() {
        return getInteger(ConfigKeys.silenceMultiplier, 3);
    }
    
    public int getMIDIInputDeviceID() {
        return getInteger(ConfigKeys.midiDevID, 0);
    }
    
    public void setMIDIInputDeviceID(int id) {
        setProperty(ConfigKeys.midiDevID, id);
    }
    
     /**
      * Generate the experiment blocks. The number of blocks is a multiple of 
      * the number of modulus widths.
      */
     public List<RhythmBlock> generateBlocks() {

         List<RhythmBlock> retval = new ArrayList<RhythmBlock>();
         
         List<Integer> baseIOIs = getBaseIOIs();
         
         int i = 1;
         for (Integer ioi : baseIOIs) {
             retval.add(new RhythmBlock(this, i++, true, ioi)); 
             retval.add(new RhythmBlock(this, i++, false, ioi)); 
        }
         
         
         return retval;
     }
     
     /**
      * Generate a warmup block.
      */
     public RhythmBlock generateWarmup() {
         RhythmBlock warmup = new RhythmBlock(this, 1, true, getBaseIOIs().get(0));
         warmup.clipTrials(getNumWarmupTrials());
         return warmup;
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




}
