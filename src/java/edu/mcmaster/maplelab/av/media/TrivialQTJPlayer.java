package edu.mcmaster.maplelab.av.media;

import java.io.File;
import java.awt.*;
 
import quicktime.*;
import quicktime.std.movies.Movie;
import quicktime.app.view.QTFactory;
import quicktime.io.*;
 
public class TrivialQTJPlayer extends Frame {
 
    public static void main (String[] args) {
        try {
            QTSession.open();
            Frame f = new TrivialQTJPlayer();
            f.pack();
            f.setVisible (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public TrivialQTJPlayer() 
        throws QTException {
    	File f = new File("/Users/bguseman/Desktop/als.avi");
        OpenMovieFile omf = OpenMovieFile.asRead (new QTFile (f));
        Movie m = Movie.fromFile (omf);
        Component c = QTFactory.makeQTComponent(m).asComponent();
        add (c);
        m.start();
    }
}

