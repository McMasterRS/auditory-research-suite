/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */

package edu.mcmaster.maplelab.toj;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.gui.FileBrowseField;
import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.sound.Playable;
import edu.mcmaster.maplelab.common.sound.SoundClip;
import edu.mcmaster.maplelab.toj.animator.AnimationParser;
import edu.mcmaster.maplelab.toj.animator.AnimationRenderer;
import edu.mcmaster.maplelab.toj.animator.AnimationSequence;
import edu.mcmaster.maplelab.toj.animator.AnimatorPanel;
import edu.mcmaster.maplelab.toj.datamodel.TOJSession;
import edu.mcmaster.maplelab.toj.datamodel.TOJTrial;

/**
 * This class creates a TOJDemoGUIPanel, which allows the user to run a TOJ demo without the setup screen.
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */
public class TOJDemoGUIPanel extends DemoGUIPanel<TOJSession, TOJTrial>{
	
	FilePathUpdater _fUpdater = new FilePathUpdater();
	JComboBox _pitches;
	JComboBox _aDurations;
	JComboBox _vDurations;
	
	FileBrowseField _audFile;
	FileBrowseField _visFile;
	
	JFormattedTextField _delayText;
	JSpinner _numPts;
	JCheckBox _useVideo;
	
	private AnimationRenderer _renderer;
		
	//read data from user entries and create a trial
	
	public TOJDemoGUIPanel(TOJSession session) {
		super(session);
		setLayout(new MigLayout("", "[right]"));

		setPreferredSize(new Dimension(640, 480));

		// auditory info
		add(new JLabel("Auditory"),  "split, span, gaptop 10");
		add(new JSeparator(),       "growx, wrap, gaptop 10");

		_pitches = new JComboBox(NotesEnum.values());
		_pitches.setSelectedItem(NotesEnum.D);
	    _pitches.addActionListener(_fUpdater);
	     
	    add(new JLabel("Pitch"),  "gap 10");
		add(_pitches, "growx");
		
		_aDurations = new JComboBox(DurationEnum.values());
		_aDurations.setSelectedItem(DurationEnum.LONG);
		_aDurations.addActionListener(_fUpdater);
		
		add(new JLabel("Duration"),  "gap 10");
		add(_aDurations, "growx");
		
		add(new JLabel("Delay (ms)", 2), "gap 10");
		_delayText = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_delayText.setValue(new Long(0)); // new
		_delayText.setColumns(2);
		
		add(_delayText,     "gapright 100, wrap");
		
		// visual info
		add(new JLabel("Visual"),	"split, span, gaptop 10");
		add(new JSeparator(),       "growx, wrap, gaptop 10");
		
		add(new JLabel("Use Video"), "gap 10");
		_useVideo = new JCheckBox();
		_useVideo.setEnabled(false);
		add(_useVideo);
		_useVideo.setEnabled(false);
	
		_vDurations = new JComboBox(DurationEnum.values());
		_vDurations.setSelectedItem(DurationEnum.NORMAL);
		_vDurations.addActionListener(_fUpdater);
		
		add(new JLabel("Duration"),  "gap 10");
		add(_vDurations, "growx");
		
		add (new JLabel("Number of points"), "gap 10");
		SpinnerModel model = new SpinnerNumberModel(6, 1, 10, 1);
		_numPts = new JSpinner(model);
		add(_numPts, 		"gapright 95, wrap");
		
		add(new JLabel("Loop"));
		JCheckBox loopBox = new JCheckBox();
		add(loopBox, "wrap");
		loopBox.setEnabled(false);

		// files 
		add(new JLabel("Files"), "split, span, gaptop 10");
		add(new JSeparator(),    "growx, wrap, gaptop 10");
		
		add(new JLabel("Audio File"));
		_audFile = new FileBrowseField(false);
		String audFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() +"_" + _aDurations.getSelectedItem().toString().toLowerCase().charAt(0) + ".wav");
		File audDir = new File(session.getExpectedAudioSubDir(), audFileName);
		_audFile.setFile(audDir);
		add (_audFile, "span, growx");
		
		add(new JLabel("Visual File"));
		_visFile = new FileBrowseField(false);
		String visFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() + _vDurations.getSelectedItem().toString().toLowerCase().charAt(0) + "_.txt"); 
		File visDir = new File(session.getExpectedVisualSubDir(), visFileName);
		_visFile.setFile(visDir);
		add (_visFile, "span, growx");
		
		add(new JLabel("Video File"));
		FileBrowseField vidFile = new FileBrowseField(false);
		File vidDir = new File("");				 //TODO: get video file
		vidFile.setFile(vidDir);
		add (vidFile, "span, growx");
		vidFile.setEnabled(false);
		
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new StartUpdater());
		add(startButton);
	}
	  
	@Override
	public TOJTrial getTrial() {
			
		Playable playable = SoundClip.findPlayable(_audFile.getFile().toString(), getSession().getExpectedAudioSubDir()); // TODO: fix sound clip duration
		
		try {
			AnimationSequence aniSeq = AnimationParser.parseFile(_visFile.getFile());
			Object val = _delayText.getValue();
			Long delay = Long.valueOf(val instanceof String ? (String) val : ((Number) val).toString());
			return new TOJTrial(aniSeq, _useVideo.isSelected(), playable, delay, (Integer)_numPts.getValue(), 0.3f);
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
    		JFrame testFrame = new JFrame(AnimatorPanel.class.getName());
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            _renderer = new AnimationRenderer(true); // connect the dots
        	AnimatorPanel view = new AnimatorPanel(_renderer);

        	testFrame.getContentPane().removeAll();
            testFrame.getContentPane().add(view, BorderLayout.CENTER);
            
            testFrame.pack();
            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);
    	}
	}
	
	/**
	 * Class for updating file fields.
	 */
	private class FilePathUpdater implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String audFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() +"_" + _aDurations.getSelectedItem().toString().toLowerCase().charAt(0) + ".wav");
			File audDir = new File(getSession().getExpectedAudioSubDir(), audFileName);
			_audFile.setFile(audDir);

			String visFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() + _vDurations.getSelectedItem().toString().toLowerCase().charAt(0) + "_.txt"); 
			File visDir = new File(getSession().getExpectedVisualSubDir(), visFileName);
			_visFile.setFile(visDir);
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
			
			TOJTrial t = getTrial();
			t.preparePlayback(getSession(), _renderer);
			t.play();
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
