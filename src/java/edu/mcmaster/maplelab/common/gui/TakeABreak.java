/*
* Copyright (C) 2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: TakeABreak.java 474 2009-03-20 17:53:30Z bhocking $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import edu.mcmaster.maplelab.common.datamodel.*;


/**
 * Screen requiring subject to wait a specified period of time before continuing.
 * 
 * @version $Revision: 474 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Mar 2, 2007
 */
public class TakeABreak extends BasicStep {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -7362002867022118420L;

	enum ConfigKeys {
        takeABreakTitle,
        takeABreakText,
        takeABreakTime
    }
    
    private long _timeMilli = 60 * 1000;
    private boolean _debug = false;
    private long _startTime = -1;
    private Timer _countdown = null;
    private JLabel _timeDisplay;

    public TakeABreak(StepManager mgr, Session<? extends Block<?,?>, ? extends Trial<?>> session) {
        super(true);
        
        setStepManager(mgr);
        setTitleText(session.getString(ConfigKeys.takeABreakTitle, null));
        setInstructionText(session.getString(ConfigKeys.takeABreakText, null));

        _timeMilli = (long) session.getFloat(ConfigKeys.takeABreakTime, 1) * 60 * 1000;
        
        // Save debug state so we can zip through the wait time.
        _debug = session.isDebug();
        
        _timeDisplay = new JLabel();
        _timeDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        _timeDisplay.setVerticalAlignment(SwingConstants.CENTER);
        _timeDisplay.setFont(new Font("SansSerif", Font.BOLD, 24));
        
        if(_debug) {
            getContentPanel().add(new JLabel("Debug mode detected. Time is accellerated.", JLabel.CENTER), BorderLayout.NORTH);
        }
        
        getContentPanel().add(_timeDisplay, BorderLayout.CENTER);
    }
    
    @Override
    protected boolean canGoBack() {
        return false;
    }
    
    @Override
    protected void onVisible() {
        setEnabled(false);
        
        if(_countdown != null) {
            _countdown.stop();
            _countdown = null;
        }
        
        int delay = _debug ? 1 : 1000;
        _countdown = new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_startTime < 0) {
                    _startTime = System.currentTimeMillis();
                }
                
                long elapsed = System.currentTimeMillis() - _startTime;
                
                if(_debug) {
                    elapsed *= 100;
                }
                
                long remain = _timeMilli - elapsed;
                
                // In seconds, clamped
                remain = Math.max(remain/1000, 0);
                
                if(remain <= 0) {
                    setEnabled(true);
                    _countdown.stop();
                    _countdown = null;
                }
                
                _timeDisplay.setText(String.format("%02d:%02d",
                    remain/60, remain % 60));
            }
        });
        _countdown.setInitialDelay(0);
        _countdown.start();
    }
    
    

}
