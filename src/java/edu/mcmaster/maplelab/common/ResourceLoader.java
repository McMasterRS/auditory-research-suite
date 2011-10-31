/*
 * Copyright (C) 2007 University of Virginia Supported by grants to the University of
 * Virginia from the National Eye Institute and the National Institute of Deafness and
 * Communicative Disorders. PI: Prof. Michael Kubovy <kubovy@virginia.edu> Distributed
 * under the terms of the GNU Lesser General Public License (LGPL). See LICENSE.TXT that
 * came with this file. $Id: ResourceLoader.java 485 2009-06-25 14:39:01Z bhocking $
 */

package edu.mcmaster.maplelab.common;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Utility class for finding and loading audio files.
 *
 *
 * @version $Revision: 485 $
 * @author <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since Nov 2, 2007
 */
public class ResourceLoader {

    /**
     * Look for the audio file with the given name. First looks in the given
     * <code>userDir</code> directory, then searches in other places via the ClassLoader
     * of this.
     *
     * @param userDir first place to search
     * @param fileName name of audio file
     * @return an open stream on the audio data.p
     * @throws FileNotFoundException
     */
    public static InputStream findAudioData(File userDir, String fileName)
            throws FileNotFoundException {
        InputStream retval = null;
        File f = new File(userDir, fileName);
        if (f.exists()) {
            retval = new FileInputStream(f);
        }
        else {
            URL loc = findResource(fileName);
            if (loc != null) {
                try {
                    retval = loc.openStream();
                }
                catch (IOException ex) {
                    LogContext.getLogger().log(Level.WARNING, "Couldn't open stream", ex);
                    // Just handle generically below.
                }
                LogContext.getLogger().finest(String.format("Found %s here: %s", fileName, loc));
            }
        }

        if (retval == null) {
            throw new FileNotFoundException(String.format(
                "Could not find audio file with name '%s'", fileName));
        }

        return retval;
    }
    
    public static File findResource(File dir, String fileName) throws FileNotFoundException {
    	File retval = null;
        File f = dir != null ? new File(dir, fileName) : null;
        if (f != null && f.exists()) {
            retval = f;
        }
        else {
            URL loc = findResource(fileName);
            if (loc != null) {
                try {
                    retval = new File(loc.toURI());
                }
                catch (URISyntaxException e) {
                	LogContext.getLogger().log(Level.WARNING, "Couldn't open file", e);
                    // Just handle generically below.
				}
                LogContext.getLogger().finest(String.format("Found %s here: %s", fileName, loc));
            }
        }

        if (retval == null) {
            throw new FileNotFoundException(String.format(
                "Could not find file with name '%s'", fileName));
        }

        return retval;
    }

    private static URL findResource(String name) {
        URL retval = null;

        retval = ResourceLoader.class.getResource(name);
        if (retval == null) {
            // On OS X see if we can find the file in the Resources folder
            File resDir = new File(libdir(), "..");
            File res = new File(resDir, name);
            if (res.exists()) {
                try {
                    retval = res.toURL();
                }
                catch (MalformedURLException ex) {
                }
            }
            if (retval == null) {
                File f = new File(name);
                if (f.exists()) {
                    try {
                        retval = f.toURL();
                    }
                    catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return retval;
    }

    public static Icon findIcon(String name) {
        Icon retval = null;

        URL loc = findResource(name);

        if (loc != null) {
            retval = new ImageIcon(loc);
        }

        return retval;
    }

    /**
     * Hack to get the directory name where the application jar file is located. (Too bad
     * the "Package" class won't provide this info.
     */
    private static String libdir() {
        // If install.root is undefined find jpython.jar in class.path
        String classpath = System.getProperty("java.class.path");
        if (classpath == null) return null;

        int index = classpath.toLowerCase().indexOf("as.jar");
        if (index == -1) {
            return null;
        }
        int start = classpath.lastIndexOf(java.io.File.pathSeparator, index) + 1;
        return classpath.substring(start, index);
    }

}
