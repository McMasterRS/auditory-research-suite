/*
* Copyright (C) 2006-2008 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: SimpleSetupScreen.java 468 2008-12-04 20:42:37Z bhocking $
*/

package edu.mcmaster.maplelab.common.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.*;


/**
 * Setup editor for non-applet mode.
 * 
 * @version   $Revision: 468 $
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Dec 4, 2006
 */
public class Questionnaire extends JPanel  {
    /**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = 8595156800591101562L;

    private QuestionnaireResponse[] _responses = null;
    private final ButtonGroup[] _options;
    
    public Questionnaire() {
        this(getDefaultQuestions());
    }
    
    public Questionnaire(InputStream is) {
        this(parseInputStream(is));
    }

    public Questionnaire(Map<String,String[]> questions) {
        super(new VerticalFlowLayout());
        
        JPanel NA = new JPanel();
        NA.add(new JLabel("If you don't feel comfortable answering a question, leave it blank."));
        add(NA);

        JPanel main = new JPanel(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder());
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.gridx = 0;
        labelGBC.anchor = GridBagConstraints.EAST;
        
        GridBagConstraints fieldGBC = new GridBagConstraints();
        fieldGBC.anchor = GridBagConstraints.EAST;
        fieldGBC.gridx = 1;
        fieldGBC.weightx = 1;
        fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        
        int numOptions = questions.keySet().size();
        _options = new ButtonGroup[numOptions];
        _responses = new QuestionnaireResponse[numOptions];
        int i = 0;
        for (String key: questions.keySet()) {
            main.add(new JLabel(key), labelGBC);
            String[] options = questions.get(key);
            _options[i] = new ButtonGroup();
            _responses[i] = new QuestionnaireResponse(key, "NA"); // default
            JPanel buttonPanel = new JPanel();
            for (String opt: options) {
                JRadioButton curOption = new JRadioButton(opt);
                _options[i].add(curOption);
                buttonPanel.add(curOption);
            }
            i++;
            main.add(buttonPanel, fieldGBC);
        }
                
        // Create buffer space between standard fields and fields
        // user may add later. Also provides better distribution of extra space.
        labelGBC.weighty = 1;
        fieldGBC.weighty = 1;
        main.add(new JLabel(), labelGBC);
        main.add(new JLabel(), labelGBC);
        main.add(new JLabel(), fieldGBC);        
        main.add(new JLabel(), fieldGBC);
        labelGBC.weighty = 0;
        fieldGBC.weighty = 0;
        add(main);
    }
    
    private static Map<String, String[]> getDefaultQuestions() {
        Map<String,String[]> defaultQuestions = new LinkedHashMap<String,String[]>();
        String[] sexes = {"M", "F"};
        defaultQuestions.put("Sex", sexes);
        String[] YN = {"Y", "N"};
        defaultQuestions.put("Hispanic", YN);
        defaultQuestions.put("Asian", YN);
        defaultQuestions.put("Pacific Islander", YN);
        return defaultQuestions;
    }
    
    private static Map<String, String[]> parseInputStream(InputStream is) {
        Map<String,String[]> parsedQuestions = new LinkedHashMap<String,String[]>();
        Properties props = new Properties();
        try {
            props.load(is);
        }
        catch (IOException ex) {
            System.err.println("Error reading configuration file");
            return null;
        }
        String[] questionSet = ((String) props.get("questionnaire")).split(",");
        for (String q: questionSet) {
            String[] quesResp = ((String) props.get(q)).split(",");
            int numQuesResp = quesResp.length;
            String[] responses = new String[numQuesResp-1];
            for(int i=1;i<numQuesResp; i++) {
                responses[i-1] = quesResp[i];
            }
            parsedQuestions.put(quesResp[0], responses);
        }
        return parsedQuestions;
    }

    /**
     * Display the screen in a dialog, blocking until OK pressed.
     */
    public void display() {
        final JDialog d = new JDialog((Frame)null, "Questionnaire", true);
        d.getContentPane().add(this);
        CloseButton.createClosePanel(d, new QuestionnaireListener());
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        
        d.dispose();
    }
    
    public QuestionnaireResponse[] getResponses() {
        return _responses;
    }
    
    private class QuestionnaireListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int numOptions = _options.length;
            for(int i=0; i<numOptions; i++) {
                Enumeration<AbstractButton> enumAB = _options[i].getElements();
                while (enumAB.hasMoreElements()) {
                    AbstractButton ab = enumAB.nextElement();
                    if (ab.isSelected()) {
                        _responses[i].setResponse(ab.getText());
                        break;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Questionnaire q = new Questionnaire();
        q.display();
        QuestionnaireResponse[] responses = q.getResponses();
        for (QuestionnaireResponse s: responses) {
            System.out.println(s.getQuestion() + ": " + s.getResponse());
        }
    }
    
    public class QuestionnaireResponse {
        private final String _question;
        private String _response;
        protected QuestionnaireResponse(String question, String response) {
            _question = question;
            _response = response;
        }
        public String getQuestion() { return _question; }
        public String getResponse() { return _response; }
        public void setResponse(String response) { _response = response; }
    }
}
