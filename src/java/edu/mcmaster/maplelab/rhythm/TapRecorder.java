/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/

package edu.mcmaster.maplelab.rhythm;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.logging.Level;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.sound.ToneGenerator;


/**
 * Traps AWT key events and generates midi key events.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Apr 10, 2007
 */
public class TapRecorder implements AWTEventListener, Receiver {
    private static final int MIDI_NOTE = 72;
    private static final int OPCODE_MASK = 0xf0;
    private static final int CHANNEL_MASK = 0x0f;
    private Sequencer _sequencer;
    private Sequence _sequence;
    private Track _track;
    private Receiver _receiver;
    private int _midiDevID = -1;
    private MidiDevice _midiInput;
    private boolean _allowCompKeyInput;
    private Boolean _lastKeyDown = null;
    private boolean _userInputOn;
    
    /** Items for suppression window implementation. */
    private Long _lastOnTick = null;
    private HashSet<MidiEvent> _skippedOnEvents = new HashSet<MidiEvent>();
    private long _suppressionWindow = 0;

    public TapRecorder(boolean allowComputerKeyInput, long suppressionWindow) throws MidiUnavailableException {
        _sequencer = ToneGenerator.getInstance().getSequencer();
        _allowCompKeyInput = allowComputerKeyInput;
        _suppressionWindow = suppressionWindow;
    }
    
    /**
     * Enable/disable user input (tapping on/off).
     */
    public void enableUserInput(boolean userInput) {
        _userInputOn = userInput;
    }
    
    /**
     * Set object to echo recording events to.
     */
    public void setReceiver(Receiver receiver) {
        _receiver = receiver;
    }

    /**
     * Set the MIDI device ID to attempt to open for recording.
     * 
     * @param midiDevID MIDI device ID/index, or -1 to disable MIDI system recording.
     */
    public void setMIDIInputID(int midiDevID) {
        _midiDevID = midiDevID;
    }
    
    public void start(Sequence sequence) {
        if (_allowCompKeyInput && _userInputOn) {
        	Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        }
        _sequence = sequence;
        
        _track = _sequence.createTrack();
        
        // Send a program change just so we have our own unique sound.
        try {
            ToneGenerator.prepareTrack(_track, (short)1, (short)0);
        }
        catch (InvalidMidiDataException e) {
        }
        _sequencer.recordEnable(_track, -1);
        
        if(_midiDevID >= 0 && _userInputOn) {
            try {
                Info[] devices = MidiSystem.getMidiDeviceInfo();
                if(_midiDevID >= devices.length) {
                    throw new ArrayIndexOutOfBoundsException(String.format(
                        "MIDI device index %d is outside bounds of devices list (length = %d)", _midiDevID, devices.length));
                }
                
                MidiDevice device = MidiSystem.getMidiDevice(devices[_midiDevID]);
                
                if (_midiInput != device) {
                	if (_midiInput != null) _midiInput.close();
                	
                	_midiInput = device;
                	
                	if (_midiInput != null) {
                		_midiInput.open();
                        // test == 0 only because -1 indicates unlimited:
                        if(_midiInput.getMaxTransmitters() == 0) {
                            throw new MidiUnavailableException(String.format(
                                "Specified device with ID/index=%d (%s) doesn't support transmitting.", 
                                _midiDevID, _midiInput.getDeviceInfo().getName()));
                        }
                        
                        _midiInput.getTransmitter().setReceiver(this);
                	}
                }
            }
            catch(Exception ex) {
                LogContext.getLogger().log(Level.SEVERE, "Couldn't initialize MIDI recording device", ex);
                if(_midiInput != null) {
                    _midiInput.close();
                    _midiInput = null;
                }
            }

        }
        
        _sequencer.startRecording();
    }
    
    /**
     * Get the midi device object for the given
     * midi device id.  
     */
    public static MidiDevice getMidiDevice(int deviceID) {
    	Info[] devices = MidiSystem.getMidiDeviceInfo();
    	try {
    		return MidiSystem.getMidiDevice(devices[deviceID]);
    	}
    	catch (Exception e) {
    		return null;
    	}
    }
    
    /**
     * Indicates if the MidiDevice with the given id has transmission capability,
     * which is necessary for recording.
     */
    public static boolean isValidTransmittingDevice(int deviceID) {
    	try {
    		MidiDevice device = getMidiDevice(deviceID);
    		return device.getMaxTransmitters() != 0;
    	}
    	catch (Exception e) {
    		return false;
    	}
    }
    
    public void stop() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        _sequencer.stopRecording();        
        _sequencer.recordDisable(_track);
        _track = null;
        _sequence = null;
        _lastOnTick = null;
        _skippedOnEvents.clear(); // in case stop preceded a corresponding note-off
        _lastKeyDown = null;
    }
    
    private MidiEvent convertNoteOffFormat(MidiEvent event) {
    	MidiEvent retval = event;
    	MidiMessage curr = event.getMessage();
		if (getOpcode(curr) == ShortMessage.NOTE_ON && getVelocity(curr) == 0) {
			ShortMessage sm = new ShortMessage();
			try {
				sm.setMessage(ShortMessage.NOTE_OFF, getChannel(curr), getKey(curr), getVelocity(curr));
				retval = new MidiEvent(sm, event.getTick());
			}
			catch (InvalidMidiDataException e) {} // don't convert
		}
		return retval;
    }
    
    // TODO: refactor the following static methods elsewhere?
    /**
     * Indicate if the given event is a note on event.
     */
    public static boolean isNoteOnEvent(MidiEvent event) {
    	MidiMessage m = event.getMessage();
    	return !(TapRecorder.getOpcode(m) != ShortMessage.NOTE_ON || 
    			TapRecorder.getVelocity(m) == 0);
    }
    
    /**
     * Indicate if the given event is a note off event.
     */
    public static boolean isNoteOffEvent(MidiEvent event) {
    	MidiMessage m = event.getMessage();
    	return (TapRecorder.getOpcode(m) == ShortMessage.NOTE_ON && 
    			TapRecorder.getVelocity(m) == 0) || TapRecorder.getOpcode(m) == ShortMessage.NOTE_OFF;
    }
    
    public static int getChannel(MidiMessage m) {
    	return m.getStatus() & CHANNEL_MASK;
    }
    
    public static int getOpcode(MidiMessage m) {
    	return m.getStatus() & OPCODE_MASK;
    }
    
    /**
     * Get the key property from the given message.  Returns null
     * if not present.
     */
    public static Integer getKey(MidiMessage message) {
    	return message.getLength() > 1 ? Integer.valueOf(message.getMessage()[1]) : null;
    }
    
    /**
     * Get the velocity property from the given message.  Returns null
     * if not present.
     */
    public static Integer getVelocity(MidiMessage message) {
    	return message.getLength() > 2 ? Integer.valueOf(message.getMessage()[2]) : null;
    }
    
    private void recordEvent(MidiEvent event) {
        if (_track == null || !_sequencer.isRecording()) return;
        
        long ticks = _sequencer.getTickPosition();

        if(ticks == 0) {
            ticks = ToneGenerator.toTicks(System.currentTimeMillis());
        }
        event.setTick(ticks);
        event = convertNoteOffFormat(event);
        
        long window = _lastOnTick != null ? ticks - _lastOnTick : Long.MAX_VALUE;
        
        // certain repeat note-on events are suppressed, and the
        // corresponding note-off events no longer make sense
        if (isNoteOnEvent(event)) {
        	// only record a note-on event if we are not in the 
        	// suppression window
        	if (window > _suppressionWindow) {
        		_track.add(event);
        		_lastOnTick = ticks;
        	}
        	else {
        		_skippedOnEvents.add(event);
        	}
        }      
        else if (isNoteOffEvent(event) && _skippedOnEvents.size() > 0) {
        	MidiMessage eventM = event.getMessage();
        	HashSet<MidiEvent> toRemove = new HashSet<MidiEvent>();
        	for (MidiEvent me : _skippedOnEvents) {
        		MidiMessage skippedM = me.getMessage();
        		if (getChannel(eventM) == getChannel(skippedM) &&
            			getKey(eventM) == getKey(skippedM)) {
            		toRemove.add(me);
    			}
        	}
        	
        	if (toRemove.size() > 0) {
        		// we matched the note-off event to the skipped note-on(s),
        		// so clear the cached event(s)
            	_skippedOnEvents.removeAll(toRemove);
        	}
        	else {
        		// if we didn't remove anything from the cache, this note-off is valid
        		_track.add(event);
        	}
        }
        else {
        	// note-off event w/ no skipped note-on events
        	_track.add(event);
        }
        
        // always send the event to the receiver (either debug or console)
        if(_receiver != null) {
            _receiver.send(event.getMessage(), event.getTick());
        }
    }
    
    private void recordEvent(boolean down) {
    	// prevent hold-down key events
    	if (_lastKeyDown == null || down != _lastKeyDown.booleanValue()) {
    		_lastKeyDown = down;
    		
    		try {
                MidiEvent event = down ? 
                    ToneGenerator.createNoteOnEvent(MIDI_NOTE, -1) :
                        ToneGenerator.createNoteOffEvent(MIDI_NOTE, -1);
                recordEvent(event);
            }
            catch (InvalidMidiDataException ex) {
                LogContext.getLogger().log(Level.SEVERE, "Error recording event", ex);
            }
    	}      
    }

    /**
     * Dispatcher for keyboard tapping events.
     * {@inheritDoc} 
     * @see java.awt.event.AWTEventListener#eventDispatched(java.awt.AWTEvent)
     */
    public void eventDispatched(AWTEvent event) {
    	switch(event.getID()) {
	        case KeyEvent.KEY_PRESSED:
	            recordEvent(true);
	            break;
	        case KeyEvent.KEY_RELEASED:
	            recordEvent(false);
	            break;
    	} 
    }
    
    /**
     * {@inheritDoc} 
     * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
     */
    public void send(MidiMessage message, long timeStamp) {
    	recordEvent(new MidiEvent(message, timeStamp));
    }    

    /**
     * {@inheritDoc} 
     * @see javax.sound.midi.Receiver#close()
     */
    public void close() {
        // Noop.
    }


}
