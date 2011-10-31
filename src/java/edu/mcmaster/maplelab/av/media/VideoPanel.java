package edu.mcmaster.maplelab.av.media;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.PaintEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/*
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.media.Media;
import javafx.scene.media.MediaBuilder;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaPlayerBuilder;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaViewBuilder;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;*/

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


import net.miginfocom.swing.MigLayout;


import java.io.File;
import java.awt.*;
 
import quicktime.*;
import quicktime.std.movies.Movie;
import quicktime.app.view.QTFactory;
import quicktime.io.*;

//import uk.co.caprica.vlcj.player.MediaPlayerFactory;
//import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

@SuppressWarnings("deprecation")
public class VideoPanel extends JPanel {
	
	/*private static void initAndShowGUI() {
		
		
		
		final JFXPanel panel = new JFXPanel();
		
		

		final JFrame window = new JFrame();
		window.add(panel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		

        Platform.runLater(new Runnable() {
            public void run() {
            	
            	File f = new File("/Users/bguseman/Desktop/al1.5static.dv");
        		URI uri = f.toURI();
        		Media m = new Media(uri.toString());
        		
        		
        		
        		Group g = new Group();
        		Scene s = new Scene(g, m.getWidth(), m.getHeight());
        		panel.setScene(s);
        		window.pack();

        		MediaPlayer mp = new MediaPlayer(m);
        		//mp.setAutoPlay(true);
        		mp.play();
        		
        		MediaView mv = new MediaView(mp);
        		g.getChildren().add(mv);
        		
        		
        		//mp.setStartTime(Duration.ZERO);
        		//mp.play();
        		
            }
        });
    }

    public static void main(String[] args) {
    	//Application.launch(AppTest.class, args);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initAndShowGUI();
            }
        });
    }
    
    public static class AppTest extends Application {

    	@Override
    	public void start(Stage primaryStage) throws Exception {
    		primaryStage.setTitle("My JavaFX Application");
            primaryStage.setScene(new Scene(null));
            //primaryStage.show();
    	}
    }*/
	
	/*public static void main(String[] args) {
		Frame f = new Frame("Test Player");
	    f.setSize(800, 600);
	    f.addWindowListener(new WindowAdapter() {
	      @Override
	      public void windowClosing(WindowEvent e) {
	        System.exit(0);
	      }
	    });
	    f.setLayout(new BorderLayout());
	    Canvas vs = new Canvas();
	    f.add(vs, BorderLayout.CENTER);
	    f.setVisible(true);

	    MediaPlayerFactory factory = new MediaPlayerFactory(new String[] {});
	    
	    EmbeddedMediaPlayer mediaPlayer = factory.newMediaPlayer(null);
	    mediaPlayer.setVideoSurface(vs);
	    
	    mediaPlayer.playMedia("/Users/bguseman/Downloads/sample_iTunes.mov");
	    try {
			Thread.currentThread().join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		
	}*/
	
	 
	
	private Playable _clip;
    public VideoPanel() throws QTException {
    	super(new MigLayout("fill, insets 0", "[left]", "[top]"));
    	File f = new File("/Users/bguseman/Desktop");
    	_clip = QTVideoClip.findPlayable("als.avi", f, 1.0f);
        add(QTVideoClip.makeComponent(_clip));
    }
    
    public Playable getPlayable() {
    	return _clip;
    }

    /* Test */
	public static void main (String[] args) {
        try {
            QTSession.open();
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            VideoPanel vp = new VideoPanel();
            f.add(vp);
            f.setVisible(true);
            f.pack(); // XXX: pack must come AFTER setVisible(true)
            vp.getPlayable().play();
            
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
