/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.webservice.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author mleidig@schlund.de
 */
public class Monitor {
    
	public static enum Scope {SESSION,IP};
	
	Scope scope;
    int histSize;
    Map<String,MonitorHistory> ipToHistory;
    long timeout=2*60*1000;
    long cleanupInterval=1*60*1000;
    Thread cleanupThread;
    
    public Monitor(Scope scope,int histSize) {
    	this.scope=scope;
        this.histSize=histSize;
        if(scope==Scope.IP) {
	        ipToHistory=new HashMap<String,MonitorHistory>();
	        cleanupThread=new Thread() {
	        	public void run() {
	                while(!isInterrupted()) {
	                	synchronized(ipToHistory) {
		                	long time=System.currentTimeMillis()-timeout;
		                	Iterator<String> it=ipToHistory.keySet().iterator();
		                	while(it.hasNext()) {
		                		String ip=it.next();
		                		MonitorHistory hist=ipToHistory.get(ip);
		                		if(hist.lastModified()<time) ipToHistory.remove(ip);
		                	}
	                	}
	                	try {
	                		Thread.sleep(cleanupInterval);
	                	} catch(InterruptedException x) {}              
	                }
	            }
	        };
	        cleanupThread.start();
        }
    }
    
    public Scope getScope() {
    	return scope;
    }
    
    public int getHistorySize() {
    	return histSize;
    }
    
    public MonitorHistory getMonitorHistory(HttpServletRequest req) {
    	MonitorHistory mh=null;
    	if(scope==Scope.IP) {
	    	String ip=req.getRemoteAddr();
	    	synchronized(ipToHistory) {
		        mh=(MonitorHistory)ipToHistory.get(ip);
		        if(mh==null) {
		            mh=new MonitorHistory(histSize);
		            ipToHistory.put(ip,mh);
		        }       
	    	}
    	} else {
    		HttpSession session=req.getSession(false);
    		if(session!=null) {
    			mh=(MonitorHistory)session.getAttribute(MonitorHistory.class.getName());
    			if(mh==null) {
    				mh=new MonitorHistory(histSize);
    				session.setAttribute(MonitorHistory.class.getName(),mh);
    			}
    		} else return new MonitorHistory(histSize);
    	}
    	return mh;
    }
    
}
