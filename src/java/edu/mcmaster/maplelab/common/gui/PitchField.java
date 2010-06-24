/*
* Copyright (C) 2006 University of Virginia
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

import java.awt.GridLayout;

import javax.swing.*;

import edu.mcmaster.maplelab.common.sound.NotesEnum;
import edu.mcmaster.maplelab.common.sound.Pitch;

/**
 * Widget for selecting a chromatic pitch.
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  May 10, 2006
 */
public class PitchField extends JPanel {

    /**
     * @uml.property  name="note"
     */
    private JComboBox _note = null;
    /**
     * @uml.property  name="octave"
     */
    private JComboBox _octave = null;
    private DefaultComboBoxModel _noteOptions = null;  //  @jve:decl-index=0:visual-constraint=""
    private DefaultComboBoxModel _octaveOptions = null;  //  @jve:decl-index=0:visual-constraint=""

    /**
     * This is the default constructor
     */
    public PitchField() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(1);
        gridLayout.setHgap(4);
        this.setLayout(gridLayout);
        this.setSize(162, 19);
        this.setPreferredSize(null);
        this.add(getJComboBox(), null);
        this.add(getJComboBox1(), null);
    }
    
    public void setValue(Pitch value) {
        _note.setSelectedItem(value.getNote());
        _octave.setSelectedItem(new Integer(value.getOctave()));
    }
    
    public Pitch getValue() {
        return new Pitch(getNote(), getOctave());
    }

    /**
     * Get the selected note value.
     * @uml.property  name="note"
     */
    public NotesEnum getNote() {
        return (NotesEnum) _note.getSelectedItem();
    }
    
    /**
     * Get the selected octave.
     * @uml.property  name="octave"
     */
    public int getOctave() {
        Integer val = (Integer) _octave.getSelectedItem();
        return val.intValue();
    }

    /**
     * This method initializes jComboBox	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getJComboBox() {
        if (_note == null) {
            _note = new JComboBox();
            _note.setModel(getNoteOptionsModel());
        }
        return _note;
    }

    /**
     * This method initializes jComboBox1	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getJComboBox1() {
        if (_octave == null) {
            _octave = new JComboBox();
            _octave.setModel(getOctaveOptionsModel());
        }
        return _octave;
    }

    private DefaultComboBoxModel getNoteOptionsModel() {
        if (_noteOptions == null) {
            _noteOptions = new DefaultComboBoxModel(NotesEnum.values());
            _noteOptions.setSelectedItem(NotesEnum.C);
        }
        return _noteOptions;
    }

    private DefaultComboBoxModel getOctaveOptionsModel() {
        if (_octaveOptions == null) {

            _octaveOptions = new DefaultComboBoxModel(new Integer[] {
                new Integer(-1), new Integer(0), new Integer(1),
                new Integer(2), new Integer(3), new Integer(4),
                new Integer(5), new Integer(6), new Integer(7),
                new Integer(8), new Integer(9)
            });
            _octaveOptions.setSelectedItem(new Integer(4));
        }
        return _octaveOptions;
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
