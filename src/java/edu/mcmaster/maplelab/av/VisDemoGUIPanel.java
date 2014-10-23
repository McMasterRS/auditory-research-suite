package edu.mcmaster.maplelab.av;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import edu.mcmaster.maplelab.av.datamodel.AVBlockType;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.media.MediaParams;
import edu.mcmaster.maplelab.av.media.MediaType;
import edu.mcmaster.maplelab.av.media.MediaType.MediaWrapper;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.VideoPanel;
import edu.mcmaster.maplelab.av.media.animation.AnimationPanel;
import edu.mcmaster.maplelab.av.media.animation.AnimationParser;
import edu.mcmaster.maplelab.av.media.animation.AnimationSequence;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Session;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.gui.FileBrowseField;

public abstract class VisDemoGUIPanel<T extends AVTrial<?>> extends DemoGUIPanel<AVSession<?, T, ?>, T> {
	
    private final FilePathUpdater _fUpdater = new FilePathUpdater();

	private final FileBrowseField _audFile;
	private final FileBrowseField _visFile;
	private final FileBrowseField _vidFile;
	
	private final JFormattedTextField _delayText;
	private final JFormattedTextField _frameAdvance;
	private final JFormattedTextField _renderCallAhead;
	private final JFormattedTextField _audioCallAhead;
	private final JSpinner _numPts;
	private final JCheckBox _connect;
	private final JCheckBox _forceReload;
	private final JCheckBox _loop;
	private final JCheckBox _useVideo;
	private final JButton _startButton;
	
	private StimulusScheduler _scheduler;
	private Boolean _video = null;

	private JFrame _testFrame;
	private AnimationPanel _aniPanel;
	private VideoPanel _vidPanel;
	
	private Map<MediaType<?>, Map<String, JComboBox> > _paramSelectors = 
			new HashMap<MediaType<?>, Map<String,JComboBox>>();
	
	//read data from user entries and create a trial
	
	public VisDemoGUIPanel(AVSession<?, T, ?> session) {
		super(session);
		setLayout(new MigLayout("", "[]30px[]30px[]30px[]30px[]30px[]30px[]", ""));

		// auditory info
		//add(new JLabel("Audio"), "split, span, gaptop 10");
		//add(new JSeparator(), "growx, wrap, gaptop 10");
		
        JPanel p = genParamControls(session, MediaType.AUDIO);
        //add(p, "spany 2, grow");
		
		
		//add(new JLabel("Delay (ms)", 2), "right, split 2");
		_delayText = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_delayText.setValue(new Long(0)); // new
		_delayText.setColumns(5);
		
		//add(_delayText, "left");
		
		//add(new JLabel("Audio call ahead (ms)", 2), "right, split 2");
		_audioCallAhead = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_audioCallAhead.setValue(new Long(0)); // new
		_audioCallAhead.setColumns(5);
		//add(_audioCallAhead, "left, wrap");
		
		// visual info
		add(new JLabel("Animation"), "newline, split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");
		
        p = genParamControls(session, MediaType.ANIMATION);
        //add(p, "spany 2, right");
		
		add (new JLabel("Number of dots"), "right, split 2");
		SpinnerModel model = new SpinnerNumberModel(6, 1, 20, 1);
		_numPts = new JSpinner(model);
		add(_numPts, "left");
		
		add(new JLabel("Render call ahead (ms)", 2), "right, split 2");
		_renderCallAhead = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_renderCallAhead.setValue(new Long(0)); // new
		_renderCallAhead.setColumns(5);
		add(_renderCallAhead, "left");

		add(new JLabel("Connect dots w/ lines"), "newline, right, split 2");
		_connect = new JCheckBox();
		add(_connect, "left");
		_connect.setSelected(true);
		
		add(new JLabel("Frame look ahead (ms)", 2), "right, split 2");
		_frameAdvance = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_frameAdvance.setValue(new Long(0)); // new
		_frameAdvance.setColumns(5);
		add(_frameAdvance, "left");
	        
		add(new JLabel("Loop"), "right, split 2");
		_loop = new JCheckBox();
		add(_loop, "right");
		
        // visual info
        //add(new JLabel("Video"), "newline, split, span, gaptop 10");
        //add(new JSeparator(), "growx, wrap, gaptop 10");
        
        //p = genParamControls(session, MediaType.VIDEO);
        //add(p, "newline, spany 2, right");

        //add(new JLabel("Video Enabled:"), "right, split, span");
        _useVideo = new JCheckBox();
		//_useVideo = null;
        //add(_useVideo, "left, grow, wrap");
		
		// files 
		add(new JLabel("Files"), "newline, split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");
		
		add(new JLabel("Force file reload (disable caching)"), 
				"left, split 2, gaptop 10, gapbottom 15");
		_forceReload = new JCheckBox();
		add(_forceReload, "left, gaptop 10, gapbottom 15, wrap");
		
		//add(new JLabel("Audio File"), "right, span, split");
		_audFile = new FileBrowseField(false);
		//_audFile = null;
		//add(_audFile, "growx, wrap");
		
		add(new JLabel("Visual File"),"right, span, split");
		_visFile = new FileBrowseField(false);
		add(_visFile, "growx, wrap");
		
		//add(new JLabel("Video File"), "right, span, split");
		_vidFile = new FileBrowseField(false);
		//_vidFile = null;
		//add(_vidFile, "growx, wrap");
		//_vidFile.setEnabled(false);
		
		p = new JPanel(new MigLayout("insets 0, fill"));
		_startButton = new JButton("Start");
		_startButton.addActionListener(new StartUpdater());
		_startButton.addActionListener(_fUpdater);
		p.add(_startButton, "center");
		add(p, "span, center, grow");
		
		_fUpdater.restore();
	}
	
	private JPanel genParamControls(AVSession<?, T, ?> session, MediaType<?> type) {
	    JPanel retval = new JPanel(new MigLayout("insets 0", "", "[][fill]"));
	    
	    Map<String, JComboBox> selectorMap = _paramSelectors.get(type);
	    if(selectorMap == null) {
	        selectorMap = new HashMap<String, JComboBox>();
	        _paramSelectors.put(type, selectorMap);
	    }
	    
        List<String> params = type.getParams(session);
        for(String param : params) {
            MediaParams vals = MediaParams.getAvailableValues(param);
            retval.add(new JLabel(vals.displayLabel()), "right");

            JComboBox options = new JComboBox(new MediaParamsModel(vals));
            options.addActionListener(_fUpdater);
            retval.add(options, "growx, wrap");
            
            selectorMap.put(param, options);
        }
        
        return retval;
    }

    protected abstract T createTrial(AVBlockType type, AnimationSequence animationSequence,
			MediaWrapper<Playable> media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots, Long mediaDelay);
	  
	@Override
	public synchronized T getTrial() {
		AVSession<?, ?, ?> session = getSession();
		final boolean vid = _useVideo.isSelected();
		boolean forceReload = _forceReload.isSelected();
		MediaWrapper<Playable> media = vid ? 
				MediaType.VIDEO.createDemoMedia(_vidFile.getFile(), session, forceReload) :
				MediaType.AUDIO.createDemoMedia(_audFile.getFile(), session, forceReload);
		try {
			AVBlockType type = vid ? AVBlockType.VIDEO_ONLY : AVBlockType.AUDIO_ANIMATION;
			AnimationSequence aniSeq = !vid ? AnimationParser.parseFile(
					_visFile.getFile(), session.getAnimationPointAspect(), forceReload) : null;
			Object val = _delayText.getValue();
			Long delay = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
			Long mediaDelay = session.getToneOnsetTime(media.getName());
			return createTrial(type, aniSeq, media, delay, 
					(Integer)_numPts.getValue(), session.getBaseAnimationPointSize(), 
					_connect.isSelected(), mediaDelay);
		}
		catch (FileNotFoundException ex) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					String msg = "%s file";
					if (vid) msg = String.format(msg, "Video");
					else {
						boolean vis = _visFile.getFile().exists();
						boolean aud = _audFile.getFile().exists();
						if (!aud & !vis) {
							msg = "Audio and animation files";
						}
						else {
							msg = String.format(msg, aud ? "Animation" : "Audio");
						}
					}
					
					JOptionPane.showMessageDialog(VisDemoGUIPanel.this, 
							msg + " could not be found.", 
							"Missing file(s)...", 
							JOptionPane.ERROR_MESSAGE);
					
				}
			};
			
			boolean ran = false;
			if (!SwingUtilities.isEventDispatchThread()) {
				try {
					SwingUtilities.invokeAndWait(r);
					ran = true;
				} 
				catch (Exception e) {}
			} 
			
			if (!ran) r.run();
		} 
		
		return null;
	}
	
	/**
	 * Prepare and display the demo display window.
	 */
	private void prepareNext(T trial) {
		if (_testFrame == null) {
			_testFrame = new JFrame();
			_testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Window w = getParentWindow();
            _testFrame.setLocation(w.getLocation().x + w.getWidth(), w.getLocation().y);

            _scheduler = new StimulusScheduler();
            _aniPanel = _scheduler.getAnimationPanel();
		}
		
    	if (trial.isVideo()) {
    	    if(_vidPanel == null) {
                _vidPanel = new VideoPanel();
    	    }
    		_vidPanel.setMovie(trial.getVideoPlayable());
    		if (_video == null || !_video) {
    			_testFrame.getContentPane().removeAll();
        		_testFrame.getContentPane().add(_vidPanel, BorderLayout.CENTER);
        		_testFrame.setTitle(_vidPanel.getClass().getSimpleName());
        		_aniPanel.stop();
    		}
    		_video = true;
    	}
    	else {
    		if (_video == null || _video) {
    			_testFrame.getContentPane().removeAll();
        		_testFrame.getContentPane().add(_aniPanel, BorderLayout.CENTER);
        		_testFrame.setTitle(_aniPanel.getClass().getSimpleName());
        		_aniPanel.start();
    		}
    		_video = false;
    	}

        _testFrame.pack();
        
        if (getSession().isOscilloscopeSensorMode()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension size = _testFrame.getSize();
            _testFrame.setLocation(screenSize.width - size.width, screenSize.height - size.height);
        }

        _testFrame.setVisible(true);
        
	}
	
	private void updateCallAheads() {
		Object val = _frameAdvance.getValue();
		Long longVal = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
		_scheduler.setAnimationFrameAdvance(longVal, TimeUnit.MILLISECONDS);
		val = _renderCallAhead.getValue();
		longVal = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
		_scheduler.setRenderCallAhead(longVal, TimeUnit.MILLISECONDS);
		val = _audioCallAhead.getValue();
		longVal = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
		_scheduler.setAudioCallAhead(longVal, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Class for updating file fields.
	 */
	private class FilePathUpdater implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			save();
		}
				
	    private Preferences prefs() {
	        Preferences prefs = Preferences.userNodeForPackage(getClass());
	        // Create a new node.
	        prefs = prefs.node("visualizer.setup");
	        return prefs; 
	    }
	
	    private void save() {
	        Preferences prefs = prefs();
	        prefs.put(Session.ConfigKeys.dataDir.name(), _visFile.getFile().getAbsolutePath());
	        
	        try {
	            prefs.flush();
	        }
	        catch (BackingStoreException e) {
	            e.printStackTrace();
	            LogContext.getLogger().log(Level.WARNING, "Couldn't save prefs.", e);
	        }
	    }
	
	    private void restore() {
	        Preferences prefs = prefs();
	        
	        //String home = System.getProperty("user.home");
	        //if(home == null) {
	        //    home = ".";
	        //}
	        
	        String path = prefs.get(Session.ConfigKeys.dataDir.name(),  null);
	        if(path != null) 
	            _visFile.setFile(new File(path));	        
	    }
	    
	}
	
	/**
	 * StartUpdater responds to the "start" button by triggering the trial sequence.
	 * @author Catherine Elder <cje@datamininglab.com>
	 *
	 */
	private class StartUpdater implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			_startButton.setEnabled(false);
			SwingUtilities.invokeLater(new PrepareAndRun());
		}
	}
	
	private class PrepareAndRun implements Runnable {
		@Override
		public void run() {
		    try {
		        T next = getTrial();
		        LogContext.getLogger().fine("\n--------------------\n-> " + next.getDescription());
		        prepareNext(next);
		        updateCallAheads();
		        _scheduler.setStimulusSource(next);
		        _scheduler.addStimulusListener(new LoopListener());
		        _scheduler.start();
		    }
		    catch (Exception e) {
                _startButton.setEnabled(true);
		    }
		}
	}
	
	private class LoopListener implements AVStimulusListener {
		@Override
		public void stimuliComplete() {
			_scheduler.stop();
			_scheduler.removeStimulusListener(LoopListener.this);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (_loop.isSelected()) {
						SwingUtilities.invokeLater(new PrepareAndRun());
					}
					else {
						_startButton.setEnabled(true);
					}
				}
			});
		}
	}
	
    /**
     * Adapter around MediaParams for use inside a JComboBox. 
     * 
     * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since Jan 24, 2012
     */
	
    public class MediaParamsModel extends DefaultComboBoxModel {

        public MediaParamsModel(MediaParams vals) {
            super(vals.getValues().toArray());
        }
    }	
}
