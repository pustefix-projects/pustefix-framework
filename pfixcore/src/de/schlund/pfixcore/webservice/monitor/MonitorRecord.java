/*
 * Created on 21.08.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MonitorRecord {

	String target;
    long startTime;
    long endTime;
    String request;
    String response;
    
    public MonitorRecord() {
        
    }
    
    public String getTarget() {
    	return target;
    }
    
    public void setTarget(String target) {
    	this.target=target;
    }
    
    public long getStartTime() {
    	return startTime;
    }
    
    public void setStartTime(long startTime) {
    	this.startTime=startTime;
    }
    
    public long getEndTime() {
    	return endTime;
    }
    
    public void setEndTime(long endTime) {
    	this.endTime=endTime;
    }
    
    public long getTime() {
    	return endTime-startTime;
    }
    
    public String getRequest() {
    	return request;
    }
    
    public void setRequest(String request) {
    	this.request=request;
    }
    
    public String getResponse() {
    	return response;
    }
    
    public void setResponse(String response) {
    	this.response=response;
    }
    
}
