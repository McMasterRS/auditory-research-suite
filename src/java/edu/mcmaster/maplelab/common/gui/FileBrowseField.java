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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.filechooser.FileFilter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;


/**
 * Widget encapsulating a text field for file path and browse button.
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Feb 26, 2007
 */
public class FileBrowseField extends JPanel {
    private final boolean _directoriesOnly;
    
    private final javax.swing.filechooser.FileFilter _filter;
    
    private final JTextField _fileName;

	private JButton _button;

    private boolean _missingFileIndicator = true;

    private ColorUpdater _colorUpdater;
    
    private final PropertyChangeSupport _PCS = new PropertyChangeSupport(this);
    
    public FileBrowseField(boolean directoriesOnly, String...extensions) {
        super(new MigLayout("insets 0 0 0 0, nogrid, fill"));
        
        _directoriesOnly = directoriesOnly;
        if (extensions != null && extensions.length > 0) {
        	_filter = new FileBrowseFilter(extensions);
        }
        else {
        	_filter = null;
        }
        
        _fileName = new JTextField();
        _colorUpdater = new ColorUpdater();
        _fileName.addFocusListener(_colorUpdater);
        _fileName.addActionListener(_colorUpdater);
        add(_fileName, "growx 200");
        
        _button = new JButton("Browse...");
        add(_button);
        _button.addActionListener(new DefaultBrowseHandler());
    }
    
    private class DefaultBrowseHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            
            String path = _fileName.getText();
            JFileChooser chooser = path != null ? new JFileChooser(path) : new JFileChooser();
            
            chooser.setFileSelectionMode(_directoriesOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            if (_filter != null) {
            	chooser.setFileFilter(_filter);
            }
            
            if (chooser.showOpenDialog(FileBrowseField.this) == JFileChooser.APPROVE_OPTION) {
            	setFile(chooser.getSelectedFile());
            }
        }
    }
    
    public void addFileChoiceChangeListener(PropertyChangeListener listener) {
    	_PCS.addPropertyChangeListener(listener);
    }
    
    public void setMissingFileIndicator(boolean state) {
        _missingFileIndicator = state;
    }

    public void setFile(File file) {
    	String oldFileAbsPath = _fileName.getText();
        _fileName.setText(file.getAbsolutePath());
        _colorUpdater.updateColor();
        
        // Pass the new chosen directory to listeners.
        _PCS.firePropertyChange("updated file", oldFileAbsPath, file);
    }
    
    public void setText(String text) {
    	_fileName.setText(text);
    }
    
    public File getFile() {
        String fStr = _fileName.getText();
        File retval = new File(fStr != null ? fStr : ".");
        if (_filter == null) {
        	return retval;
        }
        
        return _filter.accept(retval) ? retval : null;
    }
    
    public void setEnabled(boolean state) {
    	super.setEnabled(state);
    	_fileName.setEnabled(state);
    	_button.setEnabled(state);
    }
    
    private class ColorUpdater implements FocusListener, ActionListener {
        public void updateColor() {
            if(!_missingFileIndicator) return;
            
            File file = getFile();
            _fileName.setForeground(file == null || file.exists() ? SystemColor.textText : Color.red);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            updateColor();
        }

        @Override
        public void focusGained(FocusEvent arg0) {
            updateColor();
        }

        @Override
        public void focusLost(FocusEvent arg0) {
            updateColor();
        }
    }
    
    private class FileBrowseFilter extends FileFilter {
    	private final List<String> _ext;
    	
    	public FileBrowseFilter(String...strings) {
    		_ext = new ArrayList<String>();
    		for (String s : strings) {
    			if (s != null && !s.isEmpty()) {
    				_ext.add(s.startsWith(".") ? s : "." + s);
    			}
    		}
    	}
    	
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {return true;}
			
			for (String ext : _ext) {
				if (f.getName().endsWith(ext)) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public String getDescription() {
			String retval = "";
			for (String ext : _ext) {
				retval = retval + "*" + ext + ", ";
			}
			retval = retval.substring(0, retval.length() - 2);
			
			return retval;
		}
    }
}
