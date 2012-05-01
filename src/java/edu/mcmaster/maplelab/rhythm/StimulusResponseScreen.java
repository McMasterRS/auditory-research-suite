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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.sound.midi.*;
import javax.swing.*;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.AnswerConfidenceResponseInputs;
import edu.mcmaster.maplelab.common.gui.*;
import edu.mcmaster.maplelab.common.sound.MidiInterpreter;
import edu.mcmaster.maplelab.common.sound.Note;
import edu.mcmaster.maplelab.common.sound.Pitch;
import edu.mcmaster.maplelab.common.sound.ToneGenerator;
import edu.mcmaster.maplelab.rhythm.datamodel.*;


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
    private List<RhythmBlock> _blocks;
    private int _blockIndex = 0;
    private final boolean _isWarmup;
    private final JLabel _statusText;
    private TapRecorder _tapRecorder;
    private JComponent _tapTarget;
    private JLabel _results;
    
    private int _completed = 0;
    private int _correct = 0;

    public StimulusResponseScreen(StepManager steps, RhythmSession session, boolean isWarmup) {
        _isWarmup = isWarmup;
        setStepManager(steps);        
        _session = session;
        setTitleText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialTitle : ConfigKeys.testScreenTrialTitle, 
                null));
        setInstructionText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialText : ConfigKeys.testScreenTrialText, 
                null));
        
        JPanel bottom = new JPanel(new GridLayout(1, 0));
        getContentPanel().add(bottom, BorderLayout.SOUTH);
        
        JPanel textItems = new JPanel(new BorderLayout());
        textItems.setBorder(BorderFactory.createTitledBorder("Status"));
        bottom.add(textItems);
        
        _statusText = new JLabel();
        textItems.add(_statusText, BorderLayout.CENTER);
        Font f = _statusText.getFont();
        _statusText.setFont(new Font(f.getName(), Font.PLAIN, f.getSize() + 4));
        _statusText.setVerticalAlignment(SwingConstants.CENTER);
        _statusText.setHorizontalAlignment(SwingConstants.CENTER);
        
        _results = new JLabel("<html>0 Correct");
        _results.setVerticalAlignment(SwingConstants.BOTTOM);
        _results.setHorizontalAlignment(SwingConstants.RIGHT);
        textItems.add(_results, BorderLayout.SOUTH);
        
        updateResultsText();
        
        _response = new AnswerConfidenceResponseInputs(new RhythmResponseParameters(_session));
        bottom.add(_response);
        
        _response.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnabledState();
            }
        });
        
        try {
            if(!isWarmup) {
                _tapRecorder = new TapRecorder(_session);
                _tapRecorder.setMIDIInputID(session.getMIDIInputDeviceID());
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
                
                if(_session.isDebug()) {
                    _tapRecorder.setLogReceiver(new DebugTapForwarder());
                }
            }
        }
        catch (MidiUnavailableException ex) {
            LogContext.getLogger().log(Level.SEVERE, "Tap recorder error", ex);
        }
        
        LogContext.getLogger().fine("* baseIOIs: " + _session.getBaseIOIs());
        LogContext.getLogger().fine("* offsetDegrees: " + _session.getOffsetDegrees());
    }
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#onVisible()
     */
    @Override
    protected void onVisible() {
        initialize(_isWarmup);
        doNextTrial();
    }

    /**
     * Prepare screen to run tests.
     * 
     * @param isWarmup if this is a warmup run.
     */
    private void initialize(boolean isWarmup) {
        if (isWarmup) {
            _blocks = _session.generateWarmup();
        }
        else {
            _blocks = _session.generateBlocks();
            LogContext.getLogger().fine(_session.getCombinatorialDescription(_blocks));
        }
        
    }
    
    /**
     * Overridden to disable child controls.
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        updateEnabledState();
        _response.setEnabled(enabled);
    }
    
    /**
     * Update the "next" button to be enabled only when full response selected.
     *
     */
    private void updateEnabledState() {
        super.setEnabled(_response.isResponseComplete());
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
        if (!isDone()) {
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
    
    private RhythmBlock currBlock() {
        if(_blockIndex >= _blocks.size()) return null;
        
        return _blocks.get(_blockIndex);
    }
    
    /**
     * Convenience method to get the next trial
     * 
     * @return next trial or null if none.
     */
    private RhythmTrial currTrial() {
        RhythmBlock block = currBlock();
        return block != null ? block.currTrial() : null;
    }
    
    /**
     * Increment to the next block
     */
    private void incBlock() {
        _blockIndex++;       
        
        if (_blockIndex < _blocks.size()) {
            RhythmBlock block = currBlock();
            LogContext.getLogger().fine("* New block: " + block);
        }
        else if (!_isWarmup) {
        	_session.incrementRepetition();
        	if (_session.hasMoreRepetitions()) {
	    		_blockIndex = 0;
	        	_blocks = _session.generateBlocks();
	            LogContext.getLogger().fine(_session.getCombinatorialDescription(_blocks));
        	}
        }
    }
    
    private void doNextTrial() {
        _response.reset();
        
        RhythmBlock currBlock = currBlock();

        RhythmTrial currTrial = currTrial();
        if(currTrial == null) return;
        currTrial.setTimeStamp(new Date());
        
        String trialDesc = _isWarmup ? "\n---- Warmup Trial ----\n-> %s: %s: %s" : 
        		"\n--------------------\n-> %s: %s: %s";
        String rep = String.format("Repetition %d", _session.getCurrentRepetition());
        LogContext.getLogger().fine(String.format(trialDesc, 
        		rep, currBlock, currTrial.getDescription()));

        _session.execute(new PrepareRunnable(currTrial));
        _session.execute(new PlaybackRunnable(currTrial));
    }

    private boolean isDone() {
        return currTrial() == null;
    }
    
    private void recordResponse() {
    	RhythmBlock block = currBlock();
        RhythmTrial t = currTrial();
        if(t == null) return;
        
        t.setResponse(_response.getResponse());
        
        LogContext.getLogger().fine(
            String.format("---> response: %s" , t.getResponse()));
        
        if (!_isWarmup) {
            try {
                _session.getTrialLogger().submit(block, t);
            }
            catch (IOException e) {
                LogContext.getLogger().severe("Error saving trial: " + e);
            }
        }
        
        block.incTrial();
        if (block.isDone()) {
            incBlock();
        }        
        
        _session.execute(new StatusUpdaterRunnable(t));
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
                tg.setMidiBank((short)0, _session.getGMBank());
                tg.getSequencer().addMetaEventListener(_endListener);
                List<Note> seq = _trial.generateSequence(_session);
                _playbackStart = System.currentTimeMillis();
                LogContext.getLogger().fine("Playback started @ " + _playbackStart);
                _currSequence = tg.play(seq, _session.getPlaybackGain(), false);
                if (_tapRecorder != null) {
                	// this is not intuitive, but we still want the 
                	// tap recorder to handle computer taps
                	//_tapRecorder.enableUserInput(_trial.isWithTap());
                	_tapRecorder.setWithTap(_trial.isWithTap());
                    _tapRecorder.start(_currSequence);
                }
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
                LogContext.getLogger().fine(String.format(
                    "Playback ended @ %s: duration (approx) = %s milliseconds", 
                    playbackStop,  playbackStop - _playbackStart));
                ToneGenerator.getInstance().getSequencer().removeMetaEventListener(_endListener);
                if (_tapRecorder != null) {
                    _tapRecorder.stop();
                    
                    RhythmTrial trial = currTrial();
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
                    _response.requestFocusInWindow();
                }
            });            
        }
    }  
    
    private class StatusUpdaterRunnable implements Runnable {
        private final RhythmTrial _currTrial;

        public StatusUpdaterRunnable(RhythmTrial currTrial) {
            _currTrial = currTrial;
 
        }

        public void run() {
        	_completed++;
            
            final boolean wasCorrect = _currTrial.isResponseCorrect();

            if(wasCorrect) {
                _correct++;
            }
            
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
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
                	Thread.sleep(_session.getPreStimulusSilence());
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
