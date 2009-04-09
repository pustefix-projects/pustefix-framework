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

import java.util.ArrayList;
import java.util.List;

/**
 * @author mleidig@schlund.de
 */
public class MonitorHistory {
    
    private int max;
    private List<MonitorRecord> records;
    private long lastMod;
    
    public MonitorHistory(int max) {
        this.max=max;
        records=new ArrayList<MonitorRecord>();
        lastMod=System.currentTimeMillis();
    }
    
    public synchronized void addRecord(MonitorRecord record) {
    	if(records.size()>=max) records.remove(0);
        records.add(record);
        lastMod=System.currentTimeMillis();
    }
    
    public synchronized MonitorRecord[] getRecords() {
    	MonitorRecord[] mr=new MonitorRecord[records.size()];
        for(int i=0;i<records.size();i++) mr[i]=records.get(i);
        return mr;
    }
    
    public synchronized long lastModified() {
    	return lastMod;
    }

}
