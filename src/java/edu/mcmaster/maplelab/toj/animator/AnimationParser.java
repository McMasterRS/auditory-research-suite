package edu.mcmaster.maplelab.toj.animator;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;
/**
 * This class parses a file and creates an AnimationSequence
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */

public class AnimationParser {
	// parses a file and returns AnimationSequence

	public AnimationSequence parseFile(File file) throws FileNotFoundException { // how should this exception be handled?
		System.out.printf("Animation Parser method: parseFile called\n");
		// method accepts a file  and returns AnimationSequence
		// assign frames from file contents

		BufferedReader reader = null;

		ArrayList<AnimationFrame> frameList = new ArrayList<AnimationFrame>();	// create list of frames

		try {
			reader = new BufferedReader(new FileReader(file));	// construct reader
			String line = null;

			while ((line = reader.readLine()) != null) {
				LogContext.getLogger().finer(line);

				Scanner scanner = new Scanner(line);

				if (!scanner.hasNextFloat()){						// skip over time data (first column)		
					continue; // Skip to next line
				}

				float time = scanner.nextFloat();			

				List<Point2D> pointList = new ArrayList<Point2D>();

				while (scanner.hasNextFloat()) {
					float x = scanner.nextFloat();
					float y = scanner.nextFloat();
					Point2D point = new Point2D.Float(x, y);
					pointList.add(point);
				}
				AnimationFrame animFrame = new AnimationFrame(time, pointList);	// new frame

				System.out.printf("length of pointList = %d\n", pointList.size()); // print number of points in frame
				frameList.add(animFrame);							// add frame to frameList

			}
		}
		catch (IOException ex) {
			LogContext.getLogger().log(Level.SEVERE, "Animation file reading error", ex);
		}
		// close the reader
		finally { 
			try { reader.close();} catch (Exception ex) {}
		}
		
		AnimationSequence aniSeq = new AnimationSequence(frameList);	// initialize AnimationSequence

		return aniSeq;
	}
}

