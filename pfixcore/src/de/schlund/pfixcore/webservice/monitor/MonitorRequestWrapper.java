/*
 * de.schlund.pfixcore.webservice.monitor.MonitorRequestWrapper
 */
package de.schlund.pfixcore.webservice.monitor;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

/**
 * MonitorRequestWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public abstract class MonitorRequestWrapper extends HttpServletRequestWrapper {
   
    public MonitorRequestWrapper(HttpServletRequest req) {
        super(req);
    }
    
   public abstract byte[] getBytes();
   
}
