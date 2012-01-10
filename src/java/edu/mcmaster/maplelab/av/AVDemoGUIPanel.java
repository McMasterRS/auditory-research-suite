package edu.mcmaster.maplelab.av;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;

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
import edu.mcmaster.maplelab.av.animation.AnimationPanel;
import edu.mcmaster.maplelab.av.animation.AnimationParser;
import edu.mcmaster.maplelab.av.animation.AnimationRenderer;
import edu.mcmaster.maplelab.av.animation.AnimationSequence;
import edu.mcmaster.maplelab.av.datamodel.AVSession;
import edu.mcmaster.maplelab.av.datamodel.AVTrial;
import edu.mcmaster.maplelab.av.datamodel.TrialPlaybackListener;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.av.media.PlayableMedia.MediaType;
import edu.mcmaster.maplelab.av.media.VideoPanel;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.datamodel.EnvelopeDuration;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.gui.FileBrowseField;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

public abstract class AVDemoGUIPanel<T extends AVTrial<?>> extends DemoGUIPanel<AVSession<?, T, ?>, T> {
	private FilePathUpdater _fUpdater = new FilePathUpdater();
	private JComboBox _pitches;
	private JComboBox _vDurations;
	private JComboBox _aDurations;
	private JComboBox _frequency;
	private JComboBox _spectrum;
	private JComboBox _envDuration;
	
	private FileBrowseField _audFile;
	private FileBrowseField _visFile;
	private FileBrowseField _vidFile;
	
	private JFormattedTextField _delayText;
	private JSpinner _numPts;
	private JCheckBox _connect;
	private JCheckBox _loop;
	private JCheckBox _useVideo;
	
	private JButton _startButton;
	
	private AnimationRenderer _renderer;
	private Boolean _video = null;
	private T _currTrial;
	
	private JFrame _testFrame;
	private AnimationPanel _aniPanel;
	private VideoPanel _vidPanel;
		
	//read data from user entries and create a trial
	
	public AVDemoGUIPanel(AVSession<?, T, ?> session) {
		super(session);
		setLayout(new MigLayout("", "[][]30px[][]30px[][]30px[][]", ""));

		// auditory info
		add(new JLabel("Auditory"), "split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");
		
		_frequency = new JComboBox(new String[]{"330Hz"});
		_frequency.setSelectedIndex(0);
		_frequency.addActionListener(_fUpdater);

		add(new JLabel("<html>&nbsp;&nbsp;&nbsp;Frequency"), "right");
		add(_frequency, "left, growx");
		
		_spectrum = new JComboBox(new String[]{"Puretone"});
		_spectrum.setSelectedIndex(0);
		_spectrum.addActionListener(_fUpdater);

		add(new JLabel("Spectrum"), "right");
		add(_spectrum, "left, growx");
		
		_envDuration = new JComboBox(new EnvelopeDuration[]{
				new EnvelopeDuration("Flat-228ms"), 
				new EnvelopeDuration("Flat-360ms"),
				new EnvelopeDuration("Flat-580ms"),
				new EnvelopeDuration("Perc-400ms"), 
				new EnvelopeDuration("Perc-600ms"), 
				new EnvelopeDuration("Perc-1075ms")});
		_envDuration.setSelectedIndex(0);
		_envDuration.addActionListener(_fUpdater);

		add(new JLabel("Envelope & Duration"), "right");
		add(_envDuration, "left, growx");
		
		add(new JLabel("Delay (ms)", 2), "right");
		_delayText = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_delayText.setValue(new Long(0)); // new
		_delayText.setColumns(5);
		
		add(_delayText, "left, wrap");
		
		// visual info
		add(new JLabel("Visual"), "split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");

		_pitches = new JComboBox(NotesEnum.values());
		_pitches.setSelectedItem(NotesEnum.D);
	    _pitches.addActionListener(_fUpdater);
	     
	    add(new JLabel("Pitch"), "right");
		add(_pitches, "left, growx");
	
		_vDurations = new JComboBox(DurationEnum.unDampedValues());
		_vDurations.setSelectedItem(DurationEnum.NORMAL);
		_vDurations.addActionListener(_fUpdater);
		
		add(new JLabel("Duration"), "right");
		add(_vDurations, "left, growx");
		
		_aDurations = new JComboBox(DurationEnum.unDampedValues());
		_aDurations.setSelectedItem(DurationEnum.NORMAL);
		_aDurations.addActionListener(_fUpdater);
		
		add(new JLabel("Audio Duration"), "right");
		add(_aDurations, "left, growx");
		
		add (new JLabel("Number of dots"), "right");
		SpinnerModel model = new SpinnerNumberModel(6, 1, 20, 1);
		_numPts = new JSpinner(model);
		add(_numPts, "left, wrap");
		
		add(new JLabel("Loop"), "right");
		_loop = new JCheckBox();
		add(_loop, "left");
		
		add(new JLabel("Use video"), "right");
		_useVideo = new JCheckBox();
		add(_useVideo, "left");
		//_useVideo.setEnabled(false);
		
		add(new JLabel("Connect dots w/ lines"), "right");
		_connect = new JCheckBox();
		add(_connect, "left, wrap");
		_connect.setSelected(true);

		// files 
		add(new JLabel("Files"), "split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");
		
		add(new JLabel("Audio File"));
		_audFile = new FileBrowseField(false);
		add(_audFile, "span, growx");
		
		add(new JLabel("Visual File"));
		_visFile = new FileBrowseField(false);
		add(_visFile, "span, growx");
		
		add(new JLabel("Video File"));
		_vidFile = new FileBrowseField(false);
		add(_vidFile, "span, growx");
		//_vidFile.setEnabled(false);
		
		JPanel p = new JPanel(new MigLayout("insets 0, fill"));
		_startButton = new JButton("Start");
		_startButton.addActionListener(new StartUpdater());
		p.add(_startButton, "center");
		add(p, "span, center, growx");
		
		_fUpdater.update();
	}
	
	protected abstract T createTrial(AnimationSequence animationSequence,
			boolean isVideo, PlayableMedia media, Long timingOffset,
			int animationPoints, float diskRadius, boolean connectDots);
	  
	@Override
	public synchronized T getTrial() {
		AVSession<?, ?, ?> session = getSession();
		final boolean vid = _useVideo.isSelected();
		float volume = session.getPlaybackGain();
		PlayableMedia media = vid ? MediaType.VIDEO.createDemoMedia(_vidFile.getFile(), volume) :
				MediaType.AUDIO.createDemoMedia(_audFile.getFile(), volume);
		try {
			AnimationSequence aniSeq = !vid ? AnimationParser.parseFile(
					_visFile.getFile(), session.getAnimationPointAspect()) : null;
			Object val = _delayText.getValue();
			Long delay = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
			return createTrial(aniSeq, vid, media, delay, 
					(Integer)_numPts.getValue(), session.getBaseAnimationPointSize(), 
					_connect.isSelected());
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
					
					JOptionPane.showMessageDialog(AVDemoGUIPanel.this, 
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
            
            _renderer = new AnimationRenderer();
            _aniPanel = new AnimationPanel(_renderer);
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

        _testFrame.setVisible(true);
        _testFrame.pack();
	}
	
	/**
	 * Class for updating file fields.
	 */
	private class FilePathUpdater implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			update();
		}
		
		public void update() {
		    
			File f = MediaType.AUDIO.getExpectedFile(getSession(), _frequency.getSelectedItem(), 
					_spectrum.getSelectedItem(), _envDuration.getSelectedItem());

			if(f == null) {
			    LogContext.getLogger().severe("Unable find file with form: " + 
			        MediaType.AUDIO.getExpectedBasename(
			        getSession(), _frequency.getSelectedItem(), 
                    _spectrum.getSelectedItem(), _envDuration.getSelectedItem()) + ".*");
			    return;
			}
			
			_audFile.setFile(f);
			
			String aniName = _pitches.getSelectedItem().toString().toLowerCase() + 
						((DurationEnum) _vDurations.getSelectedItem()).codeString() + 
						"_.txt";
			f = new File(getSession().getExpectedAnimationSubDir(), aniName);
			_visFile.setFile(f);
			
			f = MediaType.VIDEO.getExpectedFile(getSession(), _pitches.getSelectedItem(),
					_vDurations.getSelectedItem(), _aDurations.getSelectedItem());
			if (f == null) f = getSession().getExpectedVideoSubDir();
			_vidFile.setFile(f);
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
			T next = getTrial();
			prepareNext(next);
			next.preparePlayback(getSession(), _renderer);
			next.addPlaybackListener(new LoopListener(next));
			next.play();
		}
	}
	
	private class LoopListener implements TrialPlaybackListener {
		private T _trial;
		
		public LoopListener(T trial) {
			_trial = trial;
		}
		
		@Override
		public void playbackEnded() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					_trial.removePlaybackListener(LoopListener.this);
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
}
