package edu.mcmaster.maplelab.toj.animator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.MatchResult;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;

import edu.mcmaster.maplelab.common.LogContext;

/**
 * This class parses a file and creates an AnimationSequence
 * @author Catherine Elder <cje@datamininglab.com>
 *
 */

public class AnimationParser {
	// parses a file and returns AnimationSequence
	private static final String COLOR_DATA_KEY = "colorData";
	private static final String SIZE_DATA_KEY = "sizeData";
	private static final String LUMINANCE_DATA_KEY = "luminanceData";

	public static AnimationSequence parseFile(File file) throws FileNotFoundException { // how should this exception be handled?
		// method accepts a file  and returns AnimationSequence
		// assign frames from file contents

		BufferedReader reader = null;

		ArrayList<AnimationFrame> frameList = new ArrayList<AnimationFrame>();	// create list of frames

		boolean colorData = false;
		boolean sizeData = false;
		boolean luminanceData = false;
		
		try {
			reader = new BufferedReader(new FileReader(file));	// construct reader
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
							if (luminanceData) colsPerDot++;
						} 
						catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
					System.out.printf("size data: %b, color data: %b, lum data: %b\n", sizeData, colorData, luminanceData);
				}
				System.out.printf("cols per dot = %d\n", colsPerDot);
				
				
				float time = 0f;
				if (scanner.hasNextFloat()) {		
					// assume we are at a "dot line", not metadata line
					time = scanner.nextFloat();			

					List<AnimationDot> dotList = new ArrayList<AnimationDot>();

					// search this line only
					thisline:
					while (scanner.hasNext()) {
						Double sizeDouble = null;
						Vector3d colorVec = null;
						Point2d point = null;
						Double lum = null;

						
						int cols = 0;
						// collect data for 1 dot
						while (cols < colsPerDot) {
							
							// get dot location							
							String xString = scanner.next();
							String yString = scanner.next();
							System.out.printf("x = %s, y = %s\n", xString, yString);
							
							// check for null entry
							if (!(xString.equals("-") || yString.equals("-"))) {
								
								// check if we are at the end of the line, looking at luminance
								if ((!Character.isDigit(xString.charAt(0)) && (!Character.isDigit(yString.charAt(0))))) {
									if (scanner.hasNext()) {
										String lumString = scanner.next();
										System.out.printf("lum = %s\n", lumString);
										lum = new Double(lumString);
									}
									cols++;
									System.out.println("at the end of the line\n");
									break thisline;
								}
								
								double x = Double.parseDouble(xString);
								double y = Double.parseDouble(yString);
								point = new Point2d(x,y);
							}
							cols += 2;
							System.out.printf("cols = %d\n", cols);
							if (cols >= colsPerDot) {break;}
							
							
							
							// get dot color
							if (scanner.hasNext("-")) {
								String color = scanner.next();
								System.out.printf("color = %s\n", color);
							}
							else {
								try {
									scanner.findInLine("((\\d{1,3}),\\s*(\\d{1,3}),\\s*(\\d{1,3}))");									
									MatchResult match = scanner.match();
									
									System.out.printf("number of matches = %d\n", match.groupCount());
									for (int i=1; i<=match.groupCount(); i++) {
								         System.out.printf("color match found: %s\n", match.group(i));
									}
									colorVec = new Vector3d(Double.parseDouble(match.group(2)), 
											Double.parseDouble(match.group(3)), Double.parseDouble(match.group(4)));
									
									scanner.next();		// skip end parenthesis
								}
								catch (Exception ex) {
									ex.printStackTrace();
									continue;
								}
							}
							cols++;
							System.out.printf("cols = %d\n", cols);
							if (cols >= colsPerDot) {break;}

							

							// get dot size
							
							if (scanner.hasNext("-")) {
								String size = scanner.next();
								System.out.printf("size = %s\n", size);
							}
							else {
								try {
									scanner.findInLine("(size\\s*\\d*\\.{0,1}\\d*)");									
									MatchResult match = scanner.match();
									
									System.out.printf("number of matches = %d\n", match.groupCount());
									for (int i=1; i<=match.groupCount(); i++) {
								         System.out.printf("size match found: %s\n", match.group(i));
									}
									sizeDouble = new Double(match.group(1).substring(4).trim());
								}
								catch (Exception ex) {
									ex.printStackTrace();
									continue;
								}
							}
							cols++;
							System.out.printf("cols = %d\n", cols);
							if (cols >= colsPerDot) {break;}
						}

						AnimationDot currentDot = new AnimationDot(point, colorVec, sizeDouble, lum);
						currentDot.printDescription();
						dotList.add(currentDot);

					}

					AnimationFrame animFrame = new AnimationFrame(time, dotList);	// new frame

					frameList.add(animFrame);							// add frame to frameList
				} 
				scanner.close();
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

		AnimationSequence aniSeq = new AnimationSequence(frameList);	// initialize AnimationSequence

		return aniSeq;
	}
}