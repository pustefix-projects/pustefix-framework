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
    private ArrayList records;
    private long lastMod;
    
    public MonitorHistory(int max) {
        this.max=max;
        records=new ArrayList();
        lastMod=System.currentTimeMillis();
    }
    
    public synchronized void addRecord(MonitorRecord record) {
    	if(records.size()>=max) records.remove(0);
        records.add(record);
        lastMod=System.currentTimeMillis();
    }
    
    public synchronized MonitorRecord[] getRecords() {
    	MonitorRecord[] mr=new MonitorRecord[records.size()];
        for(int i=0;i<records.size();i++) mr[i]=(MonitorRecord)records.get(i);
        return mr;
    }
    
    public synchronized long lastModified() {
    	return lastMod;
    }

}
