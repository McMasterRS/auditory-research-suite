package edu.mcmaster.maplelab.common.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * Widget for selection of Properties file.
 * 
 * @author zachbrown
 *
 */
public class PropertiesFileSelectionPanel extends JPanel implements ActionListener {

	private static String dataDirActionCommand = "datadir";
	private static String specDirActionCommand = "specdir";
	
	private JRadioButton _useDataDirRButton;
	private JRadioButton _useSpecifiedDirRButton;
	private JLabel _propFileVersionLabel;
	private JTextField _propFileVersionField;
	private FileBrowseField _browseField;
	
	public PropertiesFileSelectionPanel() {
		super(new MigLayout("insets 0 0 0 0, nogrid, fill"));
		setBorder(BorderFactory.createTitledBorder("Properties File"));
		
		_useDataDirRButton = new JRadioButton("Use Data Directory", true);
		_useDataDirRButton.setActionCommand(dataDirActionCommand);
		_useDataDirRButton.addActionListener(this);
		_useDataDirRButton.setToolTipText("Select this to read the properties file from the Data Directory.");
		add(_useDataDirRButton);
		
		_useSpecifiedDirRButton = new JRadioButton("Use Specified Directory");
		_useSpecifiedDirRButton.setActionCommand(specDirActionCommand);
		_useSpecifiedDirRButton.addActionListener(this);
		_useSpecifiedDirRButton.setToolTipText("Select this to read the properties file from a user specified location.");
		add(_useSpecifiedDirRButton, "wrap");
		
		// ButtonGroup links the buttons so they are mutually exclusive
		ButtonGroup bg = new ButtonGroup();
		bg.add(_useDataDirRButton);
		bg.add(_useSpecifiedDirRButton);
		
		_propFileVersionLabel = new JLabel("Properties File Version:");
		add(_propFileVersionLabel, "gapright 5");
		
		_propFileVersionField = new JTextField(25);
		add(_propFileVersionField, "wrap");
		
		_browseField = new FileBrowseField(false, true);
		_browseField.setEnabled(false);
		add(_browseField, "growx");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(dataDirActionCommand)) {
			_propFileVersionLabel.setEnabled(true);
			_propFileVersionField.setEnabled(true);
			_browseField.setEnabled(false);
		} else if (e.getActionCommand().equals(specDirActionCommand)) {
			_propFileVersionLabel.setEnabled(false);
			_propFileVersionField.setEnabled(false);
			_browseField.setEnabled(true);
		}
	}
	
	
	public String getSelectedPropertiesFile() {
		// TODO: add checking to return version or full absolute path. 
		// Need SimpleSetupScreen to return a correct File to Experiment Frame
		// OR maybe better to have some check method in SSS to check for version num or browse path.
		return "";
	}
	
	// main method for separate testing
	public static void main(String[] args) {
		try {
			JFrame foo = new JFrame();
			foo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			foo.getContentPane().add(new PropertiesFileSelectionPanel());
			foo.pack();
			foo.setVisible(true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

