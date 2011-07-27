package edu.mcmaster.maplelab.toj.animator;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
/**
 * This class parses a file and creates an AnimationSequence
 * @author Catherine
 *
 */

public class AnimationParser {
// parses a file and returns AnimationSequence

	public AnimationSequence parseFile(File file) throws FileNotFoundException { // how should this exception be handled?
		System.out.printf("Animation Parser method: parseFile called\n");
		// method accepts a file  and returns AnimationSequence
		// assign frames from file contents
		
		BufferedReader reader = null;

		AnimationSequence aniSeq = new AnimationSequence();						// initialize AnimationSequence
		ArrayList<AnimationFrame> frameList = new ArrayList<AnimationFrame>();	// create list of frames
			
			try {
				reader = new BufferedReader(new FileReader(file));	// construct reader
				String line = null;
				
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					
					Scanner scanner = new Scanner(line);
					AnimationFrame animFrame = new AnimationFrame(0);	// new frame
					List<Point2D> pointList = new ArrayList<Point2D>();
					
					if (scanner.hasNextFloat()){						// skip over time data (first column)		
						float time = scanner.nextFloat(); 				
					}
					while (scanner.hasNextFloat()) {
						float x = scanner.nextFloat();
						float y = scanner.nextFloat();
						Point2D point = new Point2D.Float(x, y);
						pointList.add(point);
					}
					animFrame._pointList = pointList;					// add points to frame
					System.out.printf("length of pointList = %d\n", pointList.size()); // number of points in frame
					frameList.add(animFrame);
					
				//	line = scanner.nextLine();							// advance to next line (necessary?)
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			// close the reader
			finally { 
				try {
					if (reader != null)
						reader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		
		aniSeq._aniFrames = frameList;
				
		return aniSeq;
	}
}

