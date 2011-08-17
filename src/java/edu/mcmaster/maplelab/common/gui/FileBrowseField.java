/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;


/**
 * Widget encapsulating a text field for file path and browse button.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Feb 26, 2007
 */
public class FileBrowseField extends JPanel {
    private final boolean _directoriesOnly;
    
    private final JTextField _fileName;

	private JButton _button;

    public FileBrowseField(boolean directoriesOnly) {
        super(new GridBagLayout());
        
        _directoriesOnly = directoriesOnly;
        
        _fileName = new JTextField();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        add(_fileName, gbc);
        
        _button = new JButton("Browse...");
        _button.addActionListener(new ActionHandler());
        gbc.weightx = 0;
        gbc.gridx = 1;
        add(_button, gbc);
    }
    
    private class ActionHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            
            String path = _fileName.getText();
            JFileChooser chooser = path != null ? new JFileChooser(path) : new JFileChooser();
            
            chooser.setFileSelectionMode(_directoriesOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            
            if(chooser.showDialog(FileBrowseField.this, "Select") ==
                JFileChooser.APPROVE_OPTION) {
                setFile(chooser.getSelectedFile());
            }
        }
    }

    public void setFile(File file) {
        _fileName.setText(file.getAbsolutePath());
    }
    
    public File getFile() {
        String fStr = _fileName.getText();
        return new File(fStr != null ? fStr : ".");
    }
    
    public void setEnabled(boolean state) {
    	super.setEnabled(state);
    	_fileName.setEnabled(state);
    	_button.setEnabled(state);
    }
}
