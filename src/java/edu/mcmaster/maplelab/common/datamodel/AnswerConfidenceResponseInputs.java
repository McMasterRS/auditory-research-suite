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

package edu.mcmaster.maplelab.common.datamodel;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import edu.mcmaster.maplelab.common.gui.ResponseInputs;

/**
 * Component that pairs a binary question (yes/no, true/false, chicken/egg, etc) with
 * a range of confidence values for user input.
 *
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Nov 9, 2006
 */
public class AnswerConfidenceResponseInputs extends ResponseInputs<ConfidenceResponse> {
	private static final long serialVersionUID = 1L;
	
	
	/** Confidence buttons and group. */
    private List<JRadioButton> _confButtons;
    private ButtonGroup _confGroup;
    /** Answer buttons and group. */
    private List<JRadioButton> _answerButtons;
    private ButtonGroup _answerGroup;
    
    private Map<String, AbstractButton> _hotkeysMap;
    
    /**
     * Default constructor sets the question asked and labels for the two
     * possible choices as indicated.
     */
    public AnswerConfidenceResponseInputs(ResponseParameters<?, ConfidenceLevel> interpreter) {
        this(true, false, interpreter);
    }
    
    /**
     * Default constructor sets the question asked and labels for the
     * possible choices as indicated.
     */
	public AnswerConfidenceResponseInputs(boolean vertical, boolean enableKeyEvents, 
												ResponseParameters<?, ConfidenceLevel> interpreter) {
        super(vertical, enableKeyEvents, interpreter);
        
        if (!interpreter.isDiscrete()) {
        	throw new IllegalArgumentException("Can only handle discrete response parameters.");
        }
    }

    @Override
    protected JPanel createTop() {
    	// check answer size and adjust intelligently
    	boolean vertical = false;
    	int insetAdjust = 20;
        List<JRadioButton> answerButtons = getAnswerButtons();
    	for (JRadioButton b : answerButtons) {
    		vertical = vertical || b.getPreferredSize().width + insetAdjust > answerButtons.size();
    	}
    	
        JPanel top = new JPanel();
        top.setLayout(vertical ? new MigLayout("flowy") : new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createTitledBorder(getResponseParams()[0].getQuestion()));
        for (JRadioButton b : answerButtons) {
            top.add(b, vertical ? "north" : "");
        }
        
        return top;
    }

	@Override
	protected JPanel createBottom() {
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBorder(BorderFactory.createTitledBorder("Confidence"));
        bottom.add(getConfidencePanel());
        return bottom;
	}
    
    /**
     * Set the first answer that starts with the given character (upper or lower)
     * to be the selected value.  Or, if the given character is a digit, the corresponding
     * confidence value is set to be the selected value.
     */
    @Override
    public void setSelection(KeyEvent e) {    	
    	AbstractButton button = _hotkeysMap.get(String.valueOf(e.getKeyChar()));
    	if (button != null) {
    		button.setSelected(true);
    	}
    }
    
    @Override
    public void reset() {
        deselectAll(_answerGroup);
        deselectAll(_confGroup);
    }
    
    @Override
    public boolean isResponseComplete() {
        return _answerGroup.getSelection() != null && _confGroup.getSelection() != null;
    }
    
    @Override
    public void addActionListener(ActionListener l) {
    	List<Enumeration<AbstractButton>> buttons = buttonEnumeratorList();
        
        for(Enumeration<AbstractButton> elements : buttons) {
            while(elements.hasMoreElements()) {
                elements.nextElement().addActionListener(l);
            }
        }        
    }
    
    @Override
    public void removeActionListener(ActionListener l) {
    	List<Enumeration<AbstractButton>> buttons = buttonEnumeratorList();
        
        for(Enumeration<AbstractButton> elements : buttons) {
            while(elements.hasMoreElements()) {
                elements.nextElement().removeActionListener(l);
            }
        } 
    }

	@Override
	protected void setEnabledState(boolean enabled) {
		List<Enumeration<AbstractButton>> buttons = buttonEnumeratorList();        
        
        for(Enumeration<AbstractButton> elements : buttons) {
            while(elements.hasMoreElements()) {
                elements.nextElement().setEnabled(enabled);
            }
        }  
	}

	@Override
	protected ConfidenceResponse getCurrentResponse() {
		return new ConfidenceResponse(getAnswerInput(), getConfRating());
	}
    
    private JPanel getConfidencePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        for(JRadioButton b : getConfidenceButtons()) {
            p.add(b);
        }        
        return p;
    }
    
    private List<JRadioButton> getAnswerButtons() {
        _answerGroup = new ButtonGroup();
        if (_answerButtons == null)  {
            BinaryAnswer[] answerVals = getResponseParams()[0].getAnswers();
            _answerButtons = new ArrayList<JRadioButton>(answerVals.length);
            for (BinaryAnswer a : answerVals) {
                JRadioButton b = new JRadioButton(a.toString());
                b.putClientProperty(BinaryAnswer.class, a);
                _answerGroup.add(b);
                _answerButtons.add(b);
                saveHotkeyMapping(a.getHotkey(), b);
            }
        }
        
        return _answerButtons;
    }
    
    private List<JRadioButton> getConfidenceButtons() {
        _confGroup = new ButtonGroup();
        if (_confButtons == null)  {
            ConfidenceLevel[] ratings = (ConfidenceLevel[]) getResponseParams()[0].getDiscreteValues();
            _confButtons = new ArrayList<JRadioButton>(ratings.length);
            for (ConfidenceLevel c : ratings) {
                JRadioButton b = new JRadioButton(c.toString());
                b.putClientProperty(ConfidenceLevel.class, c);
                _confGroup.add(b);
                _confButtons.add(b);
                saveHotkeyMapping(c.getHotkey(), b);
            }
        }
        
        return _confButtons;
    }
    
    private void saveHotkeyMapping(String key, AbstractButton value) {
    	if (_hotkeysMap == null) {
    		_hotkeysMap = new HashMap<String, AbstractButton>();
    	}
    	_hotkeysMap.put(key, value);
    }
    
    /**
     * Set the given answer index to be the selected value.
     */
    public void setAnswerSelection(int index) {
    	if (_answerButtons.size() > index) {
    		_answerButtons.get(index).setSelected(true);
    	}
    }
    
    /**
     * Set the given confidence index to be the selected value.
     */
    public void setConfidenceSelection(int index) {
    	if (_confButtons.size() > index) {
    		_confButtons.get(index).setSelected(true);
    	}
    }
    
    /**
     * Get the selected answer.
     */
    private BinaryAnswer getAnswerInput() {
        for (JRadioButton b : _answerButtons) {
            if (b.isSelected()) {
                return (BinaryAnswer) b.getClientProperty(BinaryAnswer.class);
            }
        }
        
        return null;
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
    
    private List<Enumeration<AbstractButton>> buttonEnumeratorList() {
    	List<Enumeration<AbstractButton>> retval = new ArrayList<Enumeration<AbstractButton>>(2);
        retval.add(_confGroup.getElements());
        retval.add(_answerGroup.getElements());
        return retval;
    }
}
