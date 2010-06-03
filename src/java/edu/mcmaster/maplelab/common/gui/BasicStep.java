/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id:BasicStep.java 399 2008-01-11 22:20:55Z sfitch $
*/
package edu.mcmaster.maplelab.common.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import edu.mcmaster.maplelab.common.LogContext;


/**
 * Abstract base class for a step in the experiment.
 * @version   $Revision:399 $
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   May 10, 2006
 */
public abstract class BasicStep extends JPanel {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -6753520521743872624L;
	private final JLabel _title;
    private final boolean _showPrevNext;
    private JTextPane _instructions;
    private StepManager _stepManager;
    private PrevNextButtons _prevNextButtons;
    private JPanel _center;
    private JPanel _controls;

    /**
     * This is the default constructor
     */
    public BasicStep() {
        this(true);
    }
    
    public BasicStep(boolean showPrevNext) {
        super();
        _showPrevNext = showPrevNext;

        _title = new JLabel();
        _title.setText("Title");
        _title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
        _title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        _title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        setLayout(new BorderLayout());
        setSize(300, 200);
        if(_showPrevNext) {
            add(getPrevNextButtons(), java.awt.BorderLayout.SOUTH);
        }
        add(_title, java.awt.BorderLayout.NORTH);
        add(getCenter(), java.awt.BorderLayout.CENTER);
        getPrevNextButtons().setStepManager(new StepManagerProxy());
        
        // When this component is made active we initialize the trial controller.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // FIXME: This doesn't get invoked on the first panel on a card layout.
                onVisible();
            }
        });      
    }
    
    /**
     * Called when component goes from hidden to visible state.
     */
    protected void onVisible() {
    }
    
    /**
     * Set delegate responsible for ordering screens.
     */
    public void setStepManager(StepManager mgr) {
        _stepManager = mgr;
    }
    
    /**
     * Get the delegate responsible for ordering screens.
     */
    public StepManager getStepManager() {
        return _stepManager;
    }
    
    /**
     * Set the title string.
     */
    public void setTitleText(String title) {
        String text = "<html>";
        if(title != null) {
            text += title;
        }

        _title.setText(text);
    }

    /**
     * Set the font size of the instructions text. Can be overridden via
     * HTML markup.
     */
    public void setInstructionTextFontSize(int points) {
        setDefaultFontSize(_instructions, points);
    }
    
    /**
     * Utility funciton for setting the default size of an text pane.
     * 
     * @param textPane formatted text display widget 
     * @param points font size in points.
     */
    public static void setDefaultFontSize(JEditorPane textPane, int points) {
        Font curr = textPane.getFont();
        Font next = curr.deriveFont((float) points);
        textPane.setFont(next);
        
        EditorKit ekit = textPane.getEditorKit();
        if(ekit instanceof HTMLEditorKit) {
            StyleSheet styleSheet = ((HTMLEditorKit)ekit).getStyleSheet();
            // Still trying to figure out how to get default font size to apply to all
            styleSheet.addRule(String.format("p { font-size: %dpt;}", points));
            styleSheet.addRule(String.format("div { font-size: %dpt;}", points));
            styleSheet.addRule(String.format("body { font-size: %dpt;}", points));
        }
    }
    
    /**
     * Set the instruction text
     */
    public void setInstructionText(String instructions, Object... formatArgs) {
        _instructions.setText(instructions != null ? String.format(instructions, formatArgs) : null);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getPrevNextButtons().setEnabled(enabled);
    }

    /**
     * This method initializes prevNextButtons	
     * @return  edu.mcmaster.maplelab.pm.editors.PrevNextButtons
     * @uml.property  name="prevNextButtons"
     */
    protected PrevNextButtons getPrevNextButtons() {
        if (_prevNextButtons == null) {
            _prevNextButtons = new PrevNextButtons();
        }
        return _prevNextButtons;
    }

    /**
     * This method initializes jPanel	
     * @return  javax.swing.JPanel
     * @uml.property  name="center"
     */
    private JPanel getCenter() {
        if (_center == null) {
            _center = new JPanel();
            _center.setLayout(new BorderLayout());
            _center.add(getContentPanel(), java.awt.BorderLayout.CENTER);
            _center.add(getInstructions(), java.awt.BorderLayout.NORTH);
        }
        return _center;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    protected JPanel getContentPanel() {
        if (_controls == null) {
            _controls = new JPanel();
            _controls.setLayout(new BorderLayout());
        }
        return _controls;
    }

    /**
     * This method initializes jTextPane	
     * @return  javax.swing.JTextPane
     * @uml.property  name="instructions"
     */
    private JTextPane getInstructions() {
        if (_instructions == null) {
            _instructions = new JTextPane();
            _instructions.setOpaque(false);
            _instructions.setEditorKit(new HTMLEditorKit());
            _instructions.setEditable(false);
            _instructions.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            _instructions.addHyperlinkListener(new HyperlinkHandler());
            _instructions.setMinimumSize(new Dimension(600, 300));
        }
        return _instructions;
    }

    /**
     * Overridable method for determining if the user is allowed to go back
     * a step in the experiment.
     */
    protected boolean canGoBack() {
        if(_stepManager != null) {
            return _stepManager.hasPrevious();
        }
        return false;
    }
    
    /**
     * Overridable method for handling a request to go to the next step.
     * Defaults to calling StepManager.next().
     */
    protected void doNext() {
        if(_stepManager != null) {
            _stepManager.next();
        }        
    }
    
    private class StepManagerProxy implements StepManager {
        public void next() {
            doNext();
        }

        public boolean hasNext() {
            if(_stepManager != null) {
                return _stepManager.hasNext();
            }
            return false;
        }

        public void previous() {
            if(_stepManager != null) {
                _stepManager.previous();
            }
        }

        public boolean hasPrevious() {
            return canGoBack();
        }
    }
    
    
    private class HyperlinkHandler implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
            
            URL url = e.getURL();
            
            // TODO: refactor
            // Copied from public domain Bare Bones Browser Launch    
            try {
                String osName = System.getProperty("os.name");
                if (osName.startsWith("Mac OS")) {
                    Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                    Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[] {String.class});
                    
                    LogContext.getLogger().finer(String.format(
                        "calling: com.apple.eio.FileManager.openURL(\"%s\")", url));
                    openURL.invoke(null, new Object[] {url.toString()});                   
                }
                else if (osName.startsWith("Windows")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                }
            }
            catch (Throwable ex) {
                LogContext.getLogger().log(Level.WARNING, "Couldn't open URL", ex);
            }            
        }
    }
}
