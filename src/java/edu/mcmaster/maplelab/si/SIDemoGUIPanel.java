package edu.mcmaster.maplelab.si;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
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
import edu.mcmaster.maplelab.av.datamodel.TrialPlaybackListener;
import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.av.media.QTVideoClip;
import edu.mcmaster.maplelab.av.media.SoundClip;
import edu.mcmaster.maplelab.av.media.PlayableMedia.MediaType;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.gui.FileBrowseField;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

/**
 * Demo panel for SI experiment.
 * 
 * @author bguseman
 */
public class SIDemoGUIPanel extends DemoGUIPanel<SISession, SITrial> {
	
	private JFrame _testFrame;
	
	private FilePathUpdater _fUpdater = new FilePathUpdater();
	private JComboBox _pitches;
	private JComboBox _vDurations;
	private JComboBox _aDurations;
	
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
	private SITrial _currTrial;

	public SIDemoGUIPanel(SISession session) {
		super(session);
		
		setLayout(new MigLayout("", "[][]30px[][]30px[][]30px[][]", ""));

		_pitches = new JComboBox(NotesEnum.values());
		_pitches.setSelectedItem(NotesEnum.D);
	    _pitches.addActionListener(_fUpdater);
	     
	    add(new JLabel("Pitch"), "right");
		add(_pitches, "left, growx, wrap");

		// auditory info
		add(new JLabel("Auditory"), "split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");
		
		_aDurations = new JComboBox(DurationEnum.unDampedValues());
		_aDurations.setSelectedItem(DurationEnum.NORMAL);
		_aDurations.addActionListener(_fUpdater);

		add(new JLabel("Duration"), "right");
		add(_aDurations, "left, growx");
		//add(new JLabel("<html>&nbsp;&nbsp;&nbsp;Frequency"), "right");
		
		add(new JLabel("Delay (ms)", 2), "right");
		_delayText = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_delayText.setValue(new Long(0)); // new
		_delayText.setColumns(5);
		
		add(_delayText, "left, wrap");
		
		// visual info
		add(new JLabel("Visual"), "split, span, gaptop 10");
		add(new JSeparator(), "growx, wrap, gaptop 10");
	
		_vDurations = new JComboBox(DurationEnum.unDampedValues());
		_vDurations.setSelectedItem(DurationEnum.NORMAL);
		_vDurations.addActionListener(_fUpdater);
		
		add(new JLabel("Duration"), "right");
		add(_vDurations, "left, growx");
		
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
		
		JPanel p = new JPanel(new MigLayout("insets 0, fill"));
		_startButton = new JButton("Start");
		_startButton.addActionListener(new StartUpdater());
		p.add(_startButton, "center");
		add(p, "span, center, growx");
		
		_fUpdater.update();
	}

	@Override
	public SITrial getTrial() {
		SISession session = getSession();
		final boolean vid = _useVideo.isSelected();
		float volume = session.getPlaybackGain();
		PlayableMedia media = vid ? MediaType.VIDEO.createDemoMedia(_vidFile.getFile(), volume) :
				MediaType.LEGACY_AUDIO.createDemoMedia(_audFile.getFile(), volume);
		
		try {
			if (media == null || media.getPlayable() == null) throw new FileNotFoundException();
			AnimationSequence aniSeq = vid ? null : AnimationParser.parseFile(
					_visFile.getFile(), session.getAnimationPointAspect());
			Object val = _delayText.getValue();
			Long delay = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
			return new SITrial(aniSeq, vid, media, delay, (Integer)_numPts.getValue(), 
					session.getBaseAnimationPointSize(), _connect.isSelected());
		}
		catch (FileNotFoundException ex) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					boolean vis = _visFile.getFile().exists();
					boolean aud = _audFile.getFile().exists();
					String msg = "%s file";
					if (vid) {
						msg = String.format(msg, "Video");
					}
					else if (!aud && !vis) {
						msg = "Audio and animation files";
					}
					else msg = String.format(msg, aud ? "Animation" : "Audio");
					
					JOptionPane.showMessageDialog(SIDemoGUIPanel.this, 
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
	private void prepareDemoScreen(SITrial currTrial) {
		if (_renderer == null) {
    		_testFrame = new JFrame("Sensory Integration Demo");
            _testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            _renderer = new AnimationRenderer();
            
            Window w = getParentWindow();
            _testFrame.setLocation(w.getLocation().x + w.getWidth(), w.getLocation().y);
            _testFrame.setVisible(true);
    	}
		
		_testFrame.getContentPane().removeAll();
        if (currTrial.isVideo()) {
			JPanel p = new JPanel(new MigLayout("insets 0, fill"));
			p.add(QTVideoClip.makeComponent(currTrial.getVideoPlayable()));
			_testFrame.getContentPane().add(p, BorderLayout.CENTER);
		}
		else {
        	AnimationPanel view = new AnimationPanel(_renderer);
            _testFrame.getContentPane().add(view, BorderLayout.CENTER);
		}
		_testFrame.pack();
	}
	
	/**
	 * Class for updating file fields.
	 */
	private class FilePathUpdater implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			update();
		}
		
		public void update() {
			File f = MediaType.LEGACY_AUDIO.getExpectedFile(getSession(), 
					_pitches.getSelectedItem(), _aDurations.getSelectedItem());
			_audFile.setFile(f);

			
			String aniName = _pitches.getSelectedItem().toString().toLowerCase() + 
						((DurationEnum) _vDurations.getSelectedItem()).codeString() + 
						"_.txt";
			f = new File(getSession().getExpectedAnimationSubDir(), aniName);
			_visFile.setFile(f);
			
			File vidDir = getSession().getExpectedVideoSubDir();
			f = MediaType.VIDEO.getExpectedFile(getSession(), _pitches.getSelectedItem(), 
					_vDurations.getSelectedItem(), _aDurations.getSelectedItem());
			_vidFile.setFile(f != null ? f : vidDir);
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
			_currTrial = getTrial();
			prepareDemoScreen(_currTrial);
			_currTrial.preparePlayback(getSession(), _renderer);
			_currTrial.addPlaybackListener(new LoopListener());
			_currTrial.play();
		}
	}
	
	private class LoopListener implements TrialPlaybackListener {
		@Override
		public void playbackEnded() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					_currTrial.removePlaybackListener(LoopListener.this);
					if (_loop.isSelected()) {
						_currTrial = getTrial();
						_currTrial.preparePlayback(getSession(), _renderer);
						_currTrial.addPlaybackListener(LoopListener.this);
						_currTrial.play();
					}
					else {
						_startButton.setEnabled(true);
					}
				}
			});
			
		}
	}

	/**
	 * Test routine.
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame(SIDemoGUIPanel.class.getName());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        f.getContentPane().add(new SIDemoGUIPanel(null));
        f.pack();
        f.setVisible(true);   
	}
}
