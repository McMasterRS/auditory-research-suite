package edu.mcmaster.maplelab.toj;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Level;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.AVBlock.AVBlockType;
import edu.mcmaster.maplelab.common.gui.BasicStep;
import edu.mcmaster.maplelab.common.gui.InvokeOnAWTQueueRunnable;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.PlayableListener;
import edu.mcmaster.maplelab.toj.animator.AnimationRenderer;
import edu.mcmaster.maplelab.toj.animator.AnimatorPanel;
import edu.mcmaster.maplelab.toj.datamodel.TOJBlock;
import edu.mcmaster.maplelab.toj.datamodel.TOJResponseParameters;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

public class TOJStimulusResponseScreen extends BasicStep {
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
	//private final boolean _isDemo;

	public TOJStimulusResponseScreen(StepManager steps, TOJSession session, boolean isWarmup) {
		super(true);
		setStepManager(steps);
		
		_session = session;
		_isWarmup = isWarmup;
		_renderer = new AnimationRenderer(_session.connectDots());
		
		setTitleText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialTitle : ConfigKeys.testScreenTrialTitle, 
                null));
        setInstructionText(_session.getString(
            isWarmup ? ConfigKeys.warmupScreenTrialText : ConfigKeys.testScreenTrialText, 
                null));
        
        _aniPanel = new AnimatorPanel(_renderer);
        
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
        
        _response = new ResponseInputs(new TOJResponseParameters(_session));
        bottom.add(_response);
        
        _response.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnabledState();
            }
        });
        
        LogContext.getLogger().fine("* animation file: " + _session.getDataFileName());
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
        private long _playbackStart;
        private int _playCount;
        private PlayableListener _endListener = new PlayableListener() {
			@Override
			public void playableEnded(EventObject e) {
				playbackEnded();
			}
        };
        
        public PlaybackRunnable(TOJTrial trial) {
            _trial = trial;
            
            TOJBlock block = currBlock();
            // how many items to play (and wait to end)
            _playCount = block.getType() == AVBlockType.AUDIO_VIDEO ? 2 : 1;
            _trial.printDescription();
            // TODO: remove this
            //_playCount = 1;
        }
         
        public void run() {
            try {
            	// TODO: start animation, add listener
            	//       start video (handle video?), add listener
            	if (_playCount > 1) _renderer.setTrial(_trial);
            	
            	Playable p = _trial.getPlayable();
            	// TODO: may need to perform conversion on this gain value:
            	p.setVolume(_session.getPlaybackGain());
            	p.addListener(_endListener);
            	_playbackStart = System.currentTimeMillis();
                LogContext.getLogger().fine("Playback started @ " + _playbackStart);
                if (_playCount > 1) {
                	_renderer.setStartTime(_playbackStart);
                }
            	_trial.getPlayable().play();
            }
            catch (Exception ex) {
                LogContext.getLogger().log(Level.SEVERE, "Playback error", ex);
            }
        }
        
        private synchronized void playbackEnded() {
        	// barrier to make sure all playback has ended
        	--_playCount;
        	if (_playCount > 0) return;
        	
            try {
            	long playbackStop = System.currentTimeMillis();
                LogContext.getLogger().fine(String.format(
                    "Playback ended @ %s: duration (approx) = %s milliseconds", 
                    playbackStop,  playbackStop - _playbackStart));
            }
            catch (Exception ex) {
                LogContext.getLogger().log(Level.SEVERE, "Media system error", ex);                
            }
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _statusText.setText(_session.getString(ConfigKeys.enterResponseText, null));
                    setEnabled(true);
                }
            });            
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
