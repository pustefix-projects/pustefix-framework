/*
 * Created on 01.08.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

import java.util.*;
import javax.servlet.http.HttpSession;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Monitor {
    
    int histSize;
    String scope;
    WeakHashMap sessionToHistory;
    HashMap ipToHistory;
    long timeout=3600;
    long cleanupInterval=10;
    Thread cleanupThread;
    
    public Monitor(int histSize,String scope) {
        this.histSize=histSize;
        this.scope=scope;
        sessionToHistory=new WeakHashMap();
        ipToHistory=new HashMap();
        cleanupThread=new Thread() {
        	public void run() {
                while(!isInterrupted()) {
                	cleanup();
                	try {
                		Thread.sleep(cleanupInterval*1000);
                	} catch(InterruptedException x) {}              
                }
            }
        };
        cleanupThread.start();
    }
    
    public int getHistorySize() {
    	return histSize;
    }
    
    public String getScope() {
    	return scope;
    }
    
    public synchronized MonitorHistory getMonitorHistory(HttpSession session) {
        MonitorHistory mh=(MonitorHistory)sessionToHistory.get(session);
        if(mh==null) {
        	mh=new MonitorHistory(histSize);
            sessionToHistory.put(session,mh);
        }
        return mh;
    }
    
    public synchronized MonitorHistory getMonitorHistory(String ip) {
        MonitorHistory mh=(MonitorHistory)ipToHistory.get(ip);
        if(mh==null) {
            mh=new MonitorHistory(histSize);
            ipToHistory.put(ip,mh);
        }
        return mh;       
    }
    
    public synchronized void cleanup() {
        long time=System.currentTimeMillis()-(timeout*1000);
        /**
        Iterator it=sessionToHistory.keySet().iterator();
        while(it.hasNext()) {
            HttpSession session=(HttpSession)it.next();
            MonitorHistory hist=(MonitorHistory)sessionToHistory.get(session);
            if(hist.lastModified()<time) sessionToHistory.remove(session);
        }
        */
        Iterator it=ipToHistory.keySet().iterator();
        while(it.hasNext()) {
            String ip=(String)it.next();
            MonitorHistory hist=(MonitorHistory)ipToHistory.get(ip);
            if(hist.lastModified()<time) ipToHistory.remove(ip);
        }
    }

}
