/*
 * de.schlund.pfixcore.webservice.MonitoringCache
 */
package de.schlund.pfixcore.webservice;

import java.util.WeakHashMap;
import javax.servlet.http.HttpSession;

/**
 * MonitoringCache.java 
 * 
 * Created: 10.08.2004
 * 
 * @author mleidig
 */
public class MonitoringCache {
    
    WeakHashMap entries;
    
    public MonitoringCache() {
        entries=new WeakHashMap();
    }
    
    public void setLastEntry(HttpSession session,MonitoringCacheEntry entry) {
        entries.put(session,entry);
    }
    
    public MonitoringCacheEntry getLastEntry(HttpSession session) {
        return (MonitoringCacheEntry)entries.get(session);
    }
    
}

