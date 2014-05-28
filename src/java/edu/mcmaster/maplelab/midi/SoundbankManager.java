package edu.mcmaster.maplelab.midi;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.mcmaster.maplelab.common.LogContext;

public class SoundbankManager {
    /**
     * Default Soundbank in case user supplied soundbank does not work.
     * See: http://www.schristiancollins.com/generaluser.php
     */
    private static final String DEFAULT_SOUNDBANK = "GeneralUser.1.44.sf2";
    
    /**
     * Get the default external soundbank.
     */
    public static Soundbank getDefaultSoundbank() {
    	Soundbank retval = null;
    	try {
    		// getResourceAsStream DOES NOT WORK!!!!
			retval = MidiSystem.getSoundbank(SoundbankManager.class.getResource(DEFAULT_SOUNDBANK));
		}
    	catch (InvalidMidiDataException e) {
			LogContext.getLogger().warning("Unable to load default soundbank.");
			e.printStackTrace();
		}
		catch (IOException e) {
			LogContext.getLogger().warning("Error reading default soundbank.");
			e.printStackTrace();
		}
		return retval;
    }
    
    public static Soundbank createSoundbank(String soundbankLocation, boolean showWarning) {
    	Soundbank retval = null;
    	retval = createSoundbank(soundbankLocation);
    	if (retval == null) {
    		if (showWarning) {
    			Runnable r = new Runnable() {
					@Override
					public void run() {
			    		// Could not get user specified soundbank file
			    		LogContext.getLogger().warning("User specified soundbank file is null or does not exist. " +
			        			"Using default internal soundbank file.");
			        	JOptionPane.showMessageDialog(null, "<html>No valid soundbank file given. " +
			        			"<br>Using default internal soundbank file: <br>" + DEFAULT_SOUNDBANK + "</html>",
			        			"Using Default Properties",
			        			JOptionPane.INFORMATION_MESSAGE);
					}
    			};
    			if (EventQueue.isDispatchThread()) {
    				r.run();
    			}
    			else {
    				try {
						SwingUtilities.invokeAndWait(r);
					} 
    				catch (InterruptedException e) {
						e.printStackTrace();
					} 
    				catch (InvocationTargetException e) {
						e.printStackTrace();
					}
    			}
    		}
    		retval = SoundbankManager.getDefaultSoundbank();
    	}
    	
    	return retval;
    }
    
    private static Soundbank createSoundbank(String soundbankLocation) {
    	Soundbank retval = null;
    	File f = new File(soundbankLocation);
    	if (f.isFile()) {
    		try {
    			retval = MidiSystem.getSoundbank(f);
    		}
        	catch (InvalidMidiDataException e) {
    			LogContext.getLogger().warning("Unable to load soundbank:" + soundbankLocation);
    			e.printStackTrace();
    		}
    		catch (IOException e) {
    			LogContext.getLogger().warning("Error reading file: " + soundbankLocation);
    			e.printStackTrace();
    		}
    	}
    	
		return retval;
    }
}
