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
    private Sequencer _sequencer;
    private Sequence _sequence;
    private Track _track;
    private Receiver _receiver;
    private int _midiDevID = -1;
    private MidiDevice _midiInput;

    public TapRecorder() throws MidiUnavailableException {
        _sequencer = ToneGenerator.getInstance().getSequencer();
    }
    
    /**
     * Set object to echo recording events ot.
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
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        _sequence = sequence;
        
        _track = _sequence.createTrack();
        
        // Send a program change just so we have our own unique sound.
        try {
            ToneGenerator.prepareTrack(_track, (short)1, (short)0);
        }
        catch (InvalidMidiDataException e) {
        }
        _sequencer.recordEnable(_track, -1);
        
        if(_midiInput == null && _midiDevID >= 0) {
            try {
                Info[] devices = MidiSystem.getMidiDeviceInfo();
                if(_midiDevID >= devices.length) {
                    throw new ArrayIndexOutOfBoundsException(String.format(
                        "MIDI device index %d is outside bounds of devices list (length = %d)", _midiDevID, devices.length));
                }
                
                _midiInput = MidiSystem.getMidiDevice(devices[_midiDevID]);
                _midiInput.open();
                if(_midiInput.getMaxTransmitters() <= 0) {
                    throw new MidiUnavailableException(String.format(
                        "Specified device with ID/index=%d (%s) doesn't support transmitting.", 
                        _midiDevID, _midiInput.getDeviceInfo().getName()));
                }
                
                _midiInput.getTransmitter().setReceiver(this);
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
    
    public void stop() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        _sequencer.stopRecording();        
        _sequencer.recordDisable(_track);
        _track = null;
        _sequence = null;
    }
    
    private void recordEvent(MidiEvent event) {
        if(_track == null || !_sequencer.isRecording()) return;
        
        long ticks = _sequencer.getTickPosition();

        if(ticks == 0) {
            ticks = ToneGenerator.toTicks(System.currentTimeMillis());
        }
        event.setTick(ticks);

        _track.add(event);
        if(_receiver != null) {
            _receiver.send(event.getMessage(), event.getTick());
        }        
    }
    
    private void recordEvent(boolean down) {
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
