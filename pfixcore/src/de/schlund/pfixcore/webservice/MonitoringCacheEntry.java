/*
 * de.schlund.pfixcore.webservice.MonitoringCacheEntry
 */
package de.schlund.pfixcore.webservice;

/**
 * MonitoringCacheEntry.java 
 * 
 * Created: 10.08.2004
 * 
 * @author mleidig
 */
public class MonitoringCacheEntry {

    String target;
    String req;
    String res;
    long start;
    long end;

    public MonitoringCacheEntry() {}

    public void setTarget(String target) {
        this.target=target;
    }

    public String getTarget() {
        return target;
    }

    public void setRequest(String req) {
        this.req=req;
    }

    public String getRequest() {
        return req;
    }

    public void setResponse(String res) {
        this.res=res;
    }

    public String getResponse() {
        return res;
    }

    public void setStart(long start) {
        this.start=start;
    }

    public long getStart() {
        return start;
    }
    
    public void setEnd(long end) {
        this.end=end;
    }
    
    public long getEnd() {
        return end;
    }

    public long getTime() {
        return end-start;
    }
    
}
