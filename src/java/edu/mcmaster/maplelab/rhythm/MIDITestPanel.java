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

package edu.mcmaster.maplelab.rhythm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.mcmaster.maplelab.common.sound.*;

/**
 * Simple widget for testing MIDI subsystem.
 * 
 * 
 * @version $Revision:$
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Apr 5, 2007
 */
public class MIDITestPanel extends JPanel {
    private final JTextPane _console;
    private JSpinner _midiDevID;
    
    public static JDialog createDialog(Component parent) {
        Window window = SwingUtilities.windowForComponent(parent);
        
        JDialog win;
        if(window instanceof Frame) {
            win = new JDialog((Frame) window, "MIDI Test");
        }
        else {
            win = new JDialog((Dialog) window, "MIDI Test");
        }
        
        win.getContentPane().add(new MIDITestPanel());
        win.pack();
        win.setLocationRelativeTo(parent);
        return win;
    }
    
    public MIDITestPanel() {
        super(new BorderLayout());
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(top, BorderLayout.NORTH);
        
        
        
        top.add(new JButton(new PlayAction()));
        top.add(new JButton(new ListMidiAction()));
        top.add(new JButton(new TestTapRecord()));
        
        top.add(new JLabel("Dev:"));
        
        _midiDevID = new JSpinner(new SpinnerNumberModel(-1, -1, 15, 1));
        top.add(_midiDevID);
        
        _console = new JTextPane();
        _console.setContentType("text/plain");
        _console.setFont(new Font("monospaced", Font.PLAIN, 10));
        _console.setPreferredSize(new Dimension(500, 200));
        _console.setEditable(false);
        
        add(new JScrollPane(_console), BorderLayout.CENTER);
    }
    
    private void printf(String print, Object... args) {
        Document doc = _console.getDocument();
        try {
            doc.insertString(doc.getLength(), String.format(print+"%n", args), null);
        }
        catch (BadLocationException e1) {
            e1.printStackTrace();
        }        
    }
    
    private void print(int character) {
        Document doc = _console.getDocument();
        try {
            doc.insertString(doc.getLength(), String.valueOf((char) character), null);
        }
        catch (BadLocationException e1) {
            e1.printStackTrace();
        }        
    }    
    
    private void print(Throwable e) {
        StringWriter buf = new StringWriter();
        e.printStackTrace(new PrintWriter(buf));
        printf(buf.toString());
    }
    
    private java.util.List<Note> tune(int len) {
        java.util.List<Note> tune = new LinkedList<Note>();
        tune.add(new Note(new Pitch(NotesEnum.C, 4), len));
        tune.add(new Note(new Pitch(NotesEnum.E, 4), len));
        tune.add(new Note(new Pitch(NotesEnum.G, 4), len));
        tune.add(new Note(new Pitch(NotesEnum.C, 5), len*2));
        tune.add(new Note(new Pitch(NotesEnum.G, 4), len));
        tune.add(new Note(new Pitch(NotesEnum.C, 5), len*3));
        return tune;
    }
    
    private class PlayAction extends AbstractAction {
        public PlayAction() {
            super("Test MIDI playback");
        }

        public void actionPerformed(ActionEvent e) {
            List<Note> tune = tune(150);
            try {
                ToneGenerator.getInstance().play(tune, true);
            }
            catch (MidiUnavailableException e1) {
                print(e1);
            }
            
            printf("Played %s", tune);
        }
    }
    
    private class ListMidiAction extends AbstractAction {
        public ListMidiAction() {
            super("List MIDI devices");
        }

        public void actionPerformed(ActionEvent e) {
            MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
            for (int i = 0; i < deviceInfo.length; i++) {
                try {
                    MidiDevice device = MidiSystem.getMidiDevice(deviceInfo[i]);
                    boolean allowsInput = (device.getMaxTransmitters() != 0);
                    boolean allowsOutput = (device.getMaxReceivers() != 0);
                    printf("%d %s %s %s, %s, %s, %s", 
                        i,
                        (allowsInput ? "TRANS" : "     "),
                        (allowsOutput ? "RECIEVE" : "       "),
                        deviceInfo[i].getName(),
                        deviceInfo[i].getVendor(),
                        deviceInfo[i].getVersion(),
                        deviceInfo[i].getDescription());
                }
                catch (MidiUnavailableException ex) {
                    print(ex);
                }
            }
            if (deviceInfo.length == 0) {
                printf("No MIDI devices available");
            }
        }
    }
    
    private class TestTapRecord extends AbstractAction {
        private TapRecorder _tapRecorder;
        private Sequence _currSequence;

        private MetaEventListener _endListener = new MetaEventListener() {
            private static final int MIDI_TRACK_END = 47;
            public void meta(MetaMessage meta) {
                // Check for end of track message.
                if(meta.getType() == MIDI_TRACK_END) {
                    sessionEnded();
                }
            }
        };

        public TestTapRecord() {
            super("Test Tap Recording");
            try {
                _tapRecorder = new TapRecorder();
                _tapRecorder.setReceiver(new ConsoleReceiver());
            }
            catch (MidiUnavailableException e) {
                print(e);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            
            try {
                ToneGenerator.getInstance().getSequencer().addMetaEventListener(_endListener);
                _currSequence = ToneGenerator.getInstance().play(tune(1000), false);
                _tapRecorder.setMIDIInputID((Integer) _midiDevID.getValue());
                _tapRecorder.start(_currSequence);
                printf("Start tapping");
            }
            catch (Exception ex) {
                print(ex);
            }
        }
        private void sessionEnded() {
            printf("Stop tapping");
            _tapRecorder.stop();
            try {
                ToneGenerator.getInstance().getSequencer().removeMetaEventListener(_endListener);
                
                File out = File.createTempFile("rhythm", ".mid", new File("."));
                MidiSystem.write(_currSequence, 1, out);
                printf("Wrote tapping to file '%s'", out.getAbsoluteFile());
            }
            catch (MidiUnavailableException e) {
            }
            catch (IOException e) {
                print(e);
            }                    
            setEnabled(true);            
        }
    }
    
    private class ConsoleReceiver extends MIDIDumpReceiver {
        public ConsoleReceiver() {
            super(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    print(b);
                }
            }));
        }
    }
    
    /**
     * Test code.
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            JFrame foo = new JFrame();
            foo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            foo.getContentPane().add(new MIDITestPanel());
            foo.pack();
            foo.setVisible(true);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
