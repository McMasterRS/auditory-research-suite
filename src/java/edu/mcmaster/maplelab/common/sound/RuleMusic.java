/*
    Author: James Pate Williams (c) 2001

    Rule based music from _Elements of Computer Music_
    by F. Richard Moore (c) 1990 pages 413 - 418

    "1. The ambitus (pitch range) of the melody shall
    be evenly distributed between middle C and the G
    a tempered twelfth higher.
    2. The duration of each note of the melody shall
    be the same for simplicity.
    3. The melody shall be generated in 11 or 12
    note groups. Each group shall be followed by
    a rest of three or four times the duration of
    each melody note.
    4. The successive intervals of the melody shall
    be restricted to semitones, whole tones, and
    minor thirds except after rests, when a melodic
    skip upward as large as a perfect fifth shall
    be allowed, followed by a downward interval
    also as large as a perfect fifth.
    5. After five groups of notes have been generated,
    the melody will repeat the first group in
    retrograde and terminate."
 */

package edu.mcmaster.maplelab.common.sound;

import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Random;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*
 * @(#)MidiSynth.java   1.15    99/12/03
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

/**
 * Table for 128 general MIDI melody instuments.
 */
class InstrumentsTable extends JPanel {

    private static final long serialVersionUID = -358047901121004633L;
    private String names[] = { 
        "Piano", "Chromatic Perc.", "Organ", "Guitar", 
        "Bass", "Strings", "Ensemble", "Brass", 
        "Reed", "Pipe", "Synth Lead", "Synth Pad",
        "Synth Effects", "Ethnic", "Percussive", "Sound Effects" };
    private int col = 0, row = 0;
    private int nRows = 8;
    private int nCols = names.length; // just show 128 instruments
    // Pate added the following private data
    private String[] instrument = new String[128];
    private Synthesizer synthesizer;
    private JTable table;

    public InstrumentsTable() {
        instrument[0] = "Piano";
        instrument[1] = "Bright Piano";
        instrument[2] = "Electric Grand";
        instrument[3] = "Honky Tonk Piano";
        instrument[4] = "Electric Piano 1";
        instrument[5] = "Electric Piano 2";
        instrument[6] = "Harpsichord";
        instrument[7] = "Clavinet";
        instrument[8] = "Celesta";
        instrument[9] = "Glockenspiel";
        instrument[10] = "Music Box";
        instrument[11] = "Vibraphone";
        instrument[12] = "Marimba";
        instrument[13] = "Xylophone";
        instrument[14] = "Tubular Bell";
        instrument[15] = "Dulcimer";
        instrument[16] = "Hammond Organ";
        instrument[17] = "Perc Organ";
        instrument[18] = "Rock Organ";
        instrument[19] = "Church Organ";
        instrument[20] = "Reed Organ";
        instrument[21] = "Accordion";
        instrument[22] = "Harmonica";
        instrument[23] = "Tango Accordion";
        instrument[24] = "Nylon Str Guitar";
        instrument[25] = "Steel String Guitar";
        instrument[26] = "Jazz Electric Gtr";
        instrument[27] = "Clean Guitar";
        instrument[28] = "Muted Guitar";
        instrument[29] = "Overdrive Guitar";
        instrument[30] = "Distortion Guitar";
        instrument[31] = "Guitar Harmonics";
        instrument[32] = "Acoustic Bass";
        instrument[33] = "Fingered Bass";
        instrument[34] = "Picked Bass";
        instrument[35] = "Fretless Bass";
        instrument[36] = "Slap Bass 1";
        instrument[37] = "Slap Bass 2";
        instrument[38] = "Syn Bass 1";
        instrument[39] = "Syn Bass 2";
        instrument[40] = "Violin";
        instrument[41] = "Viola";
        instrument[42] = "Cello";
        instrument[43] = "Contrabass";
        instrument[44] = "Tremolo Strings";
        instrument[45] = "Pizzicato Strings";
        instrument[46] = "Orchestral Harp";
        instrument[47] = "Timpani";
        instrument[48] = "Ensemble Strings";
        instrument[49] = "Slow Strings";
        instrument[50] = "Synth Strings 1";
        instrument[51] = "Synth Strings 2";
        instrument[52] = "Choir Aahs";
        instrument[53] = "Voice Oohs";
        instrument[54] = "Syn Choir";
        instrument[55] = "Orchestra Hit";
        instrument[56] = "Trumpet";
        instrument[57] = "Trombone";
        instrument[58] = "Tuba";
        instrument[59] = "Muted Trumpet";
        instrument[60] = "French Horn";
        instrument[61] = "Brass Ensemble";
        instrument[62] = "Syn Brass 1";
        instrument[63] = "Syn Brass 2";
        instrument[64] = "Soprano Sax";
        instrument[65] = "Alto Sax";
        instrument[66] = "Tenor Sax";
        instrument[67] = "Baritone Sax";
        instrument[68] = "Oboe";
        instrument[69] = "English Horn";
        instrument[70] = "Bassoon";
        instrument[71] = "Clarinet";
        instrument[72] = "Piccolo";
        instrument[73] = "Flute";
        instrument[74] = "Recorder";
        instrument[75] = "Pan Flute";
        instrument[76] = "Bottle Blow";
        instrument[77] = "Shakuhachi";
        instrument[78] = "Whistle";
        instrument[79] = "Ocarina";
        instrument[80] = "Syn Square Wave";
        instrument[81] = "Syn Saw Wave";
        instrument[82] = "Syn Calliope";
        instrument[83] = "Syn Chiff";
        instrument[84] = "Syn Charang";
        instrument[85] = "Syn Voice";
        instrument[86] = "Syn Fifths Saw";
        instrument[87] = "Syn Brass and Lead";
        instrument[88] = "Fantasia";
        instrument[89] = "Warm Pad";
        instrument[90] = "Polysynth";
        instrument[91] = "Space Vox";
        instrument[92] = "Bowed Glass";
        instrument[93] = "Metal Pad";
        instrument[94] = "Halo Pad";
        instrument[95] = "Sweep Pad";
        instrument[96] = "Ice Rain";
        instrument[97] = "Soundtrack";
        instrument[98] = "Crystal";
        instrument[99] = "Atmosphere";
        instrument[100] = "Brightness";
        instrument[101] = "Goblins";
        instrument[102] = "Echo Drops";
        instrument[103] = "Sci Fi";
        instrument[104] = "Sitar";
        instrument[105] = "Banjo";
        instrument[106] = "Shamisen";
        instrument[107] = "Koto";
        instrument[108] = "Kalimba";
        instrument[109] = "Bag Pipe";
        instrument[110] = "Fiddle";
        instrument[111] = "Shanai";
        instrument[112] = "Tinkle Bell";
        instrument[113] = "Agogo";
        instrument[114] = "Steel Drums";
        instrument[115] = "Woodblock";
        instrument[116] = "Taiko Drum";
        instrument[117] = "Melodic Tom";
        instrument[118] = "Syn Drum";
        instrument[119] = "Reverse Cymbal";
        instrument[120] = "Guitar Fret Noise";
        instrument[121] = "Breath Noise";
        instrument[122] = "Seashore";
        instrument[123] = "Bird";
        instrument[124] = "Telephone";
        instrument[125] = "Helicopter";
        instrument[126] = "Applause";
        instrument[127] = "Gunshot";
        setLayout(new BorderLayout());

        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() { return nCols; }
            public int getRowCount() { return nRows;}
            public Object getValueAt(int r, int c) { 
                if (instrument != null) {
                    return instrument[c*nRows+r];
                } else {
                    return Integer.toString(c*nRows+r);
                }
            }
            @Override
            public String getColumnName(int c) { 
                return names[c];
            }
            @Override
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
            @Override
            public boolean isCellEditable(int r, int c) {return false;}
            @Override
            public void setValueAt(Object obj, int r, int c) {}
        };

        table = new JTable(dataModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel lsm = table.getSelectionModel();
        // Listener for row changes
        //ListSelectionModel lsm = table.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel sm = (ListSelectionModel) e.getSource();
                if (!sm.isSelectionEmpty()) {
                    row = sm.getMinSelectionIndex();
                }
                programChange(col*nRows+row);
            }
        });

        // Listener for column changes
        lsm = table.getColumnModel().getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel sm = (ListSelectionModel) e.getSource();
                if (!sm.isSelectionEmpty()) {
                    col = sm.getMinSelectionIndex();
                }
                programChange(col*nRows+row);
            }
        });

        table.setPreferredScrollableViewportSize(new Dimension(nCols*110, 200));
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(true);
        for (int i = 0; i < names.length; i++) {
            TableColumn column = table.getColumn(names[i]);
            column.setPreferredWidth(110);
        }
        table.setAutoResizeMode(table.AUTO_RESIZE_OFF);

        JScrollPane sp = new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(sp);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800,170);
    }
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(800,170);
    }

    private void programChange(int program) {
        if (synthesizer != null) {
            //synthesizer.loadInstrument(instruments[program]);
            // Pate added the next three lines
            MidiChannel midiChannels[] = synthesizer.getChannels();
            for (int i = 0; i < midiChannels.length; i++)
                midiChannels[i].programChange(program);
        }
        /*cc.channel.programChange(program);
            if (record) {
                createShortEvent(PROGRAM, program);
            }*/
    }
    // Pate added this method
    String[] getInstrument() {
        return instrument;
    }
    void setData(Synthesizer s) {
        synthesizer = s;
    }
    public void initTable() {
        ListSelectionModel lsm = table.getSelectionModel();
        lsm.setSelectionInterval(0,0);
        lsm = table.getColumnModel().getSelectionModel();
        lsm.setSelectionInterval(0,0);
    }
} // end InstrumentsTable

/**
 * A collection of MIDI controllers.
 */
class Controls extends JPanel implements /*ActionListener,*/ ChangeListener, ItemListener {

    private static final long serialVersionUID = -7636745775880783637L;
    //public JButton recordB;
    //JMenu menu;
    //int fileNum = 0;
    final int REVERB = 91;
    int beats = 108, velocity = 64;
    JCheckBox soloCB, monoCB, muteCB/*, sustCB*/;
    JSlider veloS, presS, bendS, revbS, beatS;
    MidiChannel midiChannel;

    public Controls() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5,10,5,10));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        veloS = createSlider("Velocity", p, 0, 127, 64);
        presS = createSlider("Pressure", p, 0, 127, 64);
        revbS = createSlider("Reverb", p, 0, 127, 64);
        beatS = createSlider("BPM", p, 40, 208, beats);

        // create a slider with a 14-bit range of values for pitch-bend
        bendS = create14BitSlider("Bend", p);

        p.add(Box.createHorizontalStrut(10));
        add(p);

        p = new JPanel();
        p.setBorder(new EmptyBorder(10,0,10,0));
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        /*JComboBox combo = new JComboBox();
            combo.setPreferredSize(new Dimension(120,25));
            combo.setMaximumSize(new Dimension(120,25));
            for (int i = 1; i <= 16; i++) {
                combo.addItem("Channel " + String.valueOf(i));
            } 
            combo.addItemListener(this);
            p.add(combo);
            p.add(Box.createHorizontalStrut(20));*/

        muteCB = createCheckBox("Mute", p);
        soloCB = createCheckBox("Solo", p);
        monoCB = createCheckBox("Mono", p);
        //sustCB = createCheckBox("Sustain", p);

        /*createButton("All Notes Off", p);
            p.add(Box.createHorizontalStrut(10));
            p.add(mouseOverCB);
            p.add(Box.createHorizontalStrut(10));
            recordB = createButton("Record...", p);
            add(p);*/
    }

    /*public JButton createButton(String name, JPanel p) {
            JButton b = new JButton(name);
            b.addActionListener(this);
            p.add(b);
            return b;
        }*/

    private JCheckBox createCheckBox(String name, JPanel p) {
        JCheckBox cb = new JCheckBox(name);
        cb.addItemListener(this);
        p.add(cb);
        return cb;
    }

    private JSlider createSlider(String name, JPanel p, int min, int max, int value) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
        slider.addChangeListener(this);
        TitledBorder tb = new TitledBorder(new EtchedBorder());
        tb.setTitle(name + " = " + Integer.toString(value));
        slider.setBorder(tb);
        p.add(slider);
        p.add(Box.createHorizontalStrut(5));
        return slider;
    }

    private JSlider create14BitSlider(String name, JPanel p) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 16383, 8192);
        slider.addChangeListener(this);
        TitledBorder tb = new TitledBorder(new EtchedBorder());
        tb.setTitle(name + " = 8192");
        slider.setBorder(tb);
        p.add(slider);
        p.add(Box.createHorizontalStrut(5));
        return slider;
    }

    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider) e.getSource();
        int value = slider.getValue();
        TitledBorder tb = (TitledBorder) slider.getBorder();
        String s = tb.getTitle();
        tb.setTitle(s.substring(0, s.indexOf('=')+1) + s.valueOf(value));
        if (s.startsWith("Velocity")) {
            velocity = value;
        } else if (s.startsWith("Pressure")) {
            midiChannel.setChannelPressure(value);
        } else if (s.startsWith("Bend")) {
            midiChannel.setPitchBend(value);
        } else if (s.startsWith("Reverb")) {
            midiChannel.controlChange(REVERB, value);
        } else if (s.startsWith("BPM")) {
            beats = value;
        }
        slider.repaint();
    }

    public void itemStateChanged(ItemEvent e) {
        /*if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                cc = channels[combo.getSelectedIndex()];
                cc.setComponentStates();
            } else {*/
        JCheckBox cb = (JCheckBox) e.getSource();
        String name = cb.getText();
        if (name.startsWith("Mute")) {
            midiChannel.setMute(/*cc.mute = */cb.isSelected());
        } else if (name.startsWith("Solo")) {
            midiChannel.setSolo(/*cc.solo = */cb.isSelected());
        } else if (name.startsWith("Mono")) {
            midiChannel.setMono(/*cc.mono = */cb.isSelected());
        }/* else if (name.startsWith("Sustain")) {
                    cc.sustain = cb.isSelected();
                    cc.channel.controlChange(SUSTAIN, cc.sustain ? 127 : 0);
                }*/
        //}
    }

    /*public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (button.getText().startsWith("All")) {
                for (int i = 0; i < channels.length; i++) {
                    channels[i].channel.allNotesOff();
                }
                for (int i = 0; i < keys.size(); i++) {
                    ((Key) keys.get(i)).setNoteState(OFF);
                }
            } else if (button.getText().startsWith("Record")) {
                if (recordFrame != null) {
                    recordFrame.toFront();
                } else {
                    recordFrame = new RecordFrame();
                }
            }
        }*/
    // Pate added the following methods
    public int getBeats() {
        return beats;
    }
    public int getVelocity() {
        return velocity;
    }

    public void setMidiChannel(MidiChannel mc) {
        midiChannel = mc;
    }
} // End class Controls

// end of Sun's contribution to the program

class RuleMusicPanel extends JPanel {
    int deltaX, deltaY, number;
    int x0, x1, y0, y1, y2, y3;
    int[] note, duration;
    Color penColor;

    public RuleMusicPanel(int iWidth, int iHeight) {
        number = 0;
        x0 = iWidth / 16;
        x1 = 15 * x0;
        y0 = iHeight / 24;
        y1 = 2 * y0;
        deltaY = (y1 - y0) / 4;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        penColor = g.getColor();
        y2 = y0;
        y3 = y1;
        drawStaff(0, number/ 2, g);
        y2 += 10 * deltaY;
        y3 = y2 + 4 * deltaY;
        drawStaff(number / 2, number, g);
    }

    public boolean sharp(int iNote) {
        boolean s = false;
        int[] sharps = {61, 63, 66, 68, 70, 73, 75, 78};

        for (int i = 0; i < sharps.length; i++) {
            if (iNote == sharps[i]) {
                s = true;
                break;
            }
        }
        return s;
    }

    int delta(int iNote) {
        if (iNote == 79)
            return 0;
        if (iNote == 77 || iNote == 78)
            return 1;
        if (iNote == 76)
            return 2;
        if (iNote == 74 || iNote == 75)
            return 3;
        if (iNote == 72 || iNote == 73)
            return 4;
        if (iNote == 71)
            return 5;
        if (iNote == 69 || iNote == 70)
            return 6;
        if (iNote == 67 || iNote == 68)
            return 7;
        if (iNote == 65 || iNote == 66)
            return 8;
        if (iNote == 64)
            return 9;
        if (iNote == 62 || iNote == 63)
            return 10;
        else
            return 11;
    }

    public void drawSharp(int iNote, int x, int y, Graphics g) {
        if (sharp(iNote)) {
            g.drawLine(x - deltaX / 4 - 2, y - deltaY / 4, x - deltaX / 4 - 2, y + deltaY / 4);
            g.drawLine(x - deltaX / 4 - 4, y - deltaY / 4, x - deltaX / 4 - 4, y + deltaY / 4);
            g.drawLine(x - deltaX / 4 - 5, y - deltaY / 8, x - deltaX / 4 - 1, y - deltaY / 8);
            g.drawLine(x - deltaX / 4 - 5, y + deltaY / 8, x - deltaX / 4 - 1, y + deltaY / 8);
        }
    }

    public void durationCase(int iDuration, int x, int y, Graphics g) {
        switch (iDuration) {
            case 0: // whole note
            g.drawArc(x, y, deltaX, deltaY, 0, 360);
            break;
            case 1: // half note
                g.fillArc(x, y, deltaX, deltaY, 0, 360);
                break;
            case 2: // quarter note
                g.fillArc(x, y, deltaX, deltaY, 0, 360);
                g.drawLine(x + deltaX, y + deltaY / 2, x + deltaX, y - 2 * deltaY);
                break;
            case 3: // eighth note
                g.fillArc(x, y, deltaX, deltaY, 0, 360);
                g.drawLine(x + deltaX, y + deltaY / 2, x + deltaX, y - 2 * deltaY);
                g.drawLine(x + deltaX, y - 2 * deltaY, x + deltaX + deltaX / 2, y - 2 * deltaY);
                break;
        }
    }

    public void drawStaff(int lo, int hi, Graphics g) {
        int i, j, n, x, y;

        y = y2;
        for (i = 0; i < 5; i++) {
            g.drawLine(x0, y, x1, y);
            y += deltaY;
        }
        g.drawLine(x0, y2, x0, y3);
        g.drawLine(x1, y2, x1, y3);
        if (lo == 0)
            deltaX = (x1 - x0) / (2 * (hi - lo) + 1);
        for (i = lo; i < hi; i++) {
            if (lo == 0)
                x = x0 + (2 * i + 1) * deltaX;
            else
                x = x0 + (2 * (i - lo) + 1) * deltaX;
            n = note[i];
            if (n == - 1) {
                y = y2 + 3 * deltaY / 2;
                g.fillArc(x, y, deltaX / 2, deltaY / 2, 0, 360);
                g.drawLine(x + deltaX / 2, y + deltaY, x + deltaX / 2, y - deltaY);
                continue;
            }
            y = delta(n) * deltaY / 2 + y2 - deltaY;
            drawSharp(n, x, y, g);
            durationCase(duration[i], x, y, g);
        }
    }

    public void drawNote(boolean red, int i, int iNote, int iDuration) {
        int x, y, z;

        Graphics g = getGraphics();
        if (red)
            g.setColor(Color.red);
        else
            g.setColor(penColor);
        if (i < number / 2)
            z = y0;
        else {
            i -= number / 2;
            z = y2;
        }
        x = x0 + (2 * i + 1) * deltaX;
        if (iNote == - 1) {
            y = z + 3 * deltaY / 2;
            g.fillArc(x, y, deltaX / 2, deltaY / 2, 0, 360);
            g.drawLine(x + deltaX / 2, y + deltaY, x + deltaX / 2, y - deltaY);
            return;
        }
        y = delta(iNote) * deltaY / 2 + z - deltaY;
        drawSharp(iNote, x, y, g);
        durationCase(iDuration, x, y, g);
    }

    public void setMusicAndDraw(int num, int[] not,  int[] dur) {
        number = num;
        note = new int[number];
        duration = new int[number];
        for (int i = 0; i < number; i++) {
            note[i] = not[i];
            duration[i] = dur[i];
        }
        paintComponent(getGraphics());
    }
}

class RuleMusicFrame extends JFrame {
    int iHeight, iWidth, number;
    int[] duration, note;
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JPanel jPanel = new JPanel();
    GridLayout gridLayout = new GridLayout(4, 1);
    Controls controls;
    Date date = new Date();
    String[] instrument;
    InstrumentsTable instrumentsTable;
    MidiChannel[] midiChannels;
    Random random = new Random(date.getTime());
    Receiver synthesizerReceiver;
    Synthesizer synthesizer;
    RuleMusicPanel ruleMusicPanel;

    // step 3 - percentage size the window
    void setDesktopSize(JFrame frame, int wPerc, int hPerc) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        iWidth = screen.width * wPerc / 100;
        iHeight = screen.height * hPerc / 100;
        frame.setSize(iWidth, iHeight);
    }

    // step 4 - center the window
    void centerOnScreen(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = frame.getSize();
        int iCenterX = screen.width / 2;
        int iCenterY = screen.height / 2;
        frame.setLocation(iCenterX - window.width / 2, iCenterY - window.height / 2);
    }

    public RuleMusicFrame() {
        number = 0;
        String title = "RuleMusic by James Pate Williams, Jr. (c) 2002";
        jButton1.setToolTipText("Generate white noise music");
        jButton1.setText("Generate Notes");
        jButton1.setVerticalAlignment(SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        jButton2.setText("Play Notes");
        jButton2.setVerticalAlignment(SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }
        });
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannels = synthesizer.getChannels();
            for (int i = 0; i < midiChannels.length; i++)
                midiChannels[i].programChange(0);
            synthesizerReceiver = synthesizer.getReceiver();
        }
        catch (Exception exception) {
            jButton2.setEnabled(false);
        }
        this.getContentPane().setLayout(gridLayout);
        setTitle(title);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                synthesizer.close();
                System.exit(0);
            }
        });
        setDesktopSize(this, 100, 100);
        centerOnScreen(this);
        controls = new Controls();
        controls.setMidiChannel(midiChannels[0]);
        Container contentPane = getContentPane();
        jPanel.add(controls, BorderLayout.CENTER);
        jPanel.add(jButton1, BorderLayout.EAST);
        jPanel.add(jButton2, BorderLayout.WEST);
        ruleMusicPanel = new RuleMusicPanel(iWidth, iHeight);
        instrumentsTable = new InstrumentsTable();
        instrumentsTable.initTable();
        instrument = instrumentsTable.getInstrument();
        jButton2.setToolTipText(instrument[0]);
        instrumentsTable.setData(synthesizer);
        contentPane.add(ruleMusicPanel);
        contentPane.add(controls);
        contentPane.add(instrumentsTable);
        contentPane.add(jPanel);
        this.show();
    }

    public int iRandom(int lo, int hi) {
        return random.nextInt(hi - lo + 1) + lo;
    }

    void jButton1_actionPerformed(ActionEvent e) {
        int group, n, grpLen, candidate, lastPitch = 0, interval;
        int restLen, grp0Len = 0;
        int i = 0, middleC = 60, highG = 79;
        int[] grp0 = new int[12];

        number = 128;
        note = new int[number];
        duration = new int[number];
        for (group = 0; group < 4; group++) {
            grpLen = iRandom(11, 12);
            for (n = 0; n < grpLen; n++) {
                if (group == 0 && n == 0) {
                    grp0[n] = candidate = iRandom(middleC, highG);
                    grp0Len = grpLen;
                }
                else if (group == 0) {
                    do {
                        grp0[n] = candidate = iRandom(middleC, highG);
                        interval = candidate - lastPitch;
                    } while (interval > 3 || interval < - 3);
                }
                else if (n == 0) {
                    do {
                        candidate = iRandom(middleC, highG);
                        interval = candidate - lastPitch;
                    } while (interval > 7 || interval < 0);
                }
                else if (n == 1) {
                    do {
                        candidate = iRandom(middleC, highG);
                        interval = candidate - lastPitch;
                    } while (interval > 0 || interval < - 7);
                }
                else {
                    do {
                        candidate = iRandom(middleC, highG);
                        interval = candidate - lastPitch;
                    } while (interval > 3 || interval < - 3);
                }
                lastPitch = candidate;
                duration[i] = 2;
                note[i++] = candidate;
            }
            restLen = iRandom(3, 4);
            for (n = 0; n < restLen; n++) {
                duration[i] = 2;
                note[i++] = - 1;
            }
        }
        for (n = grp0Len - 1; n >= 0; n--) {
            duration[i] = 2;
            note[i++] = grp0[n];
        }
        number = i;
        ruleMusicPanel.setMusicAndDraw(number, note, duration);
    }

    void jButton2_actionPerformed(ActionEvent e) {
        int beats = controls.getBeats();
        int volume = controls.getVelocity();

        try {
            int shortNumber = 14;
            for (int i = 0; i < shortNumber; i++) {
                ShortMessage shortMessage = new ShortMessage();
                if (note[i] != - 1) {
                    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, note[i], volume);
                    synthesizerReceiver.send(shortMessage, - 1);
                }
                int time = (int) (60000.0 / beats);
                ruleMusicPanel.drawNote(true, i, note[i], duration[i]);
                Thread.sleep(time);
                shortMessage = new ShortMessage();
                if (note[i] != - 1) {
                    shortMessage.setMessage(ShortMessage.NOTE_OFF, 0, note[i], volume);
                    synthesizerReceiver.send(shortMessage, - 1);
                }
                ruleMusicPanel.drawNote(false, i, note[i], duration[i]);
            }
        }
        catch (Exception exception) {
            System.exit(0);
        }
    }
}

class RuleMusic {
    public static void main(String[] args) {
        new RuleMusicFrame();
    }
}
