/*
 * Created on 01.08.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

import java.util.ArrayList;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MonitorHistory {
    
    private int max;
    private ArrayList entries;
    private long lastMod;
    
    public MonitorHistory(int max) {
        this.max=max;
        entries=new ArrayList();
        lastMod=System.currentTimeMillis();
    }
    
    public synchronized void addEntry(HttpRequest req) {
    	if(entries.size()>=max) entries.remove(0);
        entries.add(req);
        lastMod=System.currentTimeMillis();
    }
    
    public synchronized HttpRequest[] getEntries() {
    	HttpRequest[] hr=new HttpRequest[entries.size()];
        for(int i=0;i<entries.size();i++) hr[i]=(HttpRequest)entries.get(i);
        return hr;
    }
    
    public synchronized long lastModified() {
    	return lastMod;
    }

}
