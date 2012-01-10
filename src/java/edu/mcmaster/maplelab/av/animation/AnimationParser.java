/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.animation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.MatchResult;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;

import edu.mcmaster.maplelab.common.LogContext;

/**
 * This class parses a file and creates an AnimationSequence.
 * @author Catherine Elder <cje@datamininglab.com>
 */

public class AnimationParser {
	private static final String COLOR_DATA_KEY = "colorData";
	private static final String SIZE_DATA_KEY = "sizeData";
	private static final String LUMINANCE_DATA_KEY = "luminanceData";
	private static final String SHAPE_DATA_KEY = "shapeData";
	private static final String ASPECT_RATIO_KEY = "pointAspectRatio";
	private static final String COLOR_PATTERN = "\\(({0,1}\\d{1,3}),\\s*(\\d{1,3}),\\s*(\\d{1,3})\\){0,1}";
	private static final String SIZE_PATTERN = "size\\s*(\\d*\\.{0,1}\\d*)";
	
	private static Map<String, AnimationSequence> _animationCache = new HashMap<String, AnimationSequence>();

	public static AnimationSequence parseFile(File file) throws FileNotFoundException {
		return parseFile(file, null);
	}
	
	public static AnimationSequence parseFile(File file, Float aspectRatio) throws FileNotFoundException {
		// method accepts a file  and returns AnimationSequence
		// assign frames from file contents
		
		String filename = file.getName();
		AnimationSequence retval = _animationCache.get(filename);
		if (retval != null) return retval;

		BufferedReader reader = null;

		ArrayList<AnimationFrame> frameList = new ArrayList<AnimationFrame>();

		boolean colorData = false;
		boolean sizeData = false;
		boolean luminanceData = false;
		boolean shapeData = false;
		float aspect = aspectRatio != null && aspectRatio > 0 ? aspectRatio : 1.0f;
		
		try {
			reader = new BufferedReader(new FileReader(file));	
			String line = null;
			int colsPerDot = 2;

			while ((line = reader.readLine()) != null) {
				LogContext.getLogger().finer(line);

				Scanner scanner = new Scanner(line);

				// parse data keys
				if (!scanner.hasNextFloat()){					// skip over time data (first column)
					String property = scanner.next();
					if (property.contains(SIZE_DATA_KEY)) {
						try {
							String val = property.substring(property.indexOf("=") + 1);
							sizeData = val.contains("t") || val.contains("T");
							if (sizeData) colsPerDot++;
						} 
						catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
					else if (property.contains(COLOR_DATA_KEY)) {
						try {
							String val = property.substring(property.indexOf("=") + 1);
							colorData = val.contains("t") || val.contains("T");
							if (colorData) colsPerDot++;
						} 
						catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
					else if (property.contains(LUMINANCE_DATA_KEY)) {
						try {
							String val = property.substring(property.indexOf("=") + 1);
							luminanceData = val.contains("t") || val.contains("T");
							// XXX: luminance is per frame ONLY
							//if (luminanceData) colsPerDot++;
						} 
						catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
					else if (property.contains(SHAPE_DATA_KEY)) {
						try {
							String val = property.substring(property.indexOf("=") + 1);
							shapeData = val.contains("t") || val.contains("T");
							if (shapeData) colsPerDot++;
						} 
						catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
					else if ((aspectRatio == null || aspectRatio <= 0) && 
							property.contains(ASPECT_RATIO_KEY)) {
						
						try {
							String val = property.substring(property.indexOf("=") + 1);
							aspect = Float.valueOf(val.trim());
						} 
						catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
					
					// if no float (time) and no property key, line is not valid
					scanner.close();
					continue;
				}
				
				
				// at this point, we should be at frame lines
				float time = scanner.nextFloat();
				Double lum = null;
				List<AnimationPoint> dotList = new ArrayList<AnimationPoint>();

				// search this line only
				while (scanner.hasNext()) {
					while (true) {
						int cols = 0;
						Double sizeDouble = null;
						Vector3d colorVec = null;
						Point2d point = null;
						AnimationShapeDrawable shape = null;
						
						// should always have x and y
						String xString = scanner.next();
						String yString = scanner.next();
						cols += 2;
						
						try {
							// attempt to get the point location
							point = new Point2d(Double.parseDouble(xString), 
									Double.parseDouble(yString)/aspect);
							
							// now attempt to gather, then match any secondary properties
							// for the animation point
							// XXX: could this be more efficient while remaining robust?
							String val = "";
							while (cols < colsPerDot) {
								String next = scanner.next();
								if (next.equals("-")) {
									cols++;
									continue;
								}
								else if (shape == null) {
									// check for shape, which will be single token
									try {
										shape = AnimationShapeDrawable.valueOf(next.toUpperCase());
										cols++;
										continue;
									}
									catch (Exception e) { }
								}
								
								// concatenate token
								val += next;
								
								// color
								if (colorData && colorVec == null) {
									Scanner s = new Scanner(val);
									if (s.findInLine(COLOR_PATTERN) != null) {
										MatchResult colorMatch = s.match();
										colorVec = new Vector3d(Double.parseDouble(colorMatch.group(1)), 
												Double.parseDouble(colorMatch.group(2)), 
												Double.parseDouble(colorMatch.group(3)));		
										cols++;
									}
								}
								
								// size
								if (sizeData && sizeDouble == null) {
									Scanner s = new Scanner(val);
									String res = s.findInLine(SIZE_PATTERN);
									// have to check against just "size" in case of space there
									if (res != null && !res.equals("size")) {
										MatchResult sizeMatch = s.match();
										sizeDouble = Double.parseDouble(sizeMatch.group(1));
										cols++;
									}
								}
							}
							
							dotList.add(new AnimationPoint(point, colorVec, sizeDouble, shape));
							// INNER LOOP ITERATION ENDS
							if(!scanner.hasNext()) {
							    break;
							}
						}
						catch (Exception e) {
							// if this is a "blank" animation point, just
							// move past all of its entries and add the blank
							if (xString.equals("-") || yString.equals("-")) {
								while (cols < colsPerDot && scanner.hasNext()) {
									scanner.next();
									cols++;
								}
								// add the "blank" animation point - blanks
								// must be present for interpolation purposes later
								dotList.add(new AnimationPoint(point, colorVec, sizeDouble, shape));
								// INNER LOOP ITERATION ENDS
							}
							// if we're at the end of the line, see if there
							// is a luminance value at the end
							else {
								while (scanner.hasNext()) {
									if (scanner.hasNextDouble()) {
										lum = scanner.nextDouble();
									}
									else {
										scanner.next();
									}
								}
								break; // INNER LOOP ENDS
							}
						} 
					}
					
					
					
					/*boolean addDot = true;
					int cols = 0;
					// collect data for 1 animation point
					while (cols < colsPerDot) {
						
						
						// attempt to extract the x location value
						String val = scanner.next();
						Double x = null;
						try {
							x = Double.parseDouble(val);
						}
						catch (NumberFormatException e) {
							if (val.equals("-")) {
								
							}
							// if there is not a valid point next, this
							// must be pit/vis markers and luminance data
							// (possibly) at the end of the line
							while (scanner.hasNext()) {
								// check for luminance
								if (scanner.hasNextDouble()) {
									lum = scanner.nextDouble();
								}
								else {
									scanner.next();
								}
							}
							addDot = false;
							break; // end of line will break the outer loop, also
						}
						
						// extract the full location			
						try {
							point = new Point2d(x, scanner.nextDouble());
							cols++;
						}
						catch (Exception e) {
							// add this animation point w/ null values
							break;
						}
						
						// check progress
						if (cols >= colsPerDot) break;
						
						// get dot color and size
						if (scanner.hasNext("-")) {
							// assume color and size are null
							scanner.next();
//							String color = scanner.next();
//							System.out.printf("color = %s\n", color);
							
							scanner.next();
//							String size = scanner.next();
//							System.out.printf("size = %s\n", size);
						}
						else {
							try {
								scanner.findInLine("((\\d{1,3}),\\s*(\\d{1,3}),\\s*(\\d{1,3}))");									
								MatchResult colorMatch = scanner.match();

								scanner.findInLine("(size\\s*\\d*\\.{0,1}\\d*)");									
								MatchResult sizeMatch = scanner.match();

								
//								System.out.printf("number of matches = %d\n", sizeMatch.groupCount());
								for (int i = 1; i <= sizeMatch.groupCount(); i++) {
//									System.out.printf("size match found: %s\n", sizeMatch.group(i));
								}
								sizeDouble = new Double(sizeMatch.group(1).substring(4).trim());



//								System.out.printf("number of matches = %d\n", colorMatch.groupCount());
								for (int i = 1; i <= colorMatch.groupCount(); i++) {
//							         System.out.printf("color match found: %s\n", colorMatch.group(i));
								}
								colorVec = new Vector3d(Double.parseDouble(colorMatch.group(2)), 
										Double.parseDouble(colorMatch.group(3)), Double.parseDouble(colorMatch.group(4)));
							}
							catch (Exception ex) {
								ex.printStackTrace();
								continue;
							}
						}
						cols += 2;
						
						if (cols >= colsPerDot) {break;}
					}
					
					if (addDot) dotList.add(new AnimationPoint(point, colorVec, sizeDouble, shape));*/
				}

				frameList.add(new AnimationFrame(time, dotList, lum));	
			} 
		}
		catch (IOException ex) {
			LogContext.getLogger().log(Level.SEVERE, "Animation file reading error", ex);
			ex.printStackTrace();
		}
		// close the reader
		finally { 
			try { 
				reader.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		AnimationSequence aniSeq = new AnimationSequence(filename, frameList, aspect);
		_animationCache.put(filename, aniSeq);

		return aniSeq;
	}
}