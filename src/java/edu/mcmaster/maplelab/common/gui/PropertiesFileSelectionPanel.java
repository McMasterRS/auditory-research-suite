package edu.mcmaster.maplelab.common.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.mcmaster.maplelab.common.LogContext;

import net.miginfocom.swing.MigLayout;

/**
 * Widget for selection of Properties file.
 * 
 * @author zachbrown
 *
 */
public class PropertiesFileSelectionPanel extends JPanel implements ActionListener, PropertyChangeListener {

	// Action commands for radio buttons
	private static String dataDirActionCommand = "datadir";
	private static String specDirActionCommand = "specdir";
		
	// Keep a reference to the dataDirField so we can update the combobox
	private final FileBrowseField _dataDirField;
	
	private JRadioButton _useDataDirRButton;
	private JRadioButton _useSpecifiedDirRButton;
	private FileBrowseField _browseField;
	private JLabel _propFileVersionLabel;
	private JComboBox _propFileVersionComboBox;
	
	// Default string for indicating lack of available properties files in combobox
	private static final String NO_PROP_FILES_IN_DIR = "No properties files found in chosen directory";
	// Used to keep track of current available properties files
	private File[] _possiblePropertiesFiles;
	
	/**
	 * Constructor.
	 * 
	 * @param dataDirField {@link FileBrowseField} that can specify the "data directory" for this widget.
	 */
	public PropertiesFileSelectionPanel(FileBrowseField dataDirField) {
		super(new MigLayout("insets 0 0 0 0, nogrid, fill"));
		setBorder(BorderFactory.createTitledBorder("Properties File"));
		
		_dataDirField = dataDirField;
		
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
		_browseField.addFileChoiceChangeListener(this);
		add(_browseField, "growx, wrap");
		
		_propFileVersionLabel = new JLabel("Properties File Version:");
		add(_propFileVersionLabel, "gapright 5");
		
		String[] defaultItems = {NO_PROP_FILES_IN_DIR};
		_propFileVersionComboBox = new JComboBox(defaultItems);
		_propFileVersionComboBox.setToolTipText("<html>Specify a properties file version to use.<br>" +
				"An input of [version] is used to find a file that looks like: <br>" +
				"[experiment].properties-[version]</html>");

		add(_propFileVersionComboBox, "wrap");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(dataDirActionCommand)) {
			_browseField.setEnabled(false);
			updatePropFileChoices(_dataDirField.getFile());
		} else if (e.getActionCommand().equals(specDirActionCommand)) {
			_browseField.setEnabled(true);
			updatePropFileChoices(_browseField.getFile());
		}
	}
	
	/**
	 * Get the selected properties file found by this widget.
	 * Will return null if no valid properties file has been selected.
	 * 
	 * @return A {@link File} for the selected properties file.
	 */
	public File getSelectedPropertiesFile() {
		// Check if there are any possible files to return
		if (_possiblePropertiesFiles == null || _possiblePropertiesFiles.length == 0) {
			// Fail, no valid properties files were available.
			return null;
		}
		
		// Get selected file
		File propFile = _possiblePropertiesFiles[_propFileVersionComboBox.getSelectedIndex()];
		
		if (propFile != null && propFile.exists()) {
			LogContext.getLogger().fine("Selected properties file is:" + propFile.getAbsolutePath());
		} else {
			LogContext.getLogger().fine("Unable to find user specified properties file: " + propFile.getAbsolutePath());
		}
		return propFile;
	}
	
	/*
	 * Update the ComboBox with possible properties files found in the given directory File.
	 */
	private void updatePropFileChoices(File dir) {
		_possiblePropertiesFiles = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// Only accept actual files that are .properties files 
				return file.isFile() && file.getName().contains(".properties");
			}
		});
		
		// get rid of old items before updating
		_propFileVersionComboBox.removeAllItems();
		if (_possiblePropertiesFiles != null && _possiblePropertiesFiles.length > 0) {
			for (File f : _possiblePropertiesFiles) {
				_propFileVersionComboBox.addItem(f.getName());
			}
		} else {
			// Given directory has no valid children, or file is not a directory
			_propFileVersionComboBox.addItem(NO_PROP_FILES_IN_DIR);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updatePropFileChoices((File)evt.getNewValue());
	}
	
	
	// main method for separate testing
//	public static void main(String[] args) {
//		try {
//			JFrame foo = new JFrame();
//			foo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			foo.getContentPane().add(new PropertiesFileSelectionPanel(null));
//			foo.pack();
//			foo.setVisible(true);
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
}
