/*
* Copyright (C) 2006-2008 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: SimpleSetupScreen.java 474 2009-03-20 17:53:30Z bhocking $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.*;
import java.io.File;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.Session;


/**
 * Setup editor for non-applet mode.
 * 
 * @version   $Revision: 474 $
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Dec 4, 2006
 */
public class SimpleSetupScreen extends JPanel  {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = 8595156800591101562L;

	enum ConfigKeys {
        isFullScreen,
        isDemoMode
    }
    
    private final JFormattedTextField _raID;
    private final JFormattedTextField _subject;
    private final JFormattedTextField _session;
    private final boolean _includeSex;
    private ButtonGroup _sexOptions = null;
    private final FileBrowseField _dataDir;
    private final String _prefsPrefix;
    private GridBagConstraints _labelGBC;
    private GridBagConstraints _fieldGBC;
    private JCheckBox _fullScreen;
    private JCheckBox _demoMode;

    public SimpleSetupScreen(String prefsPrefix) {
        this(prefsPrefix, false, false);
    }
    
    public SimpleSetupScreen(String prefsPrefix, boolean includeDemoModeSwitch, boolean includeSex) {
        super(new GridBagLayout());
        _prefsPrefix = prefsPrefix;
        setBorder(BorderFactory.createTitledBorder("Setup"));
        _labelGBC = new GridBagConstraints();
        _labelGBC.gridx = 0;
        _labelGBC.anchor = GridBagConstraints.EAST;
        
        _fieldGBC = new GridBagConstraints();
        _fieldGBC.anchor = GridBagConstraints.EAST;
        _fieldGBC.gridx = 1;
        _fieldGBC.weightx = 1;
        _fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        
        
        addLabel("RA ID:");
        _raID = new JFormattedTextField();
        _raID.setValue(new Integer(1));
        addField(_raID);

        addLabel("Subject #:");
        _subject = new JFormattedTextField();
        _subject.setValue(new Integer(1));
        addField(_subject);

        addLabel("Session #:");
        _session = new JFormattedTextField();
        _session.setValue(new Integer(1));
        addField(_session);
        
        _includeSex = includeSex;
        if (includeSex) {
            addLabel("Sex:");
            JPanel sexPanel = new JPanel();
            _sexOptions = new ButtonGroup();
            String[] options = {"M", "F"};
            for (String opt: options) {
                JRadioButton curOption = new JRadioButton(opt);
                _sexOptions.add(curOption);
                sexPanel.add(curOption);
            }
            addField(sexPanel);
        }        
        
        addLabel("Data directory:");
        _dataDir = new FileBrowseField(true);
        addField(_dataDir);
        
        addLabel("Full screen:");
        _fullScreen = new JCheckBox();
        addField(_fullScreen);
        
        if(includeDemoModeSwitch) {
            addLabel("Demo mode:");
            _demoMode = new JCheckBox();
            addField(_demoMode);
        }
        
        // Create buffer space between standard fields and fields
        // user may add later. Also provides better distribution of extra space.
        _labelGBC.weighty = 1;
        _fieldGBC.weighty = 1;
        add(new JLabel(), _labelGBC);
        add(new JLabel(), _fieldGBC);
        _labelGBC.weighty = 0;
        _fieldGBC.weighty = 0;
        
        _dataDir.setFile(new File("."));
        
        restore();
    }
    
    /**
     * Method (in conjunction with {@link #addField(JComponent)}) for
     * adding form controls to the setup screen. This should be called
     * first, then {@link #addField(JComponent)}. 
     * 
     * @param label label for control
     */
    public void addLabel(String label) {
        add(new JLabel(label), _labelGBC);
    }
    
    /**
     * Method for adding a form control to the setup screen. Should be called
     * after {@link #addLabel(String)}. Control is just placed in the panel; no
     * other management is performed
     * 
     * @param field field to add.
     */
    public void addField(JComponent field)  {
        add(field, _fieldGBC);
    }
    
    
    public void applySettings(Session<?,?> session) {
        session.setRAID(Integer.parseInt(_raID.getText()));
        session.setSession(Integer.parseInt(_session.getText()));
        session.setSubject(Integer.parseInt(_subject.getText()));
        String sex = "NA";
        if (_includeSex) {
            Enumeration<AbstractButton> enumAB = _sexOptions.getElements();
            while (enumAB.hasMoreElements()) {
                AbstractButton ab = enumAB.nextElement();
                if (ab.isSelected()) {
                    sex = ab.getText();
                    break;
                }
            }
        }
        session.setSex(sex);
        session.setDataDir(_dataDir.getFile());
        if(_demoMode != null) {
            session.setDemo(_demoMode.isSelected());
        }
    }
    

    public boolean isFullScreen() {
        return _fullScreen.isSelected();
    }

    public void setRAID(int val) {
        _raID.setValue(val);
    }
    
    public void setSubjectID(int val) {
        _subject.setValue(val);
    }

    
    public void setSessionID(int val) {
        _session.setValue(val);
    }

    public void setDataDir(File file) {
        _dataDir.setFile(file);
    }
    
    

    /**
     * Get the preferences node.
     */
    public Preferences prefs() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        // Create a new node.
        prefs = prefs.node(_prefsPrefix + ".setup");
        return prefs; 
    }
    
    /**
     * @return
     * @uml.property  name="dataDir"
     */
    public File getDataDir() {
        return _dataDir.getFile();
    }
    
    void save() {
        Preferences prefs = prefs();
        prefs.put(Session.ConfigKeys.raid.name(), _raID.getText());
        prefs.put(Session.ConfigKeys.subject.name(), _subject.getText());
        prefs.put(Session.ConfigKeys.session.name(), _session.getText());
        prefs.put(Session.ConfigKeys.dataDir.name(), _dataDir.getFile().getAbsolutePath());
        prefs.putBoolean(ConfigKeys.isFullScreen.name(), _fullScreen.isSelected());
        if(_demoMode != null) {
            prefs.putBoolean(ConfigKeys.isDemoMode.name(), _demoMode.isSelected());
        }

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
        _raID.setText(prefs.get(Session.ConfigKeys.raid.name(), "1"));
        
        int subject = 0;
        try {
            subject = Integer.parseInt(prefs.get(Session.ConfigKeys.subject.name(), "0"));
        }
        catch(Throwable ex){
        }
        
        _subject.setValue(++subject);
        _session.setText(prefs.get(Session.ConfigKeys.session.name(), "1"));
        
        String path = prefs.get(Session.ConfigKeys.dataDir.name(),  null);
        if(path == null) {
            path = ".";
        }
            
        _dataDir.setFile(new File(path));
        
        boolean isFullScreen = prefs.getBoolean(ConfigKeys.isFullScreen.name(), false);
        _fullScreen.setSelected(isFullScreen);
        
        if(_demoMode != null) {
            boolean isDemoMode = prefs.getBoolean(ConfigKeys.isDemoMode.name(), false);
            _demoMode.setSelected(isDemoMode);
        }
    }

    /**
     * Display the screen in a dialog, blocking until OK pressed.
     */
    public void display() {
        final JDialog d = new JDialog((Frame)null, "Experiment Setup", true);
        d.getContentPane().add(this);
        CloseButton.createClosePanel(d, null);        
        d.setSize(400, 300);
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        
        save();
        d.dispose();
    }


}
