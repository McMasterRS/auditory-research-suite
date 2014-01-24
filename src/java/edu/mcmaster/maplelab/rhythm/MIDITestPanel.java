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
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.miginfocom.swing.MigLayout;

import edu.mcmaster.maplelab.common.gui.CloseButton;
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
    private static JDialog _dialog = null;
    private DeviceSelectionPanel _devs;
    private JScrollPane _scrollPane;
    
    public static JDialog createDialog(Component parent) {
        Window window = SwingUtilities.windowForComponent(parent);
        
        if (_dialog == null) {
        	if(window instanceof Frame) {
                _dialog = new JDialog((Frame) window, "MIDI Devices and Testing");
            }
            else {
                _dialog = new JDialog((Dialog) window, "MIDI Devices and Testing");
            }
            
            _dialog.getContentPane().add(new MIDITestPanel());
            _dialog.pack();
            _dialog.setLocationRelativeTo(parent);
        }
        
        return _dialog;
    }
    
    public MIDITestPanel() {
        super(new BorderLayout());
        
        JPanel top = new JPanel(new MigLayout("fill, insets 0", "5px[][][]push[]5px", "5px[]10px[]"));
        add(top, BorderLayout.NORTH);
        
        _devs = new DeviceSelectionPanel();
        top.add(_devs, "span 4, grow, wrap");
        
        top.add(new JButton(new ListMidiAction()));
        top.add(new JButton(new PlayAction()));
        top.add(new JButton(new TestTapRecord()), "wrap");
        
        /*JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEtchedBorder());
        p.add(new JButton(new TestTapRecord()));
        p.add(new JLabel("Device:"));
        _midiDevID = new JSpinner(new SpinnerNumberModel(-1, -1, 15, 1));
        //TODO: set a custom Document object for the underlying text field
        // and limit inputs to valid integer-relevant characters?
        p.add(_midiDevID);
        top.add(p);*/
        
        _console = new JTextPane();
        _console.setContentType("text/plain");
        _console.setFont(new Font("monospaced", Font.PLAIN, 10));
        _console.setPreferredSize(new Dimension(700, 300));
        _console.setEditable(false);
        _scrollPane = new JScrollPane(_console);
        add(_scrollPane, BorderLayout.CENTER);
        
        JPanel p = new JPanel();
        p.add(new JButton(new ClearConsoleAction()));
        p.add(new CloseButton());
        add(p, BorderLayout.SOUTH);
    }
    
    private void printf(String print, Object... args) {
        Document doc = _console.getDocument();
        try {
            doc.insertString(doc.getLength(), String.format(print+"%n", args), null);
        }
        catch (BadLocationException e1) {
            e1.printStackTrace();
        }     
        finally {
        	scrollDown();
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
        finally {
        	scrollDown();
        }
    }    
    
    private void print(Throwable e) {
        StringWriter buf = new StringWriter();
        e.printStackTrace(new PrintWriter(buf));
        printf(buf.toString());
    }
    
    private void printSeparator() {
    	printf("---------------------------------------------------------\n");
    }
    
    private java.util.List<Note> tune(int len) {
        java.util.List<Note> tune = new LinkedList<Note>();
        tune.add(new Note(new Pitch(NotesEnum.C, 4), 64, len));
        tune.add(new Note(new Pitch(NotesEnum.E, 4), 64, len));
        tune.add(new Note(new Pitch(NotesEnum.G, 4), 64, len));
        tune.add(new Note(new Pitch(NotesEnum.C, 5), 64, len*2));
        tune.add(new Note(new Pitch(NotesEnum.G, 4), 64, len));
        tune.add(new Note(new Pitch(NotesEnum.C, 5), 64, len*3));
        return tune;
    }
    
    private void scrollDown() {
			JScrollBar sb = _scrollPane.getVerticalScrollBar();
	        sb.setValue(sb.getMaximum());
    }
    
    private class PlayAction extends AbstractAction {
        public PlayAction() {
            super("Test MIDI playback");
        }

        public void actionPerformed(ActionEvent e) {
            List<Note> tune = tune(150);
            try {
            	ToneGenerator tg = ToneGenerator.getInstance();
            	tg.setMIDISynthID(_devs.getToneSynthIndex());
                tg.initializeSequenceToPlay(tune, 1.0f);
                tg.startSequencerPlayback(true);
            }
            catch (MidiUnavailableException e1) {
                print(e1);
            }
            
            printf("Played %s", tune);
            printSeparator();
        }
    }
    
    private class ClearConsoleAction extends AbstractAction {
        public ClearConsoleAction() {
            super("Clear Console");
        }

        public void actionPerformed(ActionEvent e) {
        	Document doc = _console.getDocument();
            try {
				doc.remove(0, doc.getLength());
			} 
            catch (BadLocationException e1) {} // should clear; not worth an exception
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
                    boolean hasT = (device.getMaxTransmitters() != 0);
                    boolean hasR = (device.getMaxReceivers() != 0);
                    printf("%d %s %s %s, %s, %s, %s", 
                        i,
                        (hasT ? "TRANS" : "     "),
                        (hasR ? "RECIEVE" : "       "),
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
            printSeparator();
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
                _tapRecorder = new TapRecorder(true, 0);
                _tapRecorder.setLogReceiver(new ConsoleReceiver());
            }
            catch (MidiUnavailableException e) {
                print(e);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);

            try {
				ToneGenerator.getInstance().setMIDISynthID(_devs.getToneSynthIndex());
			} catch (MidiUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            int dev = _devs.getTapInputIndex() - 1;//getCurrentDeviceID();
            
            // determine notice for selected device
            String message = null;
            if (dev >= MidiSystem.getMidiDeviceInfo().length || dev < 0) {
            	message = "Invalid tapping device . . . \nStart tapping via the computer keyboard";
            }
            else if (!TapRecorder.isValidTransmittingDevice(dev)) {
            	message = "The selected device does not have transmit functionality . . . \n" +
            			"Start tapping via the computer keyboard";
            }
            else {
            	_tapRecorder.setMIDIInputID(dev);
                message = "Start tapping via the selected device, or use the computer keyboard";
            }
            _tapRecorder.setMIDISynthID(_devs.getTapSynthIndex() - 1);

            printf(message);
            
            // begin recording
            try {
                ToneGenerator.getInstance().getSequencer().addMetaEventListener(_endListener);
                _currSequence = ToneGenerator.getInstance().initializeSequenceToPlay(tune(1000), 1.0f);
                _tapRecorder.initializeSequencerForRecording();//_tapRecorder.start(_currSequence);
                
                ToneGenerator.getInstance().startSequencerPlayback(false);
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
            printSeparator();
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
