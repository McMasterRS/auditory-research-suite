package edu.mcmaster.maplelab.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JPanel;

import edu.mcmaster.maplelab.common.datamodel.Session;

public abstract class Experiment<SessionImpl extends Session<?, ?, ?>> extends JPanel {
    
    private enum VersionProps {
		buildVersion,
		buildDate
	}
	
	private static String _buildVersion;
	private static String _buildDate;
	
	public static String getBuildVersion() {
		return _buildVersion;
	}

	public static String getBuildDate() {
		return _buildDate;
	}
    
    /**
     * Initialize build information.
     */
    public static void initializeBuildInfo(Class<? extends Experiment<?>> clazz, 
    		String prefsPrefix) throws IOException {
    	
    	if (_buildDate != null && _buildVersion != null) return;
    	
    	String name = prefsPrefix + ".version.properties";
        InputStream is = clazz.getResourceAsStream(name);
        
        Properties props = new Properties();
        try {
            props.load(is);
        }
        catch (Exception ex) {
        	LogContext.getLogger().log(Level.SEVERE, "Error reading version file", ex);
        }
        finally {
            if(is != null)  try { is.close(); } catch (IOException e) {}
        }

        if (props.isEmpty()) return;
		_buildVersion = props.getProperty(VersionProps.buildVersion.name(), "-1");
		_buildDate = props.getProperty(VersionProps.buildDate.name(), "00000000");
    }
	
	private final SessionImpl _session;
	
	public Experiment(SessionImpl session) {
		super(new BorderLayout());
		_session = session;
		
		JPanel p = new JPanel(new CardLayout());
		loadContent(p);
		add(p, BorderLayout.CENTER);
	}
	
	protected SessionImpl getSession() {
		return _session;
	}
	
	/**
     * This method initializes the content into the given cardLayout.
     */
	protected abstract void loadContent(JPanel contentCard);

}
