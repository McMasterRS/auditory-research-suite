/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/
package edu.mcmaster.maplelab.common.datamodel;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.DemoGUIPanel;
import edu.mcmaster.maplelab.common.util.MathUtils;

/**
 * Context data for the experiment session.
 * @version     $Revision$
 * @author     <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since     May 10, 2006
 * @param <B> Block type
 * @param <T> Trial type
 */
public abstract class Session<TM extends TrialManager<?, T>, T extends Trial<?>, 
											L extends TrialLogger<T>> implements Executor {
	/**
     * @version   $Revision$
     * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
     * @since   Feb 28, 2007
     */
    public enum ConfigKeys {
    	propertiesFile,
        raid,
        subject,
        session,
        dataDir,        
        db_experiment_id,
        experimentID, 
        subExperimentID,
        blockSetRepetitions,
        metablocks,
        numWarmupTrials,
        randomizeBlocks,
        randomizeTrials,
        preStimulusSilence,
        allowFeedback,
        trialDelay,
        debug,
        demo,
        fullScreen,
        defaultFontSize,
        buildVersion,
        buildDate,
        playbackGain,
        speedMode,
        propertyPrefix
    }

    /**
     * @uml.property  name="properties"
     * @uml.associationEnd  qualifier="key:java.lang.Object java.lang.Object"
     */
    private final Map<String, Object> _properties = new HashMap<String, Object>();
    private final ExecutorService _executorService;
    
    private TM _trialManager;
    private TM _warmupTM;
    private L _trialLogger;
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
        
        Enumeration<?> e = props.keys();
        while(e.hasMoreElements()) {
            String key = (String) e.nextElement();
            setProperty(key, props.getProperty(key));
        }
    }
    
    /**
     * Initialize the trial manager to be used with this session.
     */
    protected abstract TM initializeTrialManager(boolean warmup);
    
    /**
     * Get the trial manager for this session.
     */
    public TM getTrialManager(boolean warmup) {
    	if (warmup && _warmupTM == null) {
    		_warmupTM = initializeTrialManager(true);
    	}
    	if (!warmup && _trialManager == null) {
    		_trialManager = initializeTrialManager(false);
    	}
    	return warmup ? _warmupTM : _trialManager;
    }
    
    /**
     * Set the initialized trial logger.
     * @uml.property  name="trialLogger"
     */
    public final void setTrialLogger(L trialLogger) {
        _trialLogger = trialLogger;
    }
    
    /**
     * Get the trial logger.
     * @uml.property  name="trialLogger"
     */
    public final L getTrialLogger() {
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
     * Get the file to which the debug log file should be written.
     */
    public File getDebugLogFile() {
    	return FileTrialLogger.getOutputFile(this, FileType.get(FileTrialLogger.DEBUG_FILE));
    }
    
    /**
    * Flag to indicate running the experiment in less time for testing purposes.
    */
   public boolean isSpeedMode() {
       boolean retval = false;
       Object val = getProperty(ConfigKeys.speedMode);
       if(val instanceof String) {
           retval = Boolean.parseBoolean((String)val);
       }
       return retval;
   }
    
    /**
     * Get a string denoting the base name/type of experiment this session
     * encapsulates.  Will be used for file/directory naming.
     */
    public abstract String getExperimentBaseName();
    
    /**
     * Load a set of properties from the given file.
     */
    protected Properties loadSecondaryProps(File propFile) {
    	Properties props = new Properties();
    	
    	// prepare input stream
    	InputStream is = null;
    	try {
    		if (propFile.exists()) {
                is = new FileInputStream(propFile);              
            }
            else {
                is = getClass().getResourceAsStream(propFile.getName());
            }
    	}
    	catch (FileNotFoundException fe) {
    		String msg = String.format("Could not find file: ", propFile.getName());
            LogContext.getLogger().log(Level.SEVERE, msg, fe);
    	}
        
        // load properties
    	if(is != null) {
    	    try {
    	        props.load(is);
    	    }
    	    catch (Exception ex) {
    	        String msg = String.format("Error reading configuration file: ", propFile.getName());
    	        LogContext.getLogger().log(Level.SEVERE, msg, ex);
    	    }
    	    finally {
    	        if(is != null)  try { is.close(); } catch (IOException e) {}
    	    }
    	}
    	
        return props;
    }
    
    /**
     * Get the experiment identifier database key.
     */
    public int getExperimentDBKey() {
        return getInteger(ConfigKeys.db_experiment_id, -1);
    }    
    
    /**
     * Get the experiment identifier property.
     */
    public String getExperimentID() {
    	return getString(ConfigKeys.experimentID, "-1");
    }
    
    /**
     * Get the sub-experiment identifier property.
     */
    public String getSubExperimentID() {
        return getString(ConfigKeys.subExperimentID, "-1");
    }    
    
    /**
     * Get the properties prefix property.
     */
    public String getPropertiesPrefix() {
        return getString(ConfigKeys.propertyPrefix, null);
    } 
    
    /**
     * Flag to indicate if text feedback relating to subject
     * responses should be displayed, if applicable.
     */
    public boolean allowResponseFeedback() {
    	return getBoolean(ConfigKeys.allowFeedback, true);
    }
    
    /**
     * Number of milliseconds to wait before playback of stimulus, if applicable.
     */
    public int getPreStimulusSilence() {
        return getInteger(ConfigKeys.preStimulusSilence, 2000);
    }    
    
    /**
     * Get the delay between trials.
     */
    public int getTrialDelay() {
        return getInteger(ConfigKeys.trialDelay, 0);
    }    
    
    /**
     * Get the number of times a set of blocks should be repeated.
     */
    public int getBlockSetRepetitions() {
    	return getInteger(ConfigKeys.blockSetRepetitions, 1);
    }
    
    /**
     * Get the number of metablocks (full sets of repetitions).
     */
    public int getMetaBlocks() {
    	return getInteger(ConfigKeys.metablocks, 1);
    }
    
     /**
     * Get NumWarmupTrials property
     * @return Value for NumWarmupTrials
     */
     public int getNumWarmupTrials() {
         return getInteger(ConfigKeys.numWarmupTrials, 1);
     }
     
     /**
      * Indicate if trials should be randomized within each block.
      */
     public boolean randomizeTrials() {
    	 return getBoolean(ConfigKeys.randomizeTrials, true);
     }
     
     /**
      * Indicate if blocks should be randomized within each repetition.
      */
     public boolean randomizeBlocks() {
    	 return getBoolean(ConfigKeys.randomizeBlocks, true);
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
         setProperty(ConfigKeys.demo, Boolean.valueOf(state));
     }
     
     /**
      * Determine if the experiment should be run in full screen.
      */
     public boolean isFullScreen() {
    	 return getBoolean(ConfigKeys.fullScreen, false);
     }
     
     /**
      * Override the full screen mode setting in config file.
      */
     public void setFullScreen(boolean state) {
         setProperty(ConfigKeys.fullScreen, Boolean.valueOf(state));
     }
     
     /**
      * get ExperimentDemoPanel
      * @return
      */
     public abstract DemoGUIPanel<?, T> getExperimentDemoPanel();
     
     /**
      * Get the playback gain as a percentage of maximum [0.0, 1.0]
      */
     public float getPlaybackGain() {
    	 return getClampedFloat(ConfigKeys.playbackGain, 1.0f, 0.0f, 1.0f);
     }
     
     /**
      * Set RAID property
      * @param val Value for RAID
      */
     public void setRAID(String val) {
         setProperty(ConfigKeys.raid, val);
     }

     /**
      * Get RAID property
      * @return Value for RAID
      */
     public String getRAID() {
         return getString(ConfigKeys.raid, "-1");
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
         return subject != null ? subject : getExperimentDBKey();
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
         return session != null ? session : getExperimentDBKey();
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
    protected final boolean getBoolean(Enum<?> key, boolean def) {
        boolean retval = def;
        
        Object b = getProperty(key);
        if (b instanceof Boolean) return (Boolean) b;
        
        String str = getString(key, null);
        if (str != null && !str.isEmpty()) {
            try {
                retval = Boolean.parseBoolean(str);
            }
            catch(Exception ex) {
            }
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
     * Get the long property value with the given enum key.
     * 
     * @param key key name
     * @param def default value if property not found.
     * @return property value.
     */
    protected final long getLong(Enum<?> key, int def) {
        return getLong(key.name(), def);
    }
    
    /**
     * Get the long property value with the given string key.
     * 
     * @param key key name
     * @param def default value if property not found.
     * @return property value.
     */    
    protected final long getLong(String key, long def) {
        long retval = def;
        Object val = getProperty(key);
        if(val instanceof String) {
            try {
                retval = Long.parseLong((String)val);
            }
            catch(NumberFormatException ex) {
            }
        }
        else if(val instanceof Long) {
            retval = ((Long)val).longValue();
        }
        return retval;
    }

    /**
     * Get property value as a list of integers
     * 
     * @param key property name
     * @param def default value if property name not defined
     * @return property value parsed as a comma delimited list of integers
     */
    protected final List<Integer> getIntegerList(Enum<?> key, Integer... def) {
        List<Integer> retval = null;
        
        Object val = getProperty(key);
        
        if(val instanceof String) {
            retval = new ArrayList<Integer>();
            Scanner s = new Scanner((String)val);
            s.useDelimiter("[\\s\\[\\(\\]\\),]+");
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
     * Get property value as a list of longs
     * 
     * @param key property name
     * @param def default value if property name not defined
     * @return property value parsed as a comma delimited list of longs
     */
    protected final List<Long> getLongList(Enum<?> key, Long... def) {
        List<Long> retval = null;
        
        Object val = getProperty(key);
        
        if (val instanceof String) {
            retval = new ArrayList<Long>();
            Scanner s = new Scanner((String)val);
            s.useDelimiter("[\\s\\[\\(\\]\\),]+");
            while(s.hasNextLong()) {
                retval.add(s.nextLong());
            }
        }
        else if(val instanceof Long[]) {
            retval = Arrays.asList((Long[]) val);
        }
        
        if (retval == null && def != null) {
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
            // Finds one or more: whitespace OR open bracket OR open paren OR 
            //        close bracket OR close paren OR comma
            // as delimiter
            s.useDelimiter("[\\s\\[\\(\\]\\),]+");
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
    
    public final float getClampedFloat(Enum<?> key, float def, float upper, float lower) {
    	Float retval = getFloat(key, def);
    	return MathUtils.clamp(retval, upper, lower);
    }
    
    /**
     * Get property value as a list of strings from an enum key
     * 
     * @param key property name
     * @param def default value if property name not defeined
     * @return property value parsed as a comma delimited list of integers
     */
    public final List<String> getStringList(Enum<?> key, String... def) {
        return getStringList(key.name(), def);
    }
    
    /**
     * Get property value as a list of strings from a string key
     * 
     * @param key property name
     * @param def default value if property name not defeined
     * @return property value parsed as a comma delimited list of integers
     */
    public final List<String> getStringList(String key, String... def) {
        List<String> retval = null;
        
        Object val = getProperty(key);
        
        if(val instanceof String) {
            retval = new ArrayList<String>();
            Scanner s = new Scanner((String)val);
            s.useDelimiter("[,\\s\\t]+");
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
            retval = ((String) val).trim();
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
        return getString(key, null);
    }
    
    /**
     * Lookup a strng value with the given key
     * 
     * @param key value key
     * @param def default value if no value is found
     * @return key value, or <tt>def</tt> if no String with value found.
     */
    public final String getString(String key, String def)  {
        Object val = getProperty(key);
        if(val instanceof String) {
            return (String) val;
        }
        return def;
    }

    // Property management.
    /**
     * Retrieve a property with an enum key.
     */
    protected Object getProperty(Enum<?> key) {
    	// necessary to avoid infinite loop
    	if (key == ConfigKeys.propertyPrefix) {
    		return getProperty(key.name(), false);
    	}
        return getProperty(key.name());
    }
    
    /**
     * Retrieve a property with a string key.
     */
    protected Object getProperty(String key) {
    	return getProperty(key, true);
    }
    
    /**
     * Retrieve a property with a string key.
     */
    protected Object getProperty(String key, boolean allowPrefix) {
    	String pref = allowPrefix ? getPropertiesPrefix() : null;
    	Object retval = null;
    	if (pref != null && !pref.isEmpty()) {
    		retval = _properties.get(pref + "." + key);
    	}
    	
    	return retval != null ? retval : _properties.get(key);
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
    	return toPropertiesStringWithStrings(true, keys);
    }
    
    /**
     * Utility method for constructing value for toPropertiesString().
     * 
     * @param e enumeration values to iterate over.
     * @return properties file like string.
     */
    protected String toPropertiesStringWithStrings(boolean allowNull, String... keys) {
        StringBuilder buf = new StringBuilder();
        for (String key : keys) {
            Object value = getProperty(key);
            // Hack to save these in a more parseable format.
            if(value instanceof PropertyFileValue) {
                value = ((PropertyFileValue)value).toPropertyValueFormat();
            }
            
            if (!allowNull && value == null) continue;
            
            buf.append(key);
            buf.append('=');
            buf.append(value);
            buf.append("\n");
        }
        return buf.toString();
    }

	public static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
		}
	}

}
