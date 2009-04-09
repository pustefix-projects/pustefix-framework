/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.pustefixframework.webservices.monitor;

/**
 * @author mleidig@schlund.de
 */
public class MonitorRecord {
	
	String service;
    String method;
	String protocol;
    long startTime;
    long procTime;
    long invocTime;
    String reqMsg;
    String resMsg;
    
    public MonitorRecord() {
        
    }
    
    public String getService() {
    	return service;
    }
    
    public void setService(String service) {
    	this.service=service;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method=method;
    }
    
    public String getProtocol() {
    	return protocol;
    }
    
    public void setProtocol(String protocol) {
    	this.protocol=protocol;
    }
    
    public long getStartTime() {
    	return startTime;
    }
    
    public void setStartTime(long startTime) {
    	this.startTime=startTime;
    }
    
    public long getProcessingTime() {
    	return procTime;
    }
    
    public void setProcessingTime(long procTime) {
        this.procTime=procTime;
    }
    
    public long getInvocationTime() {
    	return invocTime;
    }
    
    public void setInvocationTime(long invocTime) {
        this.invocTime=invocTime;
    }
    
    public String getRequestMessage() {
    	return reqMsg;
    }
    
    public void setRequestMessage(String reqMsg) {
    	this.reqMsg=reqMsg;
    }
    
    public String getResponseMessage() {
    	return resMsg;
    }
    
    public void setResponseMessage(String resMsg) {
    	this.resMsg=resMsg;
    }
    
}
