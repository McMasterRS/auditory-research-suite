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


//public class TOJDemoGUIPanel extends JPanel implements DemoGUIPanel {
public class TOJDemoGUIPanel extends DemoGUIPanel<TOJSession, TOJTrial>{
	
	FilePathUpdater _fUpdater = new FilePathUpdater();
	JComboBox _pitches;
	JComboBox _aDurations;
	JComboBox _vDurations;
	
	FileBrowseField _audFile;
	FileBrowseField _visFile;
	
	//JTextField _delayText;
	JFormattedTextField _delayText;
	JSpinner _numPts;
	JCheckBox _useVideo;
	
	private JFrame _testFrame;
		
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
		_delayText = new JFormattedTextField(NumberFormat.getNumberInstance());
		_delayText.setValue(new Float(0)); // new
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
		SpinnerModel model = new SpinnerNumberModel(4, 1, 20, 1);
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
		File audDir = new File("datafiles/examples/aud" + "/" + audFileName);
		_audFile.setFile(audDir);
		add (_audFile, "span, growx");

		
		add(new JLabel("Visual File"));
		_visFile = new FileBrowseField(false);
		String visFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() + _vDurations.getSelectedItem().toString().toLowerCase().charAt(0) + "_.txt"); 
		File visDir = new File("datafiles/examples/vis" + "/" + visFileName);
		_visFile.setFile(visDir);
		add (_visFile, "span, growx");
		
		
		add(new JLabel("Video File"));
		FileBrowseField vidFile = new FileBrowseField(false);
		File vidDir = new File(""); //TODO: get video file
		vidFile.setFile(vidDir);
		add (vidFile, "span, growx");
		vidFile.setEnabled(false);

		
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new StartUpdater());
		add(startButton);
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame(TOJDemoGUIPanel.class.getName());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        f.getContentPane().add(new TOJDemoGUIPanel(null));
        f.pack();
        f.setVisible(true);   
	}
	  
	@Override
	public TOJTrial getTrial() {
		// turn pitch, duration data into filenames
			
		Playable playable = SoundClip.findPlayable(_audFile.getFile().toString(), new File("datafiles/examples/aud")); // TODO: fix sound clip duration
		
		try {
			AnimationSequence aniSeq = AnimationParser.parseFile(_visFile.getFile());
			return new TOJTrial(aniSeq, _useVideo.isSelected(), playable, (Float)_delayText.getValue(), (Integer)_numPts.getValue(), 0.3f);
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} 
		
		return null;
	}
	
	private class FilePathUpdater implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if ((JComboBox)source == _pitches) {
				System.out.println("Auditory pitch was changed");
			}
			if ((JComboBox)source == _aDurations) {
				System.out.println("Auditory duration was changed");
			}
			if ((JComboBox)source == _vDurations) {
				System.out.println("Visual duration was changed");
			}

			String audFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() +"_" + _aDurations.getSelectedItem().toString().toLowerCase().charAt(0) + ".wav");
			File audDir = new File("datafiles/examples/aud" + "/" + audFileName);
			_audFile.setFile(audDir);

			String visFileName = new String(_pitches.getSelectedItem().toString().toLowerCase() + _vDurations.getSelectedItem().toString().toLowerCase().charAt(0) + "_.txt"); 
			File visDir = new File("datafiles/examples/vis" + "/" + visFileName);
			_visFile.setFile(visDir);
		}
	}
	
	private class StartUpdater implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Start button pressed\n");
			System.out.printf("delay = %f\n", _delayText.getValue());
			
			TOJTrial t = getTrial();

			t.printDescription();
			
			testRun(t);
		}
	}
	
	public void testRun(TOJTrial trial) {

        try {
        	if (_testFrame == null) {
        		_testFrame = new JFrame(AnimatorPanel.class.getName());
                _testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                _testFrame.setVisible(true);
                
        	}
        	AnimationRenderer renderer = new AnimationRenderer(true); // connect the dots
        	final AnimatorPanel view = new AnimatorPanel(renderer);

        	_testFrame.getContentPane().removeAll();
            _testFrame.getContentPane().add(view, BorderLayout.CENTER);
            _testFrame.pack();
            
           
            final Playable audio = trial.getPlayable();
			
			Runnable r = new Runnable() {
				@Override
				public void run() {
					audio.play();

				}
			} ;

            renderer.setTrial(trial);
            renderer.setCurrentFrame(0);
            
            
    		long currentTime = System.currentTimeMillis();

    		renderer.setStartTime(currentTime); 
    		SwingUtilities.invokeLater(r);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
		
    
	}
}
