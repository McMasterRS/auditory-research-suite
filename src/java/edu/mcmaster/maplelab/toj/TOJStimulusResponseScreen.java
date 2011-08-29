/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.toj;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.gui.BasicStep;
import edu.mcmaster.maplelab.common.gui.InvokeOnAWTQueueRunnable;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.toj.animator.AnimationRenderer;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;
import edu.mcmaster.maplelab.toj.animator.AnimatorPanel;
import edu.mcmaster.maplelab.toj.datamodel.TOJBlock;
import edu.mcmaster.maplelab.toj.datamodel.TOJResponseParameters;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrialPlaybackListener;

/**
 * Primary trial run screen for the TOJ experiment.
 */
public class TOJStimulusResponseScreen extends BasicStep {
	/**
	 * TOJStimulusResponseScreen handles the user's response to stimulus.
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
    
    private ResponseInputs _response;
    private List<TOJBlock> _blocks;
    private int _blockIndex = 0;
    private final JLabel _statusText;
    private JLabel _results;
    
    private int _completed = 0;
    private int _correct = 0;

    private final TOJSession _session;
	private final boolean _isWarmup;
	private final AnimatorPanel _aniPanel;
	private final AnimationRenderer _renderer;
	private final ResponseKeyListener _keyListener;

	public TOJStimulusResponseScreen(StepManager steps, TOJSession session, boolean isWarmup) {
		super(true);
		setStepManager(steps);
		
		_session = session;
		_isWarmup = isWarmup;
		_renderer = new AnimationRenderer(_session.connectDots());
		_keyListener = new ResponseKeyListener();
		
		setTitleText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialTitle : ConfigKeys.testScreenTrialTitle, 
                null));
        setInstructionText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialText : ConfigKeys.testScreenTrialText, 
                null));
        
        Dimension dim = new Dimension(_session.getScreenWidth(), _session.getScreenHeight());
        _aniPanel = new AnimatorPanel(_renderer, dim);
        
        JPanel bottom = new JPanel(new MigLayout("insets 0, fill", "[]0px[]", "[]"));
        getContentPanel().add(bottom, BorderLayout.SOUTH);
        
        JPanel textItems = new JPanel(new MigLayout("insets 0, fill"));
        textItems.setBorder(BorderFactory.createTitledBorder("Status"));
        bottom.add(textItems, "sgx, grow");
        
        _statusText = new JLabel() {
        	@Override
        	public void setText(String text) {
        		super.setText("<html><p width=\"330\"" + text);
        	}
        };
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
        
        class TOJResponseInputs extends ResponseInputs {
        	public TOJResponseInputs(TOJResponseParameters rp, boolean vertical) {
        		super(rp, vertical);
        		enableEvents(AWTEvent.KEY_EVENT_MASK);
        	}
        }
        
        _response = new TOJResponseInputs(new TOJResponseParameters(_session), false);
        bottom.add(_response, "sgx, grow");
        
        _response.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnabledState();
            }
        });
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
            _blocks = new ArrayList<TOJBlock>(1);
            _blocks.add(_session.generateWarmup());
        }
        else {
            _blocks = _session.generateBlocks();
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
    
    private TOJBlock currBlock() {
        if(_blockIndex >= _blocks.size()) return null;
        
        return _blocks.get(_blockIndex);
    }
    
    /**
     * Convenience method to get the next trial
     * 
     * @return next trial or null if none.
     */
    private TOJTrial currTrial() {
        TOJBlock block = currBlock();
        return block != null ? block.currTrial() : null;
    }
    
    /**
     * Increment to the next block
     */
    private void incBlock() {
        _blockIndex++;       
        
        if (_blockIndex < _blocks.size()) {
            TOJBlock block = currBlock();
            LogContext.getLogger().fine("* New block: " + block);
        }
        else if (!_isWarmup) {
        	_session.incrementRepetition();
        	if (_session.hasMoreRepetitions()) {
	    		_blockIndex = 0;
	        	_blocks = _session.generateBlocks();
        	}
        }
    }
    
    private void doNextTrial() {
        _response.reset();
        
        TOJBlock currBlock = currBlock();

        TOJTrial currTrial = currTrial();
        if (currTrial == null) return;
        currTrial.setTimeStamp(new Date());
        
        LogContext.getLogger().fine(String.format("-> %s: %s", currBlock, currTrial));

        _session.execute(new PrepareRunnable(currTrial));
        _session.execute(new PlaybackRunnable(currTrial));
    }

    private boolean isDone() {
        return currTrial() == null;
    }
    
    private void recordResponse() {
    	TOJBlock block = currBlock();
        TOJTrial t = currTrial();
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
        private final TOJTrial _trial;

        public PrepareRunnable(TOJTrial trial) {
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
                    }
                });
            	
            	AnimationSequence as = _trial.getAnimationSequence();
            	Playable p = _trial.getPlayable();
            	LogContext.getLogger().fine("Trial data:");
            	LogContext.getLogger().fine(String.format("--animation file: %s", 
            			as != null ? as.getSourceFileName() : "n/a"));
            	LogContext.getLogger().fine(String.format("--audio file: %s", 
            			p != null ? p.name() : "n/a"));
            	LogContext.getLogger().fine(String.format(
            			"--audio offset: %s", String.valueOf(_trial.getOffset())));
            	LogContext.getLogger().fine(String.format("--num animation points: %d", 
            			_trial.getNumPoints()));
            	
            	// actual delay until next trial
            	Thread.sleep(_session.getTrialDelay());
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    	TOJBlock block = currBlock();
                    	getContentPanel().remove(_aniPanel);
                    	if (block.getType() == AVBlockType.AUDIO_VIDEO) {
                    		getContentPanel().add(_aniPanel, BorderLayout.CENTER);
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
        private final TOJTrial _trial;
        private TOJTrialPlaybackListener _listener = new TOJTrialPlaybackListener() {
			@Override
			public void playbackEnded() {
				trialPlaybackDone();
			}
        };
        
        public PlaybackRunnable(TOJTrial trial) {
            _trial = trial;
            _trial.addPlaybackListener(_listener);
            _trial.preparePlayback(_session, _renderer);
        }
         
        public void run() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                	@Override
                	public void run() {
                		_trial.play();
                	}
                });
            }
            catch (Exception ex) {
                LogContext.getLogger().log(Level.SEVERE, "Playback error", ex);
            }
        }
        
        private synchronized void trialPlaybackDone() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	_trial.removePlaybackListener(_listener);
                    _statusText.setText(_session.getString(ConfigKeys.enterResponseText, null));
                    setEnabled(true);
                    boolean focusRequest = _response.requestFocusInWindow();
                    LogContext.getLogger().fine("Result from focus request: " + focusRequest);
                    Toolkit.getDefaultToolkit().addAWTEventListener(_keyListener, 
                    		AWTEvent.KEY_EVENT_MASK);
                }
            });            
        }
    }  
    
    /**
     * Listener for key events corresponding to response selections.s
     */
    private class ResponseKeyListener implements AWTEventListener {
		@Override
		public void eventDispatched(AWTEvent event) {
			switch(event.getID()) {
		        case KeyEvent.KEY_PRESSED:
		            int key = ((KeyEvent) event).getKeyCode();
		            if (key == KeyEvent.VK_D) {
		            	_response.setAnswerSelection(0);
		            }
		            else if (key == KeyEvent.VK_T) {
		            	_response.setAnswerSelection(1);
		            }
		            else if (key == KeyEvent.VK_5 || key == KeyEvent.VK_NUMPAD5) {
		            	_response.setConfidenceSelection(0);
		            }
		            else if (key == KeyEvent.VK_4 || key == KeyEvent.VK_NUMPAD4) {
		            	_response.setConfidenceSelection(1);
		            }
		            else if (key == KeyEvent.VK_3 || key == KeyEvent.VK_NUMPAD3) {
		            	_response.setConfidenceSelection(2);
		            }
		            else if (key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) {
		            	_response.setConfidenceSelection(3);
		            }
		            else if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
		            	_response.setConfidenceSelection(4);
		            }
		            updateEnabledState();
		            break;
		        case KeyEvent.KEY_RELEASED:
		            return; //nothing
			} 
			
		}
    }
    
    private class StatusUpdaterRunnable implements Runnable {
        private final TOJTrial _currTrial;

        public StatusUpdaterRunnable(TOJTrial currTrial) {
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
                    	Toolkit.getDefaultToolkit().removeAWTEventListener(_keyListener);
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
