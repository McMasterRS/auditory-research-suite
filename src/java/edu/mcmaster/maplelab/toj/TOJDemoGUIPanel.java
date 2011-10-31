/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */

package edu.mcmaster.maplelab.toj;

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
import edu.mcmaster.maplelab.av.media.PlayableMedia;
import edu.mcmaster.maplelab.av.media.PlayableMedia.MediaType;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.gui.FileBrowseField;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * This class creates a TOJDemoGUIPanel, which allows the user to run a TOJ demo without the setup screen.
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class TOJDemoGUIPanel extends DemoGUIPanel<TOJSession, TOJTrial>{
	
	private FilePathUpdater _fUpdater = new FilePathUpdater();
	private JComboBox _pitches;
	private JComboBox _vDurations;
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
	private TOJTrial _currTrial;
		
	//read data from user entries and create a trial
	
	public TOJDemoGUIPanel(TOJSession session) {
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
		
		_envDuration = new JComboBox(new String[]{"Flat-228ms", "Flat-360ms",
				"Flat-580ms", "Perc-400ms", "Perc-600ms", "Perc-1075ms"});
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
		_useVideo.setEnabled(false);
		
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
		_vidFile.setEnabled(false);
		
		JPanel p = new JPanel(new MigLayout("insets 0, fill"));
		_startButton = new JButton("Start");
		_startButton.addActionListener(new StartUpdater());
		p.add(_startButton, "center");
		add(p, "span, center, growx");
		
		_fUpdater.update();
	}
	  
	@Override
	public TOJTrial getTrial() {
			
		TOJSession session = getSession();
		final boolean vid = _useVideo.isSelected();
		float volume = session.getPlaybackGain();
		PlayableMedia media = vid ? MediaType.VIDEO.createDemoMedia(_vidFile.getFile(), volume) :
				MediaType.AUDIO.createDemoMedia(_audFile.getFile(), volume);
		try {
			AnimationSequence aniSeq = AnimationParser.parseFile(
					_visFile.getFile(), session.getAnimationPointAspect());
			Object val = _delayText.getValue();
			Long delay = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
			return new TOJTrial(aniSeq, vid, media, delay, 
					(Integer)_numPts.getValue(), session.getBaseAnimationPointSize(), 
					_connect.isSelected());
		}
		catch (FileNotFoundException ex) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					boolean vis = _visFile.getFile().exists();
					boolean aud = _audFile.getFile().exists();
					String msg = "%s file";
					if (aud & vis) {
						msg = "Audio and animation files";
					}
					else String.format(msg, aud ? "Animation" : "Audio");
					
					JOptionPane.showMessageDialog(TOJDemoGUIPanel.this, 
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
	private void prepareDemoScreen() {
		if (_renderer == null) {
    		JFrame testFrame = new JFrame(AnimationPanel.class.getName());
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            _renderer = new AnimationRenderer();
        	AnimationPanel view = new AnimationPanel(_renderer);

        	testFrame.getContentPane().removeAll();
            testFrame.getContentPane().add(view, BorderLayout.CENTER);
            
            testFrame.pack();
            Window w = getParentWindow();
            testFrame.setLocation(w.getLocation().x + w.getWidth(), w.getLocation().y);
            testFrame.setVisible(true);
    	}
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
			_audFile.setFile(f);
			
			String aniName = _pitches.getSelectedItem().toString().toLowerCase() + 
						((DurationEnum) _vDurations.getSelectedItem()).codeString() + 
						"_.txt";
			f = new File(getSession().getExpectedAnimationSubDir(), aniName);
			_visFile.setFile(f);
			
			/*File vidDir = new File("");				 //TODO: get video file
			_vidFile.setFile(vidDir);*/
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
			prepareDemoScreen();
			
			_startButton.setEnabled(false);
			_currTrial = getTrial();
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
		JFrame f = new JFrame(TOJDemoGUIPanel.class.getName());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        f.getContentPane().add(new TOJDemoGUIPanel(null));
        f.pack();
        f.setVisible(true);   
	}
}
