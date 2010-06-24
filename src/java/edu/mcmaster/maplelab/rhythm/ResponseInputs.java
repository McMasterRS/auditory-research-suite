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

package edu.mcmaster.maplelab.rhythm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import edu.mcmaster.maplelab.rhythm.datamodel.ConfidenceRatingEnum;
import edu.mcmaster.maplelab.rhythm.datamodel.Response;

/**
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Nov 9, 2006
 */
public class ResponseInputs extends JPanel {

    private static final long serialVersionUID = 1L;
    /**
     * @uml.property  name="top"
     */
    private JPanel top = null;
    /**
     * @uml.property  name="yes"
     */
    private JRadioButton yes = null;
    /**
     * @uml.property  name="no"
     */
    private JRadioButton no = null;
    /**
     * @uml.property  name="bottom"
     */
    private JPanel bottom = null;
    private List<JRadioButton> _confButtons = null;  //  @jve:decl-index=0:
    private ButtonGroup _confGroup;
    private ButtonGroup _timingGroup;  //  @jve:decl-index=0:
    
    /**
     * This is the default constructor
     */
    public ResponseInputs() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(300, 200);
        this.setLayout(new BorderLayout(0, 0));
        this.add(getTop(), BorderLayout.NORTH);
        this.add(getBottom(), BorderLayout.CENTER);
        
        getBottom().add(getConfidencePanel());
    }
    
    /**
     * Overridden to pass state to child widgets.
     * {@inheritDoc} 
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Enumeration[] buttons = buttonEnumeratorArray();        
        
        for(Enumeration<AbstractButton> elements : buttons) {
            while(elements.hasMoreElements()) {
                elements.nextElement().setEnabled(enabled);
            }
        }        
    }

    /**
     * This method initializes top	
     * @return  javax.swing.JPanel
     * @uml.property  name="top"
     */
    private JPanel getTop() {
        if (top == null) {
            top = new JPanel();
            top.setLayout(new FlowLayout(FlowLayout.LEFT));
            top.setBorder(BorderFactory.createTitledBorder("Accurate timing"));
            top.add(getYes(), null);
            top.add(getNo(), null);
            
            _timingGroup = new ButtonGroup();
            _timingGroup.add(getYes());
            _timingGroup.add(getNo());
        }
        return top;
    }

    /**
     * This method initializes yes	
     * @return  javax.swing.JRadioButton
     * @uml.property  name="yes"
     */
    private JRadioButton getYes() {
        if (yes == null) {
            yes = new JRadioButton();
            yes.setText("Yes");
        }
        return yes;
    }

    /**
     * This method initializes no	
     * @return  javax.swing.JRadioButton
     * @uml.property  name="no"
     */
    private JRadioButton getNo() {
        if (no == null) {
            no = new JRadioButton();
            no.setText("No");
        }
        return no;
    }

    /**
     * This method initializes bottom	
     * @return  javax.swing.JPanel
     * @uml.property  name="bottom"
     */
    private JPanel getBottom() {
        if (bottom == null) {
            bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bottom.setBorder(BorderFactory.createTitledBorder("Confidence"));
        }
        return bottom;
    }
    
    private JPanel getConfidencePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        for(JRadioButton b : getConfidenceButtons()) {
            p.add(b);
        }        
        return p;
    }
    
    private List<JRadioButton> getConfidenceButtons() {
        _confGroup = new ButtonGroup();
        if(_confButtons == null)  {
            ConfidenceRatingEnum[] ratings = ConfidenceRatingEnum.values();
            _confButtons = new ArrayList<JRadioButton>(ratings.length);
            for(ConfidenceRatingEnum c : ratings) {
                JRadioButton b = new JRadioButton(c.toString());
                b.putClientProperty(ConfidenceRatingEnum.class, c);
                _confGroup.add(b);
                _confButtons.add(b);
            }
        }
        
        return _confButtons;
    }
    
    /**
     * Clear current response selection.
     */
    public void reset() {
        deselectAll(_timingGroup);
        deselectAll(_confGroup);
    }
    
    /**
     * Deselect all the radio buttons in the given group.
     * 
     * @param group
     */
    private void deselectAll(ButtonGroup group) {

        // This add/remove dance is necessary to be able to de-select
        // all buttons in the group.
        List<AbstractButton> buttons = new ArrayList<AbstractButton>();
        
        Enumeration<AbstractButton> elements = group.getElements();
        
        while(elements.hasMoreElements()) {
            buttons.add(elements.nextElement());
        }
        
        for(AbstractButton b : buttons) {
            group.remove(b);
        }
        
        for(AbstractButton b : buttons) {
            b.setSelected(false);
            group.add(b);
        }        
        
    }
    
    /**
     * Is the "yes" option selected under accurate timing section.
     * 
     * @return
     */
    private boolean isAccurateSelected() {
        return getYes().isSelected();
    }
    
    /**
     * Get the selected confidence rating.
     */
    private ConfidenceRatingEnum getConfRating() {
        for(JRadioButton b : _confButtons) {
            if(b.isSelected()) {
                return (ConfidenceRatingEnum) b.getClientProperty(ConfidenceRatingEnum.class);
            }
        }
        
        return null;
    }
    
    /**
     * Determine if both response options have been selected.
     */
    public boolean isResponseComplete() {
        return _timingGroup.getSelection() != null && _confGroup.getSelection() != null;
    }
    
    /**
     * Get the response selections.
     * 
     * @return response data, or null if response not complete.
     */
    public Response getResponse() {
        if(!isResponseComplete()) return null;
        
        Response retval = new Response(isAccurateSelected(), getConfRating());
        
        return retval;
    }
    
    private Enumeration[] buttonEnumeratorArray() {
        return new Enumeration[] {
            _timingGroup.getElements(),
            _confGroup.getElements()
        };
    }
    
    public void addActionListener(ActionListener l) {
        Enumeration[] buttons = buttonEnumeratorArray();
        
        for(Enumeration<AbstractButton> elements : buttons) {
            while(elements.hasMoreElements()) {
                elements.nextElement().addActionListener(l);
            }
        }        
    }
    
    public void removeActionListener(ActionListener l) {
        Enumeration[] buttons = buttonEnumeratorArray();
        
        for(Enumeration<AbstractButton> elements : buttons) {
            while(elements.hasMoreElements()) {
                elements.nextElement().removeActionListener(l);
            }
        } 
    }
}
