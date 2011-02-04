package org.pustefixframework.agent;

/**
 * MBean providing live status information.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface LiveAgentMBean {

    public String getLiveLocation(String moduleName);
    
}
