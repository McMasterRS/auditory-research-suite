package edu.mcmaster.maplelab.common.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import edu.mcmaster.maplelab.common.LogContext;

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
		_useSpecifiedDirRButton.setToolTipText("Select this to read the properties file from a user specified directory.");
		add(_useSpecifiedDirRButton, "wrap");
		
		// ButtonGroup links the buttons so they are mutually exclusive
		ButtonGroup bg = new ButtonGroup();
		bg.add(_useDataDirRButton);
		bg.add(_useSpecifiedDirRButton);
		
		_browseField = new FileBrowseField(true);
		_browseField.setEnabled(false);
		add(_browseField, "growx, wrap");
		
		_propFileVersionLabel = new JLabel("Properties File Version:");
		add(_propFileVersionLabel, "gapright 5");
		
		_propFileVersionField = new JTextField(20);
		_propFileVersionField.setToolTipText("<html>Specify a properties file version to use.<br>" +
				"An input of [version] is used to find a file that looks like: <br>" +
				"[experiment].properties-[version]</html>");
		add(_propFileVersionField, "wrap");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(dataDirActionCommand)) {
			_browseField.setEnabled(false);
		} else if (e.getActionCommand().equals(specDirActionCommand)) {
			_browseField.setEnabled(true);
		}
	}
	
	public File getSelectedPropertiesFile(SimpleSetupScreen<?> setup) {
		File propFile = null;
		
		String propFileStr = setup.getPrefsPrefix() + ".properties";
		String version = _propFileVersionField.getText().trim();
		
		// Add version modifier to file name if text has been input.
		// Makes the file searched for of the form [experiment name].properties-[version]
		if (!"".equals(version)) {
			propFileStr += ("-" + version);
		}
		
		File propertiesDir;
		if (_useDataDirRButton.isSelected()) {
			propertiesDir = setup.getDataDir();
		} 
		// Otherwise the specified directory is selected. 
		else {
			propertiesDir = _browseField.getFile();
		}
		propFile = new File(propertiesDir, propFileStr);
		
		if (propFile != null && propFile.exists()) {
			LogContext.getLogger().fine("Selected properties file is:" + propFile.getAbsolutePath());
		} else {
			LogContext.getLogger().fine("Unable to find user specified properties file: " + propFile.getAbsolutePath());
		}
		
		return propFile;
	}
	
	// main method for separate testing
//	public static void main(String[] args) {
//		try {
//			JFrame foo = new JFrame();
//			foo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			foo.getContentPane().add(new PropertiesFileSelectionPanel());
//			foo.pack();
//			foo.setVisible(true);
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
}
