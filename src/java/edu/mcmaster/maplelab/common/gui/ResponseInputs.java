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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceLevel;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

/**
 * Component that pairs a binary question (yes/no, true/false, chicken/egg, etc) with
 * a range of confidence values for user input.
 *
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Nov 9, 2006
 */
public class ResponseInputs extends JPanel {
    private static final long serialVersionUID = 1L;
    
    /** Response interpreter. */
    private final ResponseParameters<?> _responseParams;
    
    /** Top panel. */
    private JPanel _top = null;
    /** Bottom panel. */
    private JPanel _bottom = null;
    /** Confidence buttons and group. */
    private List<JRadioButton> _confButtons = null;
    private ButtonGroup _confGroup;
    /** Answer buttons and group. */
    private List<JRadioButton> _answerButtons = null;
    private ButtonGroup _answerGroup;
    
    /**
     * Default constructor sets the question asked and labels for the two
     * possible choices as indicated.
     */
    public ResponseInputs(ResponseParameters<?> interpreter) {
        super();
        _responseParams = interpreter;
        
        setSize(300, 200);
        setLayout(new BorderLayout(0, 0));
        add(getTop(), BorderLayout.NORTH);
        add(getBottom(), BorderLayout.CENTER);
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
        if (_top == null) {
            _top = new JPanel();
            _top.setLayout(new FlowLayout(FlowLayout.LEFT));
            _top.setBorder(BorderFactory.createTitledBorder(_responseParams.getQuestion()));
            for (JRadioButton b : getAnswerButtons()) {
                _top.add(b);
            }
        }
        return _top;
    }
    
    private List<JRadioButton> getAnswerButtons() {
        _answerGroup = new ButtonGroup();
        if (_answerButtons == null)  {
            Answer[] answerVals = _responseParams.getAnswers();
            _answerButtons = new ArrayList<JRadioButton>(answerVals.length);
            for (Answer a : answerVals) {
                JRadioButton b = new JRadioButton(a.toString());
                b.putClientProperty(Answer.class, a);
                _answerGroup.add(b);
                _answerButtons.add(b);
            }
        }
        
        return _answerButtons;
    }

    /**
     * This method initializes bottom	
     * @return  javax.swing.JPanel
     * @uml.property  name="bottom"
     */
    private JPanel getBottom() {
        if (_bottom == null) {
            _bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            _bottom.setBorder(BorderFactory.createTitledBorder("Confidence"));
            _bottom.add(getConfidencePanel());
        }
        return _bottom;
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
        if (_confButtons == null)  {
            ConfidenceLevel[] ratings = _responseParams.getConfidenceLevels();
            _confButtons = new ArrayList<JRadioButton>(ratings.length);
            for (ConfidenceLevel c : ratings) {
                JRadioButton b = new JRadioButton(c.toString());
                b.putClientProperty(ConfidenceLevel.class, c);
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
        deselectAll(_answerGroup);
        deselectAll(_confGroup);
    }
    
    /**
     * Deselect all the radio buttons in the given group.
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
     * Get the selected confidence rating.
     */
    private ConfidenceLevel getConfRating() {
        for (JRadioButton b : _confButtons) {
            if (b.isSelected()) {
                return (ConfidenceLevel) b.getClientProperty(ConfidenceLevel.class);
            }
        }
        
        return null;
    }
    
    /**
     * Get the selected answer.
     */
    private Answer getAnswerInput() {
        for (JRadioButton b : _answerButtons) {
            if (b.isSelected()) {
                return (Answer) b.getClientProperty(Answer.class);
            }
        }
        
        return null;
    }
    
    /**
     * Determine if both response options have been selected.
     */
    public boolean isResponseComplete() {
        return _answerGroup.getSelection() != null && _confGroup.getSelection() != null;
    }
    
    /**
     * Get the response selections.
     * 
     * @return response data, or null if response not complete.
     */
    public Response getResponse() {
        if (!isResponseComplete()) return null;
        
        Response retval = new Response(getAnswerInput(), getConfRating());
        
        return retval;
    }
    
    private Enumeration[] buttonEnumeratorArray() {
        return new Enumeration[] {
            _answerGroup.getElements(),
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
