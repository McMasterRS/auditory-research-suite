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
import edu.mcmaster.maplelab.common.sound.MidiInterpreter;
import edu.mcmaster.maplelab.common.sound.ToneGenerator;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession;


/**
 * Traps AWT key events and generates midi key events.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Apr 10, 2007
 */
public class TapRecorder implements AWTEventListener, Receiver {
    private static final int ARTIFICIAL_MIDI_NOTE = 72;
    
    private static Receiver _feedbackReceiver;
    private RhythmSession _session = null;
    private Sequencer _sequencer;
    private Track _track;
    private Receiver _logReceiver;
    private int _midiDevID = -1;
    private MidiDevice _midiInput;
    private boolean _allowCompKeyInput;
    private Boolean _lastKeyDown = null;
    private boolean _userInputOn = true;
    
    /** Items for suppression window implementation. */
    private Long _lastOnTick = null;
    private HashSet<MidiEvent> _skippedOnEvents = new HashSet<MidiEvent>();
    private long _suppressionWindow = 0;
    
    public TapRecorder(RhythmSession session) throws MidiUnavailableException {
    	this(session.allowComputerKeyInput(), session.getSuppressionWindow());
    	_session = session;
    }

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
    public void setLogReceiver(Receiver receiver) {
        _logReceiver = receiver;
    }

    /**
     * Set the MIDI device ID to attempt to open for recording.
     * 
     * @param midiDevID MIDI device ID/index, or -1 to disable MIDI system recording.
     */
    public void setMIDIInputID(int midiDevID) {
    	// the previous input device must be closed, or
    	// it will continue to transmit midi signals to
    	// this TapRecorder
    	if (_midiDevID != midiDevID && _midiInput != null) {
    		_midiInput.close();
    		_midiInput = null;
    	}
        _midiDevID = midiDevID;
    }
    
    /**
     * Start recording by adding a track to the given sequence for recording and
     * connecting this Receiver to the selected input device. Also initializes 
     * resources for playing user feedback, if required.
     */
    public void start(Sequence sequence) {
        if (_allowCompKeyInput && _userInputOn) {
        	Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        }
        
        _track = _sequencer.getSequence().createTrack();
        
        // Send a program change just so we have our own unique sound.
        try {
        	float volumePct = _session != null ? _session.getPlaybackGain() : 1.0f;
            ToneGenerator.prepareTrack(_track, volumePct, (short)1, (short)0);
        }
        catch (InvalidMidiDataException e) {
        }
        _sequencer.recordEnable(_track, -1);
        
        Info info = null;
        if (_midiDevID >= 0 && _userInputOn) {
            try {
                Info[] devices = MidiSystem.getMidiDeviceInfo();
                if (_midiDevID >= devices.length) {
                    throw new ArrayIndexOutOfBoundsException(String.format(
                        "MIDI device index %d is outside bounds of devices list (length = %d)", 
                        _midiDevID, devices.length));
                }
                
                info = devices[_midiDevID];
                MidiDevice device = MidiSystem.getMidiDevice(info);
                
                if (_midiInput != device) {
                	_midiInput = device;
                	
                	if (_midiInput != null) {
                		_midiInput.open();
                        // test == 0 only because -1 indicates unlimited:
                        if (_midiInput.getMaxTransmitters() == 0) {
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
                if (_midiInput != null) {
                    _midiInput.close();
                    _midiInput = null;
                }
            }
        }
		
        // prepare to play user tap sounds
        if (_session == null || _session.playSubjectTaps()) {
        	try {
            	// XXX: This allocates resources on every call
    			_feedbackReceiver = MidiSystem.getReceiver();
    			if (_session != null) {
        			MidiEvent[] prep = ToneGenerator.initializationEvents(_session.subjectTapGain(), 
        					(short) _session.subjectTapGM(), (short) 0);
        			for (MidiEvent me : prep) {
        				_feedbackReceiver.send(me.getMessage(), me.getTick());
        			}
    			}
    		} 
            catch (Exception e) {
            	LogContext.getLogger().log(Level.SEVERE, "Couldn't initialize MIDI feedback device", e);
                if (_feedbackReceiver != null) {
                	_feedbackReceiver.close();
                	_feedbackReceiver = null;
                }
    		}
        }
        
        if (!_sequencer.isRecording()) _sequencer.startRecording();
        if (info != null) LogContext.getLogger().info("Recording with: " + info.getName() + 
        		", " + info.getDescription());
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
    
    /**
     * Stop recording, clear cached items, deallocate resources.
     */
    public void stop() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (_feedbackReceiver != null) _feedbackReceiver.close();
        _sequencer.stopRecording();        
        _sequencer.recordDisable(_track);
        _track = null;
        _lastOnTick = null;
        _skippedOnEvents.clear(); // in case stop preceded a corresponding note-off
        _lastKeyDown = null;
    }
    
    /**
     * If the given midi event is the shortcut note-off format (note-on w/ velocity=0),
     * convert it to a standard note-off event. Also set the note and velocity according
     * to those specified in the properties.
     */
    private MidiEvent convertEvent(MidiEvent event) {
    	MidiEvent retval = event;
    	MidiMessage curr = event.getMessage();
    	
    	int currOp = MidiInterpreter.getOpcode(curr);
    	int newOp = currOp == ShortMessage.NOTE_ON && MidiInterpreter.getVelocity(curr) == 0 ? 
    			ShortMessage.NOTE_OFF : currOp;
    	int key = MidiInterpreter.getKey(curr);
    	int vel = MidiInterpreter.getVelocity(curr);
    	if (_session != null && _session.playSubjectTaps()) {
    		key = _session.subjectTapNote();
    		int val = _session.subjectTapVelocity();
    		if (val > -1) vel = val;
    	}
    	ShortMessage sm = new ShortMessage();
		try {
			sm.setMessage(newOp, MidiInterpreter.getChannel(curr), key, vel);
			retval = new MidiEvent(sm, event.getTick());
		}
		catch (InvalidMidiDataException e) {} // don't convert
		
		return retval;
    }
    
    /**
     * Record the given midi event after adjusting ticks and filtering
     * for events within the suppression window. Also hands-off the event
     * to play user feedback sounds, if required.
     */
    private void recordEvent(MidiEvent event) {
    	if (_track == null || !_sequencer.isRecording()) return;
        
        event = convertEvent(event);
        long ticks = _sequencer.getTickPosition();
        event.setTick(ticks);
        
        long window = _lastOnTick != null ? ticks - _lastOnTick : Long.MAX_VALUE;
        
        // certain repeat note-on events are suppressed, and the
        // corresponding note-off events no longer make sense
        if (MidiInterpreter.isNoteOnEvent(event)) {
        	// only record a note-on event if we are not in the 
        	// suppression window
        	if (window > _suppressionWindow) {
        		finalizeEvent(event);
        		_lastOnTick = ticks;
        	}
        	else {
        		_skippedOnEvents.add(event);
        	}
        }      
        else if (MidiInterpreter.isNoteOffEvent(event) && _skippedOnEvents.size() > 0) {
        	MidiMessage eventM = event.getMessage();
        	HashSet<MidiEvent> toRemove = new HashSet<MidiEvent>();
        	for (MidiEvent me : _skippedOnEvents) {
        		MidiMessage skippedM = me.getMessage();
        		if (MidiInterpreter.getChannel(eventM) == MidiInterpreter.getChannel(skippedM) &&
            			MidiInterpreter.getKey(eventM) == MidiInterpreter.getKey(skippedM)) {
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
        		finalizeEvent(event);
        	}
        }
        else {
        	// note-off event w/ no skipped note-on events
        	finalizeEvent(event);
        }
        
        // always send the event to the receiver (either debug or console)
        if(_logReceiver != null) {
            _logReceiver.send(event.getMessage(), event.getTick());
        }
    }
    
    /**
     * Handle recording (to track) and feedback (playing) of the filtered event.
     */
    private void finalizeEvent(MidiEvent event) {
    	_track.add(event);
    	if (_feedbackReceiver != null) {
    		try {
    			_feedbackReceiver.send(event.getMessage(), System.currentTimeMillis()*1000);
    		}
    		catch (IllegalStateException ise) {
    			// result of timing issue between end-of-track and subject tap
    			LogContext.getLogger().fine("Subject tap sound cancelled because receiver closed.");
    		}
    	}
    }
    
    /**
     * Record a key up or down event as a midi event.
     */
    private void recordEvent(boolean down) {
    	// prevent hold-down key events
    	if (_lastKeyDown == null || down != _lastKeyDown.booleanValue()) {
    		_lastKeyDown = down;
    		
    		try {
                MidiEvent event = down ? 
                    ToneGenerator.createNoteOnEvent(ARTIFICIAL_MIDI_NOTE, -1) :
                        ToneGenerator.createNoteOffEvent(ARTIFICIAL_MIDI_NOTE, -1);
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
