/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id: Session.java 484 2009-06-22 13:26:59Z bhocking $
*/
package edu.mcmaster.maplelab.common.datamodel;

import java.applet.Applet;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Context data for the experiment session.
 * @version     $Revision: 484 $
 * @author     <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since     May 10, 2006
 * @param <B> Block type
 * @param <T> Trial type
 */
public abstract class Session<B extends Block<?,?>, T extends Trial<?>> implements Executor {
    // NB: These are in camel case so that the "name()" matches
    // properties file values.
    /**
     * @version   $Revision: 484 $
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    public enum ConfigKeys {
        raid,
        subject,
        session,
        sex,
        dataDir,
        experiment_id,
        numBlocks,
        sequences,
        probePractice,
        numWarmupTrials,
        maxProbePractice,
        reqProbeConsistent,
        maxSeqPractice,
        reqSeqConsistent,
        debug,
        demo,
        defaultFontSize
    }

    /**
     * @uml.property  name="properties"
     * @uml.associationEnd  qualifier="key:java.lang.Object java.lang.Object"
     */
    private final Map<String, Object> _properties = new HashMap<String, Object>();
    private final ExecutorService _executorService;

    private TrialLogger<B, T> _trialLogger;
    /**
     * @uml.property  name="applet"
     */
    private Applet _applet = null;

    /**
     * Default ctor.
     * @param props Initial values
     */
    protected Session(Properties props) {
        _executorService = Executors.newSingleThreadExecutor();

        Enumeration<Object> e = props.keys();
        while(e.hasMoreElements()) {
            String key = (String) e.nextElement();
            setProperty(key, props.getProperty(key));
        }
    }

    /**
     * Set the initialized trial logger.
     * @uml.property  name="trialLogger"
     */
    public final void setTrialLogger(TrialLogger<B, T> trialLogger)

    {
        _trialLogger = trialLogger;
    }

    /**
     * Get the trial logger.
     * @uml.property  name="trialLogger"
     */
    public final TrialLogger<B, T> getTrialLogger()

    {
        return _trialLogger;
    }

    /**
     * Execute the given object in a worker thread.
     * {@inheritDoc}
     * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
     */
    public final void execute(Runnable command) {
        _executorService.execute(command);
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException ex) {
            // ignore
        }
    }

    /**
     * Get the applet if experiment is running inside an applet container.
     * @uml.property  name="applet"
     */
    public Applet getApplet() {
        return _applet;
    }

    /**
     * For use when running inside an applet, set the outer applet object.
     * @deprecated Need to remove this. Check availability through global
     * AppletContext
     */
    @Deprecated
    public void setApplet(Applet applet)  {
        _applet = applet;
    }

    /**
     * Get the experiment identifier (database key.
     */
    public int getExperimentID() {
        return getInteger(ConfigKeys.experiment_id, -1);
    }

    public int getNumBlocksFromSequences() {
        return getNumBlocksFromSequences(false);
    }

    /**
     * Get NumBlocks property
     * @return Value for NumBlocks
     */
    public int getNumBlocksFromSequences(boolean useProbeList) {
        ConfigKeys seqKey = (useProbeList) ? ConfigKeys.probePractice : ConfigKeys.sequences;
        String[] sequences = getString(seqKey, "seq1,seq2,seq3").split(",");
        return sequences.length;
    }

    /**
     * Get NumBlocks property
     * @return Value for NumBlocks
     */
    public int getNumBlocks() {
        return getInteger(ConfigKeys.numBlocks, 1);
    }

     /**
     * Get NumWarmupTrials property
     * @return Value for NumWarmupTrials
     */
     public int getNumWarmupTrials() {
         return getInteger(ConfigKeys.numWarmupTrials, 1);
     }

     public String[] getSequences() {
         return getSequences(false);
     }

     public String[] getSequences(boolean useProbeList) {
         ConfigKeys seqKey = (useProbeList) ? ConfigKeys.probePractice : ConfigKeys.sequences;
         String[] toReturn = getString(seqKey, "seq1,seq2,seq3").split(",");
         return toReturn;
     }

    /**
     * Determine if debug mode has been requested.
     */
    public boolean isDebug() {
        return getBoolean(ConfigKeys.debug, false);
    }

    /**
     * Determine if demo mode has been requested.
     */
     public boolean isDemo() {
         return getBoolean(ConfigKeys.demo, false);
     }

     /**
      * Override the demo mode setting in config file.
      */
     public void setDemo(boolean state) {
         setProperty(ConfigKeys.demo, state);
     }

     /**
      * Set RAID property
      * @param val Value for RAID
      */
     public void setRAID(int val) {
         setProperty(ConfigKeys.raid, val);
     }

     /**
      * Get RAID property
      * @return Value for RAID
      */
     public int getRAID() {
         return getInteger(ConfigKeys.raid, -1);
     }

     /**
      * Set Subject property
      * @param val Value for Subject
      */
     public void setSubject(int val) {
         setProperty(ConfigKeys.subject, val);
     }

     /**
      * Get Subject property
      * @return Value for Subject
      */
     public int getSubject() {
         Integer subject = (Integer) getProperty(ConfigKeys.subject);
         return subject != null ? subject : getExperimentID();
     }

     /**
      * Set Sex property
      * @param val Value for Sex
      */
     public void setSex(String val) {
         setProperty(ConfigKeys.sex, val);
     }

     /**
      * Get Sex property
      * @return Value for Sex
      */
     public String getSex() {
         return (String) getProperty(ConfigKeys.sex);
     }

     /**
      * Set Session property
      * @param val Value for Session
      */
     public void setSession(int val) {
         setProperty(ConfigKeys.session, val);
     }

     /**
      * Get Session property
      * @return Value for Session
      */
     public int getSession() {
         Integer session = (Integer) getProperty(ConfigKeys.session);
         return session != null ? session : getExperimentID();
     }

     /**
      * Set DataDir property
      * @param val Value for DataDir
      */
     public void setDataDir(File val) {
         setProperty(ConfigKeys.dataDir, val);
     }

     /**
      * Get DataDir property
      * @return Value for DataDir
      */
     public File getDataDir() {
         return (File) getProperty(ConfigKeys.dataDir);
     }

     /**
      * Default text font size, primarily for instruction text.
      */
     public int getDefaultFontSize() {
         return getInteger(ConfigKeys.defaultFontSize, 16);
     }

    /**
     * Get the boolean property value with the given key.
     *
     * @param key key name
     * @param def default value if property not found.
     * @return property value.
     */
    public final boolean getBoolean(Enum<?> key, boolean def) {
        boolean retval = def;

        String str = getString(key, null);
        if (str != null) {
            if (str.equals("true")) {
                return true;
            }
            if (str.equals("false")) {
                return false;
            }
        }
        try {
            retval = Boolean.parseBoolean(str);
        }
        catch(Exception ex) {
        }

        return retval;
    }

    /**
     * Get the integer property value with the given enum key.
     *
     * @param key key name
     * @param def default value if property not found.
     * @return property value.
     */
    public final int getInteger(Enum<?> key, int def) {
        return getInteger(key.name(), def);
    }

    /**
     * Get the integer property value with the given string key.
     *
     * @param key key name
     * @param def default value if property not found.
     * @return property value.
     */
    protected final int getInteger(String key, int def) {
        int retval = def;
        Object val = getProperty(key);
        if(val instanceof String) {
            try {
                retval = Integer.parseInt((String)val);
            }
            catch(NumberFormatException ex) {
            }
        }
        else if(val instanceof Integer) {
            retval = ((Integer)val).intValue();
        }
        return retval;
    }

    /**
     * Get property value as a list of integers
     *
     * @param key property name
     * @param def default value if property name not defeined
     * @return property value parsed as a comma delimited list of integers
     */
    protected final List<Integer> getIntegerList(Enum<?> key, Integer... def) {
        List<Integer> retval = null;

        Object val = getProperty(key);

        if(val instanceof String) {
            retval = new ArrayList<Integer>();
            Scanner s = new Scanner((String)val);
            s.useDelimiter("[, \t]");
            while(s.hasNextInt()) {
                retval.add(s.nextInt());
            }
        }
        else if(val instanceof Integer[]) {
            retval = Arrays.asList((Integer[]) val);
        }

        if(retval == null && def != null) {
            retval = Arrays.asList(def);
        }

        return retval;
    }

    /**
     * Get property value as a list of floats
     *
     * @param key property name
     * @param def default value if property name not defeined
     * @return property value parsed as a comma delimited list of integers
     */
    protected final List<Float> getFloatList(Enum<?> key, Float[] def) {
        List<Float> retval = null;

        Object val = getProperty(key);

        if(val instanceof String) {
            retval = new ArrayList<Float>();
            Scanner s = new Scanner((String)val);
            s.useDelimiter("[, \t]");
            while(s.hasNextFloat()) {
                retval.add(s.nextFloat());
            }
        }
        else if(val instanceof Float[]) {
            retval = Arrays.asList((Float[]) val);
        }

        if(retval == null && def != null) {
            retval = Arrays.asList(def);
        }

        return retval;
    }
    /**
     * Get the float property value with the given key.
     *
     * @param key key name
     * @param def default value if property not found
     * @return property value
     */
    public final float getFloat(Enum<?> key, float def) {
        float retval = def;
        Object val = getProperty(key);
        if(val instanceof String) {
            try {
                retval = Float.parseFloat((String)val);
            }
            catch(NumberFormatException ex) {
            }
        }
        else if(val instanceof Float) {
            retval = ((Float)val).floatValue();
        }
        return retval;
    }

    /**
     * Get property value as a list of strings from an enum key
     *
     * @param key property name
     * @param def default value if property name not defined
     * @return property value parsed as a comma delimited list of integers
     */
    protected final List<String> getStringList(Enum<?> key, String... def) {
        return getStringList(key.name(), def);
    }

    /**
     * Get property value as a list of strings from a string key
     *
     * @param key property name
     * @param def default value if property name not defined
     * @return property value parsed as a comma delimited list of integers
     */
    protected final List<String> getStringList(String key, String... def) {
        List<String> retval = null;

        Object val = getProperty(key);

        if(val instanceof String) {
            retval = new ArrayList<String>();
            Scanner s = new Scanner((String)val);
            s.useDelimiter("[, \t]");
            while(s.hasNext()) {
                retval.add(s.next());
            }
        }
        else if(val instanceof Integer[]) {
            retval = Arrays.asList((String[]) val);
        }

        if(retval == null && def != null) {
            retval = Arrays.asList(def);
        }

        return retval;
    }

    /**
     * Look up the property value with the given key.
     *
     * @param name key
     * @return value
     */
    public final String getString(Enum<?> key, String def) {
        String retval = def;
        Object val = getProperty(key);
        if (val instanceof String) {
            retval = (String) val;
        }
        return retval;
    }

    /**
     * Lookup a string value with the given key.
     *
     * @param key value key.
     * @return key value, or null if no String value with key found.
     */
    public final String getString(String key)  {
        Object val = getProperty(key);
        if(val instanceof String) {
            return (String) val;
        }
        System.out.println("key=" + key);
        System.out.println("val=" + val);
        if(val!=null) {
        	System.out.println(" {" + val.getClass() + "}");
        }
        return null;
    }

    // Property management.
    /**
     * Retrieve a property with an enum key.
     */
    protected Object getProperty(Enum<?> key) {
        return getProperty(key.name());
    }

    /**
     * Retrieve a property with a string key.
     */
    protected Object getProperty(String key) {
        if (_properties.get(key) == null) {
            System.out.println("Entry for key '" + key + "' not found");
        }
        return _properties.get(key);
    }

    /**
     * Store a property.
     */
    protected void setProperty(String key, Object value) {
        _properties.put(key, value);
    }

    /**
     * Store a property.
     */
    protected void setProperty(Enum<?> key, Object value) {
        _properties.put(key.name(), value);
    }

    /**
     * Convert the current settings to a string in (more or less) .properties
     * file format.
     */
    public abstract String toPropertiesString();

    /**
     * Utility method for constructing value for toPropertiesString().
     *
     * @param e enumeration values to iterate over.
     * @return properties file like string.
     */
    protected String toPropertiesStringWithEnum(EnumSet<? extends Enum<?>> e) {
        StringBuilder buf = new StringBuilder();
        for (Enum<?> key : e) {
            Object value = getProperty(key);
            if(value instanceof PropertyFileValue) {
                value = ((PropertyFileValue)value).toPropertyValueFormat();
            }
            buf.append(key.name());
            buf.append('=');
            buf.append(value);
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * Utility method for constructing value for toPropertiesString().
     *
     * @param e enumeration values to iterate over.
     * @return properties file like string.
     */
    protected String toPropertiesStringWithStrings(String... keys) {
        StringBuilder buf = new StringBuilder();
        for (String key : keys) {
            Object value = getProperty(key);
            // Hack to save these in a more parseable format.
            if(value instanceof PropertyFileValue) {
                value = ((PropertyFileValue)value).toPropertyValueFormat();
            }
            buf.append(key);
            buf.append('=');
            buf.append(value);
            buf.append("\n");
        }
        return buf.toString();
    }

}
