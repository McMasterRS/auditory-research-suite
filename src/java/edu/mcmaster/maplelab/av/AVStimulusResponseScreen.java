/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av;

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
import edu.mcmaster.maplelab.av.animation.AnimationPanel;
import edu.mcmaster.maplelab.av.animation.AnimationRenderer;
import edu.mcmaster.maplelab.av.datamodel.AVBlock;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.datamodel.TrialPlaybackListener;
import edu.mcmaster.maplelab.av.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.av.media.QTVideoClip;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.TrialLogger;
import edu.mcmaster.maplelab.common.gui.BasicStep;
import edu.mcmaster.maplelab.common.gui.InvokeOnAWTQueueRunnable;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;

/**
 * Primary trial run screen for the AV experiments.
 */
public abstract class AVStimulusResponseScreen<R, B extends AVBlock<S, T>, T extends AVTrial<R>, 
			L extends TrialLogger<B, T>, S extends AVSession<B, T, L>> extends BasicStep {
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
    
    private ResponseInputs<R> _response;
    private List<B> _blocks;
    private int _blockIndex = 0;
    private final JLabel _statusText;
    private JLabel _results;
    
    private int _completed = 0;
    private int _correct = 0;

    private final S _session;
	private final boolean _isWarmup;
	private AnimationPanel _aniPanel;
	private final AnimationRenderer _renderer;
	private JPanel _vidPanel;
	private final ResponseKeyListener _keyListener;

	public AVStimulusResponseScreen(StepManager steps, S session, boolean isWarmup) {
		super(true);
		setStepManager(steps);
		
		_session = session;
		_isWarmup = isWarmup;
		_renderer = new AnimationRenderer();
		_keyListener = new ResponseKeyListener();
		
		setTitleText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialTitle : ConfigKeys.testScreenTrialTitle, 
                null));
        setInstructionText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialText : ConfigKeys.testScreenTrialText, 
                null));
        
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
            _blocks = new ArrayList<B>(1);
            _blocks.add(_session.generateWarmup());
        }
        else {
            _blocks = _session.generateBlocks();
            LogContext.getLogger().fine(_session.getCombinatorialDescription());
        }
        
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
    
    private B currBlock() {
        if(_blockIndex >= _blocks.size()) return null;
        
        return _blocks.get(_blockIndex);
    }
    
    /**
     * Convenience method to get the next trial
     * 
     * @return next trial or null if none.
     */
    private T currTrial() {
        B block = currBlock();
        return block != null ? block.currTrial() : null;
    }
    
    /**
     * Increment to the next block
     */
	private void incBlock() {
        _blockIndex++;       
        
        if (_blockIndex < _blocks.size()) {
            AVBlock<?, ?> block = currBlock();
            LogContext.getLogger().fine(String.format("> New block: %s: %d trials", 
            		block, block.getNumTrials()));
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
        
        B currBlock = currBlock();

        T currTrial = currTrial();
        if (currTrial == null) return;
        currTrial.setTimeStamp(new Date());
        
        LogContext.getLogger().fine(String.format("\n--------------------\n-> %s: %s", 
        		currBlock, currTrial.getDescription()));

        _session.execute(new PrepareRunnable(currTrial));
        _session.execute(new PlaybackRunnable(currTrial));
    }

    private boolean isDone() {
        return currTrial() == null;
    }
    
    private void recordResponse() {
    	B block = currBlock();
    	T t = currTrial();
        if(t == null) return;
        
        t.setResponse(_response.getResponse());
        
        LogContext.getLogger().fine(
            String.format("-> %s" , t.getResponse()));
        
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
        private final T _trial;

        public PrepareRunnable(T trial) {
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
            	
            	// actual delay until next trial
            	Thread.sleep(_session.getTrialDelay());
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    	B block = currBlock();

                    	if (block.getType() == AVBlockType.AUDIO_ANIMATION) {
                        	// create and add animation panel if not done
                        	if (_aniPanel == null) {
                                _aniPanel = new AnimationPanel(_renderer, 
                                		new Dimension(_session.getScreenWidth(), _session.getScreenHeight()));

                                getContentPanel().add(_aniPanel, BorderLayout.CENTER);
                        	}
                    		_aniPanel.start();
                    	}
                    	else if (_aniPanel != null) {
                    		getContentPanel().remove(_aniPanel);
                    		_aniPanel = null;
                    	}
                    	
                    	if (block.getType() == AVBlockType.VIDEO_ONLY) {
                    		// create and add video panel if not done
                    		if (_vidPanel == null) {
                    			_vidPanel = new JPanel();
                    		}

                			getContentPanel().remove(_vidPanel);
                			getContentPanel().add(_vidPanel, BorderLayout.CENTER);
                    		_vidPanel.removeAll();
                    		_vidPanel.add(QTVideoClip.makeComponent(_trial.getVideoPlayable()));
                    	}
                    	else if (_vidPanel != null) {
                    		getContentPanel().remove(_vidPanel);
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
        private final T _trial;
        private TrialPlaybackListener _listener = new TrialPlaybackListener() {
			@Override
			public void playbackEnded() {
				trialPlaybackDone();
			}
        };
        
        public PlaybackRunnable(T trial) {
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
                	if (_aniPanel != null) _aniPanel.stop();
                	_trial.removePlaybackListener(_listener);
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
        private final T _currTrial;

        public StatusUpdaterRunnable(T currTrial) {
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
