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

package edu.mcmaster.maplelab.rhythm;

import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.RelativeTrialPosition.BLOCK_IN_METABLOCK;
import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.RelativeTrialPosition.REPETITION;
import static edu.mcmaster.maplelab.common.datamodel.TrialPositionHierarchy.TrialHierarchy.METABLOCK;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.AnswerConfidenceResponseInputs;
import edu.mcmaster.maplelab.common.gui.BasicStep;
import edu.mcmaster.maplelab.common.gui.InvokeOnAWTQueueRunnable;
import edu.mcmaster.maplelab.common.gui.StepManager;
import edu.mcmaster.maplelab.common.sound.MidiInterpreter;
import edu.mcmaster.maplelab.common.sound.Note;
import edu.mcmaster.maplelab.common.sound.Pitch;
import edu.mcmaster.maplelab.common.sound.ToneGenerator;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmResponseParameters;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmSession;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmTrial;
import edu.mcmaster.maplelab.rhythm.datamodel.RhythmTrialManager;


/**
 * Screen where the stimulus is generated and the user provides a response.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 8, 2006
 */
public class StimulusResponseScreen extends BasicStep {

    /**
     * @version   $Revision$
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    enum ConfigKeys {
        testScreenTrialTitle,
        testScreenTrialText, 
        warmupScreenTrialTitle, 
        warmupScreenTrialText, 
        withTapText,
        withoutTapText, 
        enterResponseText,
        accuracyCorrectText, 
        accuracyIncorrectText, 
        resultsFormatText, 
        firstDelayText,
        trialDelayText,
        warmupDelayText,
    }
    
    private final RhythmSession _session;
    private AnswerConfidenceResponseInputs _response;
    private final boolean _isWarmup;
    private final JLabel _statusText;
    private TapRecorder _tapRecorder;
    private JComponent _tapTarget;
    private JLabel _results;
    
    private RhythmTrialManager _trialManager;
    private RhythmTrial _currTrial;
    private int _completed = 0;
    private int _correct = 0;
    
	private final ResponseKeyListener _keyListener;

    public StimulusResponseScreen(StepManager steps, RhythmSession session, boolean isWarmup) {
        _isWarmup = isWarmup;
        setStepManager(steps);        
        _session = session;
        _trialManager = _session.getTrialManager(_isWarmup);
        setTitleText(_session.getString(
            _isWarmup ? ConfigKeys.warmupScreenTrialTitle : ConfigKeys.testScreenTrialTitle, 
                null));
        setInstructionText(_session.getString(
            _isWarmup ? ConfigKeys.warmupScreenTrialText : ConfigKeys.testScreenTrialText, 
                null));
		_keyListener = new ResponseKeyListener();
        
        /* Status/Response setup */
        JPanel bottom = new JPanel(new MigLayout("insets 0, fill"));
        getContentPanel().add(bottom, BorderLayout.SOUTH);
        
        JPanel statusItems = new JPanel(new MigLayout("insets 0, fill"));
        statusItems.setBorder(BorderFactory.createTitledBorder("Status"));
        
        _statusText = new JLabel();
        statusItems.add(_statusText, BorderLayout.CENTER);
        Font f = _statusText.getFont();
        _statusText.setFont(new Font(f.getName(), Font.PLAIN, f.getSize() + 4));
        _statusText.setVerticalAlignment(SwingConstants.CENTER);
        _statusText.setHorizontalAlignment(SwingConstants.CENTER);
        
        _results = new JLabel("<html>0 Correct");
        _results.setVerticalAlignment(SwingConstants.BOTTOM);
        _results.setHorizontalAlignment(SwingConstants.RIGHT);
        statusItems.add(_results, BorderLayout.SOUTH);
        
        updateResultsText();
        
        _response = new AnswerConfidenceResponseInputs(new RhythmResponseParameters(_session));
        
        switch (_session.getStatusOrientation()) {
        case verticalTop:
        	bottom.add(statusItems, "north");
            bottom.add(_response, "south");
        	break;
        case verticalBottom:
        	bottom.add(statusItems, "south");
            bottom.add(_response, "north");
        	break;
        case suppressed:
        	// StatusItems set to invisible, and hidden such that they do not participate in the layout
        	statusItems.setVisible(false);
        	bottom.add(statusItems, "hidemode 3"); 
            bottom.add(_response, "grow");
        	break;
        case horizontalRight:
        	bottom.add(statusItems, "east, w 50%"); // width percentage constraints to keep status box from 
            bottom.add(_response, "west, w 50%");	// jumping around as the text changes.  
        	break;
        case horizontalLeft:
        default:
        	bottom.add(statusItems, "west, w 50%"); // width percentage constraints to keep status box from
            bottom.add(_response, "east, w 50%");	// jumping around as the text changes.
        }
        
        _response.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePrevNextState();
            }
        });
        /* End Status/Response setup */
        
        LogContext.getLogger().fine("Loading soundbank file for "
        		+ (_isWarmup ? "warmup." : "trials."));
        Soundbank sb = _session.getSoundbank();
        try {
        	_tapRecorder = new TapRecorder(_session);
        	_tapRecorder.setSoundbank(sb);
        	_tapRecorder.setMIDIInputID(_session.getTapInputDevID());
        	_tapRecorder.setMIDISynthID(_session.getTapSynthDevID());
        	
        	// Must set Soundbank prior so ToneGenerator will use it to setup devices.
        	ToneGenerator.getInstance().setSoundbank(sb);
        	ToneGenerator.getInstance().setMIDISynthID(_session.getSynthDevID());
        	
        	// We have to add a key listener to make sure this gets
        	// registered to receive events.
        	class TapTarget extends JComponent {
        		public TapTarget() {
        			if (_session.allowComputerKeyInput()) {
        				enableEvents(AWTEvent.KEY_EVENT_MASK);
        			}
        		}
        	}

        	_tapTarget = new TapTarget();

        	getContentPanel().add(_tapTarget, BorderLayout.CENTER);

        	if (_session.isDebug()) {
        		_tapRecorder.setLogReceiver(new DebugTapForwarder());
            }
        }
        catch (MidiUnavailableException ex) {
            LogContext.getLogger().log(Level.SEVERE, "Tap recorder error", ex);
        }
        
        LogContext.getLogger().fine("* baseIOIs: " + _session.getBaseIOIs());
        LogContext.getLogger().fine("* offsetDegrees: " + _session.getBaseIOIoffsetDegrees());
        LogContext.getLogger().fine("* probeDetuneAmounts: " + _session.getProbeDetuneAmounts());
    }
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#onVisible()
     */
    @Override
    protected void onVisible() {
        incrementTrial();
        doNextTrial();
    }
    
    /**
     * Overridden to disable child controls.
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        updatePrevNextState();
        _response.setEnabled(enabled);
    }
    
    /**
     * Update the "next" button to be enabled only when full response selected.
     */
    private void updatePrevNextState() {
        getPrevNextButtons().setEnabled(_response.isResponseComplete());
    }
    
    /** 
     * Called when the Next button is pressed.
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#doNext()
     */
    @Override
    protected void doNext() {
    	// block user input and allow response collection code to re-enable
    	getPrevNextButtons().setEnabled(false);
    	
    	recordResponse();
        if (_trialManager.hasNext()) {
            incrementTrial(); 
            doNextTrial();
        }
        else {      
            // Hack that needs to be cleaned up with better abstraction.
            // If this was the last trial, we need to include some time
            // for StatusUpdaterRunnable to finish.
            _session.execute(new InvokeOnAWTQueueRunnable(new Runnable() {
                public void run() {
                    getStepManager().next();
                }
            }, false));
        }
    }
    
    /**
     * Convenience method to get the next trial
     * 
     * @return next trial or null if none.
     */
    private RhythmTrial currentTrial() {
        return _currTrial;
    }
    
    private void doNextTrial() {
    	_response.reset();
        
        RhythmTrial trial = currentTrial();
        if (trial == null) return;
        trial.setTimeStamp(new Date());
        
        String trialDesc = String.format("Metablock %d: Repetition %d: Block %d: %s", 
        		trial.getNumber(METABLOCK), trial.getNumber(REPETITION), 
        		trial.getNumber(BLOCK_IN_METABLOCK), trial.getDescription());
        
        String delim = _isWarmup ? "\n---- Warmup Trial ----\n-> " : "\n--------------------\n-> ";
        LogContext.getLogger().fine(delim + String.format(trialDesc, trial.getDescription()));

        _session.execute(new PrepareRunnable(trial));
        _session.execute(new PlaybackRunnable(trial));
    }
    
    private void recordResponse() {
        RhythmTrial t = currentTrial();
        if(t == null) return;
        
        t.setResponse(_response.getResponse());
        
        LogContext.getLogger().fine(
            String.format("---> response: %s" , t.getResponse()));
        
        //if (!_isWarmup) {
            try {
                _session.getTrialLogger().submit(t);
            }
            catch (IOException e) {
                LogContext.getLogger().severe("Error saving trial: " + e);
            }
        //}  
        
        _session.execute(new StatusUpdaterRunnable(t));
    }
    
    private void incrementTrial() {
    	_currTrial = _trialManager.next();
    }
    
    /**
     * Mark step as being one way.
     * 
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#canGoBack()
     */
    @Override
    protected boolean canGoBack() {
        return false;
    }    
    
    private void updateResultsText() {
    	if (_session.allowResponseFeedback()) {
    		int percent = _completed == 0 ? 100 :
                (int)Math.round(_correct/(float)_completed*100);
            
            _results.setText(String.format(
                _session.getString(ConfigKeys.resultsFormatText, "??"),
                _correct, _completed, percent));
    	}
    	else {
    		_results.setText("");
    	}
    }
    
    /** Task to update screen and prepare user for running trial. */
    private class PrepareRunnable implements Runnable {
        private final RhythmTrial _trial;

        public PrepareRunnable(RhythmTrial trial) {
            _trial = trial;
        }
        
        public void run() {
            try {
            	SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        setEnabled(false);
                        // Give user some time until next trial begins, setting an
                        // appropriate message
                    	if (_isWarmup) {
                    		_statusText.setText(_session.getString(ConfigKeys.warmupDelayText, null));
                    	}
                    	else if (_completed == 0) {
                    		_statusText.setText(_session.getString(ConfigKeys.firstDelayText, null));
                    	}
                    	else {
                    		_statusText.setText(_session.getString(ConfigKeys.trialDelayText, null));
                    	}
                    	// necessary for allowing shutdown key sequence
                    	getParent().requestFocus();
                    }
                });
            	
            	// actual delay until next trial
            	Thread.sleep(_session.getTrialDelay());
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        _statusText.setText(_session.getString(_trial.isWithTap() ? 
                            ConfigKeys.withTapText : ConfigKeys.withoutTapText, null));
                        
                        if(_tapTarget != null) {
                        	_tapTarget.requestFocusInWindow();
                        }
                    }
                });
                
                // Give user time to prepare for playback.
                Thread.sleep(_session.getPreStimulusSilence());
            }
            catch (InterruptedException ex) {
                // Ignore
            }
            catch (InvocationTargetException ex) {
                LogContext.getLogger().log(Level.SEVERE, "Widget update error", ex);
            }
        }
    }
    
    /**
     * @version   $Revision$
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    private class PlaybackRunnable implements Runnable {
        private final RhythmTrial _trial;
        private MetaEventListener _endListener = new MetaEventListener() {
            private static final int MIDI_TRACK_END = 47;
            public void meta(MetaMessage meta) {
                // Check for end of track message.
                if (meta.getType() == MIDI_TRACK_END) {
                    playbackEnded();
                }
            }
        };
        private Sequence _currSequence;
        private long _playbackStart;
        
        public PlaybackRunnable(RhythmTrial trial) {
            _trial = trial;
        }
         
        public void run() {
            try {
                ToneGenerator tg = ToneGenerator.getInstance();
                tg.setPercussionChannelOn(_session.getUsePercussionChannel());
                tg.setMidiBank((short)0, _session.getInstrumentNumber());
                tg.getSequencer().addMetaEventListener(_endListener);
                
                Pitch.setMiddleCOctave(_session.getMiddleCOctave()); 
                // Important to set Middle C for Pitches before Notes are generated
                List<Note> seq = _trial.generateSequence(_session, _session.getTrialSpecificationStyle());
                
                // Use to time initialization time for tg.initializeSequenceToPlay and 
                // _tapRecorder.initializeSequencerForRecording
                //long loadingStart = System.currentTimeMillis();
                
                _currSequence = tg.initializeSequenceToPlay(seq, _session.getPlaybackGain());
                if (_tapRecorder != null) {
                	// this is not intuitive, but we still want the 
                	// tap recorder to handle computer taps
                	_tapRecorder.setWithTap(_trial.isWithTap());
                	// Must be called to initialize resources for tapping/recording
                    _tapRecorder.initializeSequencerForRecording();
                }
                _playbackStart = System.currentTimeMillis();
                
                // Use for init timing
                //LogContext.getLogger().finest("Sequence loading time: " + (_playbackStart - loadingStart));
                
                LogContext.getLogger().fine("Playback started @ " + _playbackStart);
                
                tg.startSequencerPlayback(false);
            }
            catch (MidiUnavailableException ex) {
                LogContext.getLogger().log(Level.SEVERE, "MIDI error", ex);
                
                JOptionPane.showMessageDialog(StimulusResponseScreen.this, 
                    "Some other program is currently using the MIDI playback device.\n"+
                    "Try closing all other applications and restarting this program.",
                    "MIDI Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (Exception ex) {
                LogContext.getLogger().log(Level.SEVERE, "Playback error", ex);
            }
        }
        
        private void playbackEnded() {
            try {
                long playbackStop = System.currentTimeMillis();
                // Subtracts off timing offset to only account for the time that actual notes are played. 
                LogContext.getLogger().fine(String.format(
                    "Playback ended @ %s: duration (approx) = %s milliseconds", playbackStop,  
                    (playbackStop - _playbackStart) - ToneGenerator.INITIALIZATION_TIMING_OFFSET));
                
                ToneGenerator.getInstance().getSequencer().removeMetaEventListener(_endListener);
                if (_tapRecorder != null) {
                    _tapRecorder.stop();
                    
                    RhythmTrial trial = currentTrial();
                    trial.setRecording(_currSequence);
                    
                    _currSequence = null;
                }
            }
            catch (MidiUnavailableException ex) {
                LogContext.getLogger().log(Level.SEVERE, "MIDI system error", ex);                
            }
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _statusText.setText(_session.getString(ConfigKeys.enterResponseText, null));
                    setEnabled(true);
                    _keyListener.setEnabled(true);
                    _response.requestFocusInWindow();
                }
            });            
        }
    }  
    
    /**
     * Listener for key events corresponding to response selections.
     */
    private class ResponseKeyListener implements AWTEventListener {
    	private boolean _enabled = false;
		@Override
		public void eventDispatched(AWTEvent event) {
			switch(event.getID()) {
		        case KeyEvent.KEY_PRESSED:
		        	_response.setSelection((KeyEvent) event);
		            updatePrevNextState();
		            break;
		        case KeyEvent.KEY_RELEASED:
		            return; //nothing
			} 
		}
		
		public void setEnabled(boolean enabled) {
			// avoid adding this twice
			if (enabled == _enabled) return;
			
			if (!enabled) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			}
			else {
				Toolkit.getDefaultToolkit().addAWTEventListener(this, 
                		AWTEvent.KEY_EVENT_MASK);
			}
			_enabled = enabled;
		}
    }
    
    private class StatusUpdaterRunnable implements Runnable {
        private final RhythmTrial _trial;

        public StatusUpdaterRunnable(RhythmTrial currTrial) {
            _trial = currTrial;
        }

        public void run() {
        	_completed++;
            
            final boolean wasCorrect = _trial.isResponseCorrect();

            if(wasCorrect) {
                _correct++;
            }
            
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    	_keyListener.setEnabled(false);
                    	if (_session.allowResponseFeedback()) {
                    		_statusText.setText(_session.getString(wasCorrect ?
                                    ConfigKeys.accuracyCorrectText : ConfigKeys.accuracyIncorrectText, null)); 
                    	}
                    	else {
                    		_statusText.setText("");
                    	}
                    	updateResultsText();
                    }
                });
                
                // Give user time to read results, if necessary.
                if (_session.allowResponseFeedback()) {
                	Thread.sleep(_session.getPostStimulusSilence());
                }
            }
            catch (InterruptedException ex) {
                // ignore
            }
            catch (InvocationTargetException ex) {
                LogContext.getLogger().log(Level.SEVERE, "Status update error", ex);       
            }  
        }
    }
    
    /** 
     * Forward tap events to debug console.
     */
    public class DebugTapForwarder implements Receiver {
        public void close() {
        }

        public void send(MidiMessage midimessage, long l) {
            if (midimessage instanceof ShortMessage) {
            	ShortMessage sm = (ShortMessage) midimessage;
            	int key = MidiInterpreter.getKey(sm);
            	Pitch pitch = new Pitch(key);
            	int vel = MidiInterpreter.getVelocity(sm);
            	String format = "%8d -> %s, midi note %s, velocity %s, %s";
                if (sm.getCommand() == ShortMessage.NOTE_ON) {
                	LogContext.getLogger().fine(String.format(format, l, "tap", key, vel, pitch));
                    
                }
                else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
                	LogContext.getLogger().fine(String.format(format, l, "release", key, vel, pitch));
                }
            }
        }
    }    

}
