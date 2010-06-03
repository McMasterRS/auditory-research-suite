/*
 * Copyright (c) 2008 Southwest Research Institute.
 * All Rights reserved.
 */
package edu.mcmaster.maplelab.common.util;

import java.util.*;

public class ArrayUtils {
    public static String getCenterArray(String[] arr) {
        int center = (int) Math.floor(arr.length/2);
        return arr[center];
    }
    
    public static Set<List<String>> generatePermutations(List<String> varList) {
        Set<List<String>> permList = new HashSet<List<String>>();
        generatePermutationsHelper(varList, 0, permList, new ArrayList<String>());

        return permList;
    }
    
    private static void generatePermutationsHelper(List<String> varList, int varLoc, Set<List<String>> permList, List<String> valList) {
        String[] varVals = varList.get(varLoc).split(",");
        boolean atVarListEnd = (varLoc == (varList.size()-1));
        for (int i=0; i<varVals.length; i++) {
            List<String> passValList = new ArrayList<String>(valList);
            passValList.add(varVals[i]);
            if (atVarListEnd) {
                permList.add(passValList);
            } else {
                generatePermutationsHelper(varList, varLoc+1, permList, passValList);
            }
        }
    }
    
}
