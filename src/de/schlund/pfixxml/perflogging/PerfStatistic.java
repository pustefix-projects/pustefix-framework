/*
 * Created on 08.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



/**
 * @author jh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PerfStatistic {
    private HashMap category_map = new HashMap(); 
    
    private static PerfStatistic instance = new PerfStatistic();
    
    
    static PerfStatistic getInstance() {
        return instance;
    }
    
    
    private int[] createCount(int size) {
        int[] count = new int[size];
        return count;
    }
        
   synchronized void reset() {
       category_map = null;
   }

    /**
     * @param pe
     */
   synchronized void process(PerfEvent pe) {
      
       
        List intervals = IntervalFactory.getInstance().getIntervalForCategory(pe.getCategory());
        
        if(!category_map.containsKey(pe.getCategory())) {
            category_map.put(pe.getCategory(), new HashMap());
        }
        
        HashMap identity_map = (HashMap) category_map.get(pe.getCategory());
        
        if(!identity_map.containsKey(pe.getIdentifier())) {
            int[] c = createCount(intervals.size());
            identity_map.put(pe.getIdentifier(), c);
        }
        
        int[] count = (int[])identity_map.get(pe.getIdentifier()); 
        int index = search(pe.getDuration(), intervals);
        count[index]++;
    }
    
   synchronized String toXML() {
        StringBuffer sb = new StringBuffer(1024);
       
        sb.append("<perf>").append("\n");
        
        sb.append("<status>").append("\n");
        sb.append("<enabled>").append(PerfLogging.getInstance().isPerfLogggingEnabled()).
        append("</enabled>").append("\n");
        sb.append("<active>").append(PerfLogging.getInstance().isPerfLoggingActive()).
        append("</active>").append("\n");
        
        sb.append("</status").append("\n");
        
       
        
        for(Iterator i = category_map.keySet().iterator(); i.hasNext();) {
            String category = (String) i.next();
            sb.append("<category name=\""+category+"\">").append("\n");
            
            HashMap identity_map = (HashMap)category_map.get(category);
            
            for(Iterator j = identity_map.keySet().iterator(); j.hasNext(); ) {
                String identfier = (String) j.next();
                sb.append("<id name=\""+identfier+"\">").append("\n");
                
                int[] cc = (int[]) identity_map.get(identfier);
                
                List intervals = IntervalFactory.getInstance().getIntervalForCategory(category);
                
                int count = 0;
                for(Iterator k = intervals.iterator(); k.hasNext();) {
                    Interval interval = (Interval) k.next();
                    sb.append("<count from=\""+interval.getFrom()+
                               "\" to=\""+interval.getUntil()+  
                               "\">"+cc[count++]+"</count>").append("\n");
                }
                sb.append("</id>").append("\n");
            }
            
            
            sb.append("</category>").append("\n");
        }
        sb.append("</perf>").append("\n");
        return sb.toString();
    }
   
    synchronized String toLogfilePresentation() {
        StringBuffer sb = new StringBuffer(1024);
        if(category_map.isEmpty()) return "";
        
        sb.append("\n----------------------------------------------"); 
        sb.append("\n*** Performance ***").append("\n");
        
        for(Iterator i = category_map.keySet().iterator(); i.hasNext();) {
            String category = (String) i.next();
            sb.append(category).append("\n");
            
            HashMap identity_map = (HashMap)category_map.get(category);
            
            for(Iterator j = identity_map.keySet().iterator(); j.hasNext(); ) {
                String identfier = (String) j.next();
                sb.append("\t"+identfier+":").append("\n");
                
                int[] cc = (int[]) identity_map.get(identfier);
                
                List intervals = IntervalFactory.getInstance().getIntervalForCategory(category);
                
                int count = 0;
                for(Iterator k = intervals.iterator(); k.hasNext();) {
                    Interval interval = (Interval) k.next();
                    if(cc[count] > 0) {
                        sb.append("\t\t["+interval.getFrom()+
                               "-"+interval.getUntil()+"]"+  
                               "=>"+cc[count]).append("\n");
                    }
                    count++;
                }
                
            }
        }
        sb.append("----------------------------------------------\n");
        return sb.toString();
    }
    
    String toStr() {
        return category_map.toString();
    }
  
    
    
    private int search(long time, List intervals) {
        boolean success = false;
        int first = 0;
        int last = intervals.size();
        int middle = 0;
        int index = 0;
        
        if(time >= Long.MAX_VALUE) {
            time = Long.MAX_VALUE - 1 ;
        }
        
        while (!success && first <= last) {

            middle = (first + last) / 2;
            /*System.out.println("Middle=" + middle + " first=" + first
                    + ", last=" + last + ", time=" + time);*/

            Interval midd = (Interval) intervals.get(middle);
            if (midd.contains(time)) {
                index = middle;
                success = true;
                break;
            }

            // search left
            if (midd.isLess(time)) {
                last = middle - 1;
            }
            // search right
            if (midd.isGreater(time)) {
                first = middle + 1;
            }

        }
        return index;
    }

}
