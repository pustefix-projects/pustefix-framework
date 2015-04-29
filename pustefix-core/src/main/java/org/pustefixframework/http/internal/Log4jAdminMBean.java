package org.pustefixframework.http.internal;

/**
 * MBean interface for changing Log4j log levels at runtime. 
 */
public interface Log4jAdminMBean {

	public String getLogLevel(String logger);
	public void setLogLevel(String logger, String level);

	public String getRootLogLevel();
	public void setRootLogLevel(String level);

	public void restoreLogLevels();

}