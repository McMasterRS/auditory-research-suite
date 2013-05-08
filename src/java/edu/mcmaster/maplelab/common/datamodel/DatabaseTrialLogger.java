/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id$
 */
package edu.mcmaster.maplelab.common.datamodel;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import edu.mcmaster.maplelab.common.LogContext;

/**
 * Class responsible for logging trial data and other database connection duties.
 * @version  $Revision$
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Sep 7, 2006
 */
public abstract class DatabaseTrialLogger<S extends Session<?,?,?>, T extends Trial<?>> implements TrialLogger<T> {

    private final URL _base;
    /**
     * @uml.property  name="session"
     */
    private final S _session;
    private final ExecutorService _submitExecutor;

    /**
     * Standard constructor.
     * @param base url root for connection
     * @param id experiment id
     */
    public DatabaseTrialLogger(S session, URL base) throws IOException {
        _session = session;
        _base = base;
        _submitExecutor = Executors.newSingleThreadExecutor();
        
        int id = _session.getExperimentDBKey();
        if(id >= 0) {
            testConnection();
            saveSessionConfig();
        }
    }
    
    /**
     * Get the session object.
     * @uml.property  name="session"
     */
    public final S getSession() {
        return _session;
    }
    
    private void testConnection() throws IOException {
        // Test database connection.
        URLConnection conn = makeTestConnection();
        if(conn != null) {
            conn.connect();
            StringBuilder buf = readResponse(conn);
            
            buf.insert(0, "Experiment confirmation: ");
            LogContext.getLogger().fine(buf.toString());
        }
    }
    
    /**
     * Create a URL connection for testing connection to server.
     * It is suggested that {@link initConnection(String)} be used once relative
     * path is constructed.
     * 
     * @return Initialized connection, or null if connection shouldn't be tested.
     * @throws IOExceptionon connection error.
     */
    protected abstract URLConnection makeTestConnection() throws IOException;
    
    /**
     * Create a URLConnection ready for sending x-www-form-urlencoded data.
     * It is suggested that {@link initConnection(String)} be used once relative
     * path is constructed.
     * 
     * @return initialized URL connection for use by {@link submit}
     * @throws IOException on connection error.
     */
    protected abstract URLConnection makeSubmitConnection() throws IOException;

    /**
     * Create a URLConnection ready for saving the configuration data in
     * x-www-form-urlendoded format.
     * 
     * @return Initialized URL connection for use by {@link saveSessionConfig}
     *  or null if config shouldn't be saved.
     * @throws IOException
     */
    protected abstract URLConnection makeSaveConfigConnection() throws IOException;
    
    
    
    /**
     * Convert given block and trial objects into a key-value map to be encoded
     * as x-www-form-urlencoded data.
     * 
     * @param trial Current trail, with response data.
     * @return key-value map
     */
    protected abstract Map<String, String> marshalToMap(T trial);

    
    /**
     * Setup a connection to the server
     * 
     * @param path format encoded path string (relative to base URL)
     * @param pathArgs arguments to String.format() for path
     * @return opened connection
     * @throws IOException on setup error
     */
    protected URLConnection initConnection(String path, Object... pathArgs) 
        throws IOException {
        
        URL url = null;
        try {
            url = new URL(_base + String.format(path, pathArgs));
            
            LogContext.getLogger().finer("--> Initializing HTTP connection: " + url);
        }
        catch (MalformedURLException ex) {
            throw (IOException) new IOException("Couldn't create test URL: " + url).initCause(ex);
        }        
        
        URLConnection retval = url.openConnection();
        retval.setUseCaches(false);
        retval.setDoInput(true);
                
        return retval;
    }
    
    /**
     * Process connection response data.
     * 
     * @param conn connection to read response data from.
     * @return assembled response as string.
     * @throws IOException on I/O error
     */
    private StringBuilder readResponse(URLConnection conn) throws IOException {
        StringBuilder retval = new StringBuilder();
        HttpURLConnection http = (HttpURLConnection)conn;
        int resp = 200;
        try {
            resp = http.getResponseCode();
        }
        catch(Throwable ex) {
        }
        
        if(resp >=200 && resp < 300) {
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            catch(Throwable ex) {
                retval.append(ex.toString());
                return retval;
            }

            String line = null;
            while((line = input.readLine()) != null) {
                retval.append(line);
                retval.append("\n");
            }
            input.close();
        }
        else {
            retval.append(http.getResponseMessage());
        }
        
        LogContext.getLogger().finer(String.format("<-- HTTP Response: %d: %s", resp, retval));
        
        return retval;
    }

    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#submit(edu.mcmaster.maplelab.common.datamodel.Block, edu.mcmaster.maplelab.common.datamodel.Trial)
     */
    public void submit(final T trial) {
        _submitExecutor.execute(new Runnable() {
            public void run() {
                // NB: In development mode we set a negative experiment id
                // causing logging to be ignored.
                final int id = getSession().getExperimentDBKey();
                if(id < 0) return;
                

                try {
                    URLConnection conn = makeSubmitConnection();
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    
                    Map<String, String> fields = marshalToMap(trial);
                    
                    String row = encode(fields);
                    
                    LogContext.getLogger().finer("--> x-www-form-urlencoded data: " + row);
                    
                    OutputStreamWriter out = null;
                    try {
                        
                        OutputStream str = conn.getOutputStream();
                        
                        out = new OutputStreamWriter(str);
                        out.write(row);
                        out.flush();
                    }
                    finally {
                        if(out != null) out.close();
                    }
                    conn.connect();
      
                    readResponse(conn);
                }
                catch (IOException ex) {
                    LogContext.getLogger().log(Level.SEVERE, "Trial logging error", ex);
                }
            }
        });
    }

    /**
     * Convenience function for converting a map of key-value pairs
     * into a URL query string.
     * 
     * @param fields key-value pairs
     * @return query string.
     */
    protected static String encode(Map<String, String> fields) {
        StringBuilder buf = new StringBuilder();
        try {
            boolean prev = false;
            for(String key : fields.keySet()) {
                if(prev) {
                    buf.append("&");
                }
                else {
                    prev = true;
                }
                
                buf.append(URLEncoder.encode(key, "UTF-8"));
                buf.append("=");
                buf.append(URLEncoder.encode(fields.get(key), "UTF-8"));
            }
        }
        catch (Exception e) {
            LogContext.getLogger().log(Level.WARNING, "Problem encoding map.", e);
        }
        return buf.toString();
    }
    

    /**
     * {@inheritDoc} 
     * @see edu.mcmaster.maplelab.common.datamodel.TrialLogger#saveSessionConfig()
     */
    public void saveSessionConfig() throws IOException {
        // Save session configuration data.
        URLConnection conn = makeSaveConfigConnection();
        if(conn == null) return;
        
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        Map<String, String> fields = new HashMap<String, String>();
        
        fields.put("config", getSession().toPropertiesString());
        
        String row = encode(fields);
        
        LogContext.getLogger().finer("-> config log: " + row);
        
        if(getSession().getExperimentDBKey() >= 0) {
            OutputStreamWriter out = null;
            try {
                out = new OutputStreamWriter(conn.getOutputStream());
                out.write(row);
                out.flush();
            }
            finally {
                if(out != null) out.close();
            }
            conn.connect();
            readResponse(conn);
        }
    }    
}
