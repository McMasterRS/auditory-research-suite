package edu.mcmaster.maplelab.common.gui;

import java.awt.AWTEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public abstract class ResponseInputs<T> extends JPanel {
	private static final long serialVersionUID = 1L;
    
    /** Response interpreter. */
    private final ResponseParameters<?, ?>[] _responseParams;
    
    /** Top panel. */
    private JPanel _top = null;
    /** Bottom panel. */
    private JPanel _bottom = null;
    
    /**
     * Default constructor sets the question asked and labels for the two
     * possible choices as indicated.
     */
    public ResponseInputs(ResponseParameters<?, ?>... interpreter) {
        this(true, false, interpreter);
    }
    
    /**
     * Default constructor sets the question asked and labels for the
     * possible choices as indicated.
     */
    public ResponseInputs(boolean vertical, boolean enableKeyEvents, 
    		ResponseParameters<?, ?>... interpreter) {
        super();
        _responseParams = interpreter;
        
        setSize(300, 200);
        
        MigLayout layout = vertical ? 
        		new MigLayout("insets 0, fill") : new MigLayout("insets 0, fill", "[]0px[]", "");
        setLayout(layout);
        add(getTop(), vertical ? "north" : "sgy, grow");
        add(getBottom(), vertical ? "" : "sgy, grow");
        
        if (enableKeyEvents) enableEvents(AWTEvent.KEY_EVENT_MASK);
    }
    
    /**
     * Get the response parameters object(s).
     */
    protected ResponseParameters<?, ?>[] getResponseParams() {
    	return _responseParams;
    }
    
    /**
     * Overridden to pass state to child widgets.
     * {@inheritDoc} 
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setEnabledState(enabled);    
    }

    /**
     * Get the top panel.
     */
    private JPanel getTop() {
        if (_top == null) {
            _top = createTop();
        }
        return _top;
    }

    /**
     * Get the bottom panel
     */
    private JPanel getBottom() {
        if (_bottom == null) {
            _bottom = createBottom();
        }
        return _bottom;
    }
    
    /**
     * Get the response selections.
     * 
     * @return response data, or null if response not complete.
     */
    public T getResponse() {
        if (!isResponseComplete()) return null;
        
        T retval = getCurrentResponse();
        
        return retval;
    }
    
    /**
     * Set the enabled state of all items in this widget.
     */
    protected abstract void setEnabledState(boolean enabled);
    
    /**
     * This method initializes top	
     * @return  javax.swing.JPanel
     * @uml.property  name="top"
     */
    protected abstract JPanel createTop();
    
    /**
     * This method initializes bottom	
     * @return  javax.swing.JPanel
     * @uml.property  name="bottom"
     */
    protected abstract JPanel createBottom();
    
    /**
     * Set the selection based on the given key event, if enabled.
     */
    public abstract void setSelection(KeyEvent e);
    
    /**
     * Clear current response selection.
     */
    public abstract void reset();
    
    /**
     * Determine if all response options have been selected.
     */
    public abstract boolean isResponseComplete();
    
    /**
     * Get the current response value.
     */
    protected abstract T getCurrentResponse();
    
    /**
     * Add an action listener.
     */
    public abstract void addActionListener(ActionListener l);
    
    /**
     * Remove an action listener.
     */
    public abstract void removeActionListener(ActionListener l);
}
