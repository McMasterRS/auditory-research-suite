/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av;

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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.av.datamodel.AVBlockType;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.datamodel.AVTrialManager;
import edu.mcmaster.maplelab.av.media.VideoPanel;
import edu.mcmaster.maplelab.av.media.animation.AnimationPanel;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.BasicStep;
import edu.mcmaster.maplelab.common.gui.InvokeOnAWTQueueRunnable;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;

/**
 * Primary trial run screen for the AV experiments.
 */
public abstract class AVStimulusResponseScreen<R, T extends AVTrial<R>, L extends TrialLogger<T>, 
			S extends AVSession<? extends AVTrialManager<S, T>, T, L>> extends BasicStep {
	/**
	 * AVStimulusResponseScreen handles the user's response to AV stimulus.
     * @version   $Revision$
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    enum ConfigKeys {
        testScreenTrialTitle,
        testScreenTrialText, 
        warmupScreenTrialTitle, 
        warmupScreenTrialText, 
        duringTrialText, 
        enterResponseText,
        accuracyCorrectText, 
        accuracyIncorrectText, 
        resultsFormatText, 
        firstDelayText,
        trialDelayText,
        warmupDelayText,
    }
    
    private final ResponseInputs<R> _response;
    private final JLabel _statusText;
    private final JLabel _results;
    
    private AVTrialManager<S, T> _trialManager;
    private T _currTrial;
    private T _completedTrial;
    
    private int _completed = 0;
    private int _correct = 0;

    private final S _session;
	private final boolean _isWarmup;
	private final StimulusScheduler _scheduler;
	private final VideoPanel _vidPanel;
	private final ResponseKeyListener _keyListener;

	public AVStimulusResponseScreen(StepManager steps, S session, boolean isWarmup) {
		super(true);
		setStepManager(steps);
		
		_session = session;
		_isWarmup = isWarmup;
		_trialManager = _session.getTrialManager(_isWarmup);
		_scheduler = new StimulusScheduler();
		_scheduler.setAnimationFrameAdvance(_session.getAnimationFrameAdvance(), TimeUnit.MILLISECONDS);
		_scheduler.setAudioCallAhead(_session.getAudioCallAhead(), TimeUnit.MILLISECONDS);
		_scheduler.setRenderCallAhead(_session.getRenderCallAhead(), TimeUnit.MILLISECONDS);
		_vidPanel = new VideoPanel();
		_keyListener = new ResponseKeyListener();
		
		setTitleText(_session.getString(isWarmup ? 
				ConfigKeys.warmupScreenTrialTitle : ConfigKeys.testScreenTrialTitle, null));
        setInstructionText(_session.getString(isWarmup ? 
        		ConfigKeys.warmupScreenTrialText : ConfigKeys.testScreenTrialText, null));
        
        JPanel bottom = new JPanel(new MigLayout("insets 0, fill", "[]0px[]", "[]"));
        getContentPanel().add(bottom, BorderLayout.SOUTH);
        
        JPanel textItems = new JPanel(new MigLayout("insets 0, fill"));
        textItems.setBorder(BorderFactory.createTitledBorder("Status"));
        bottom.add(textItems, "sg one, grow");
        
        _statusText = new JLabel();
        textItems.add(_statusText);
        Font f = _statusText.getFont();
        _statusText.setFont(new Font(f.getName(), Font.PLAIN, f.getSize() + 4));
        _statusText.setVerticalAlignment(SwingConstants.CENTER);
        _statusText.setHorizontalAlignment(SwingConstants.CENTER);
        
        _results = new JLabel("<html>0 Correct");
        _results.setVerticalAlignment(SwingConstants.BOTTOM);
        _results.setHorizontalAlignment(SwingConstants.RIGHT);
        textItems.add(_results, "south");
        
        updateResultsText();
        
        _response = createResponseInputs(_session);
        bottom.add(_response, "sg one, grow");
        
        _response.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePrevNextState();
            }
        });
        
        // initialize w/ response controls disabled
        setEnabled(false);
    }
	
	/** 
	 * Get the response inputs panel.
	 */
	protected ResponseInputs<R> getResponseInputs() {
		return _response;
	}
	
	/**
	 * Create a response inputs panel specific to this experiment.
	 */
	public abstract ResponseInputs<R> createResponseInputs(S session);
	
	/**
	 * Update the response inputs as necessary for the next trial.
	 */
	public abstract void updateResponseInputs(T trial);
    
    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#onVisible()
     */
    @Override
    protected void onVisible() {
        incrementTrial();
        doTrial();
    }
    
    /**
     * Overridden to disable child controls.
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.gui.BasicStep#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled && _response.isResponseComplete());
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
    	setEnabled(false);
    	
    	recordResponse();
        if (_trialManager.hasNext()) {
            doTrial();
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
    
    private void incrementTrial() {
    	_completedTrial = _currTrial;
    	_currTrial = _trialManager.next();
    }
    
    /**
     * Convenience method to get the next trial
     * 
     * @return next trial or null if none.
     */
    private T currentTrial() {
    	return _currTrial;
    }
    
    /**
     * Convenience method to get the just-played trial.
     * 
     * @return finished trial or null if none.
     */
    private T completedTrial() {
        return _completedTrial;
    }
    
    private void doTrial() {
        _response.reset();
        
        T trial = currentTrial();
        if (trial == null) return;
        updateResponseInputs(trial);
        trial.setTimeStamp(new Date());
        
        String trialDesc = String.format("Metablock %d: Repetition %d: Block %d: %s", 
        		trial.getNumber(METABLOCK), trial.getNumber(REPETITION), 
        		trial.getNumber(BLOCK_IN_METABLOCK), trial.getDescription());
        
        String delim = _isWarmup ? "\n---- Warmup Trial ----\n-> " : "\n--------------------\n-> ";
        LogContext.getLogger().fine(delim + trialDesc);

        _session.execute(new PrepareRunnable(trial));
        _session.execute(new PlaybackRunnable());
    }
    
    private void recordResponse() {
    	T trial = completedTrial();
        if (trial == null) return;
        
        trial.setResponse(_response.getResponse());
        
        LogContext.getLogger().fine(
            String.format("-> %s" , trial.getResponse()));
        
        if (!_isWarmup) {
            try {
                _session.getTrialLogger().submit(trial);
            }
            catch (IOException e) {
                LogContext.getLogger().severe("Error saving trial: " + e);
            }
        }       
        
        _session.execute(new StatusUpdaterRunnable(trial));
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
        private final T _trial;

        public PrepareRunnable(T trial) {
            _trial = trial;
        }
        
        public void run() {
            try {
            	// next trial set on playback completion, but have to set
            	// here on first trial
            	if (completedTrial() == null) _scheduler.setStimulusSource(_trial);
            	
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
                    	// gather block type information
                    	T completed = completedTrial();
                    	T currTrial = currentTrial();
                    	AVBlockType prevType = completed != null ? completed.getType() : null;
                    	AVBlockType currType = currTrial.getType();

                		getContentPanel().remove(_vidPanel);
                    	
                    	if (currType != AVBlockType.VIDEO_ONLY) {
                    		AnimationPanel aniPanel = _scheduler.getAnimationPanel();
                        	if (prevType == null || prevType == AVBlockType.VIDEO_ONLY) {
                    			aniPanel.setSize(_session.getScreenWidth(), _session.getScreenHeight());
                            	getContentPanel().add(aniPanel, BorderLayout.CENTER);
                    		}
                    		
                    		if (currType.usesAnimation()) aniPanel.start();
                    	}
                    	else {
                    		if (prevType != AVBlockType.VIDEO_ONLY) {
                        		// XXX: removing and re-adding this every time causes 
                        		// white flash on screen!
                        		getContentPanel().remove(_scheduler.getAnimationPanel());
                        	}
                    		
                			getContentPanel().add(_vidPanel, BorderLayout.CENTER);
                    		_vidPanel.setMovie(_trial.getVideoPlayable());
                    	}
                    	
                        _statusText.setText(_session.getString(ConfigKeys.duringTrialText, ""));
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
        private AVStimulusListener _listener = new AVStimulusListener() {
			@Override
			public void stimuliComplete() {
				trialPlaybackDone();
			}
        };
        
        public PlaybackRunnable() {
            _scheduler.addStimulusListener(_listener);
        }
         
        public void run() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                	@Override
                	public void run() {
                		_scheduler.start();
                	}
                });
            }
            catch (Exception ex) {
                LogContext.getLogger().log(Level.SEVERE, "Playback error", ex);
            }
        }
        
        private synchronized void trialPlaybackDone() {
        	// stop processing items
        	_scheduler.stop();
        	_scheduler.removeStimulusListener(_listener);
        	_scheduler.getAnimationPanel().stop();
            
            // move to the next trial
        	incrementTrial();
        	_scheduler.setStimulusSource(currentTrial());
        	
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	// update UI
                    _statusText.setText(_session.getString(ConfigKeys.enterResponseText, null));
                    setEnabled(true);
                    _response.requestFocusInWindow();
                    _keyListener.setEnabled(true);
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
		            //_response.setSelection(((KeyEvent) event).getKeyChar());
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
        private final T _trial;

        public StatusUpdaterRunnable(T currTrial) {
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
}
