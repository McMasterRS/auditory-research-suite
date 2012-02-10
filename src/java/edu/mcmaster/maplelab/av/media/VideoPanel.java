/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.media;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.rococoa.cocoa.qtkit.MovieComponent;
import org.rococoa.cocoa.qtkit.QTMovieView;


public class VideoPanel extends JPanel {
	private final QTMovieView _view;
	
	public VideoPanel() {
		super(new MigLayout("insets 0, fill"));
		_view = QTMovieView.CLASS.create();
		_view.setControllerVisible(false);
		_view.setPreservesAspectRatio(true);
	}
	
	public void setMovie(Playable clip) {
		if (clip instanceof QTVideoClip) {
			QTVideoClip qtc = (QTVideoClip) clip;
	        
	        MovieComponent component = new MovieComponent(_view);
	        _view.setMovie(qtc.getQTMovie()); 
			
	        removeAll();
	        add(component);
		}
	}
}
