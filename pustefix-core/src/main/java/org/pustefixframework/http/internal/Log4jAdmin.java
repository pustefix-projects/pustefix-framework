package org.pustefixframework.http.internal;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * MBean for changing Log4j log levels at runtime. 
 */
public class Log4jAdmin implements Log4jAdminMBean, InitializingBean, DisposableBean  {

	private Logger LOG = Logger.getLogger(Log4jAdmin.class);

	private String projectName;
	private Map<String, Level> originalLevels = new HashMap<String, Level>();
	private Level originalRootLevel;

	/**
	 * Set project name.
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Register MBean.
	 */
    public void afterPropertiesSet() throws Exception {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=Log4jAdmin,project="+projectName);
            if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register Log4jAdmin MBean!",x);
        } 
    }

    /**
     * Unregister MBean.
     */
    public void destroy() throws Exception {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=Log4jAdmin,project="+projectName);
            if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
        } catch(Exception x) {
            LOG.error("Can't unregister Log4jAdmin MBean!",x);
        } 
    }

    /**
     * Get the log level for the specified logger.
     */
	public synchronized String getLogLevel(String logger) {
		if(logger == null || logger.isEmpty()) {
			throw new IllegalArgumentException("Missing argument: logger");
		}
		Logger log4jLogger = Logger.getLogger(logger);
		if(log4jLogger.getLevel() != null) {
			return log4jLogger.getLevel().toString();
		} else {
			return "";
		}
	}

	/**
	 * Set the log level for the specified logger.
	 */
	public synchronized void setLogLevel(String logger, String level) {
		if(logger == null || logger.isEmpty() || level == null || level.isEmpty()) {
			throw new IllegalArgumentException("Missing arguments: logger, level");
		}
		Level log4jLevel = getLevel(level);
		Logger log4jLogger = Logger.getLogger(logger);
		if(!originalLevels.containsKey(logger)) {
			originalLevels.put(logger, log4jLogger.getLevel());
		}
		log4jLogger.setLevel(log4jLevel);
	}

	/**
	 * Get the log level for the root logger.
	 */
	public synchronized String getRootLogLevel() {
		return LogManager.getRootLogger().getLevel().toString();
	}

	/**
	 * Set the log level for the root logger.
	 */
	public synchronized void setRootLogLevel(String level) {
		if(level == null || level.isEmpty()) {
			throw new IllegalArgumentException("Missing argument: level");
		}
		Level log4jLevel = getLevel(level);
		if(originalRootLevel != null) {
			originalRootLevel = LogManager.getRootLogger().getLevel();
		}
		LogManager.getRootLogger().setLevel(log4jLevel);
	}

	/**
	 * Convert string representation of log level to Level object.
	 */
	private Level getLevel(String level) {
		Level log4jLevel = Level.toLevel(level);
		if(log4jLevel == Level.DEBUG && !level.equalsIgnoreCase("DEBUG")) {
			throw new IllegalArgumentException("Illegal log level value: " + level);
		}
		return log4jLevel;
	}

	/**
	 * Set log levels back to initial value.
	 */
	@Override
	public synchronized void restoreLogLevels() {
		Iterator<String> it = originalLevels.keySet().iterator();
		while(it.hasNext()) {
			String logger = it.next();
			Level log4jLevel = originalLevels.get(logger);
			Logger log4jLogger = Logger.getLogger(logger);
			log4jLogger.setLevel(log4jLevel);
		}
		originalLevels.clear();
		if(originalRootLevel != null) {
			LogManager.getRootLogger().setLevel(originalRootLevel);
			originalRootLevel = null;
		}
	}

}