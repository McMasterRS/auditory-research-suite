/*
 * Copyright (c) 2008 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.common.util;

import javax.vecmath.Point2d;

public class MathUtils {
    /**
     * Calculates the distance between a point and a line, defined by y = mx + b
     * 
     * @param p Point to calculate distance from line for
     * @param slope m in y = mx + b
     * @param yIntercept b in y = mx + b
     * @return Distance
     */
    public static double pointDistFromLine(Point2d p, double slope, double yIntercept) {
        // Textbook formula is |Ax_1 + By_1 - C|/sqrt(A^2+B^2) for line defined by Ax + By = C
        // For y = mx + b, A=-m, B=1, and C=b
        return Math.abs(-slope * p.x + p.y - yIntercept)/Math.sqrt(slope*slope +1);
    }
    /**
     * Calculates the distance between a point and a line, defined by Ax + By = C
     * This form should be used if the slope is infinite
     * 
     * @param p Point to calculate distance from line for
     * @param A First parameter in Ax + By = C
     * @param B Second parameter in Ax + By = C
     * @param C Third parameter in Ax + By = C
     * @return Distance
     */
    public static double pointDistFromLine(Point2d p, double A, double B, double C) {
        // Textbook formula is |Ax_1 + By_1 - C|/sqrt(A^2+B^2) for line defined by Ax + By = C
        return Math.abs(A * p.x + B * p.y - C)/Math.sqrt(A*A +B*B);
    }
    
    /**
     * Clamps the given number to fall within (or equal to) the two given bounds.
     */
    public static float clamp(float value, float bound1, float bound2) {
    	float upper = Math.max(bound1, bound2);
    	float lower = Math.min(bound1, bound2);
    	float retval = Math.max(value, lower);
    	return Math.min(retval, upper);
    }
}
