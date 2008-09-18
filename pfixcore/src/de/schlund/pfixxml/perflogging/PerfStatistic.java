/*
 * Created on 08.06.2005
 *
 */
package de.schlund.pfixxml.perflogging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * @author jh
 *
 */
public class PerfStatistic {
    
    private PerfLogging perfLogging;
    
    private Map<String,Map<String, int[]>> category_map = 
        new HashMap<String, Map<String, int[]>>(); 
    
    private int[] createCount(int size) {
        int[] count = new int[size];
        return count;
    }
        
    public PerfStatistic(PerfLogging perfLogging) {
        this.perfLogging = perfLogging;
    }
    
   synchronized void reset() {
       category_map = new HashMap<String, Map<String, int[]>>();
   }

    /**
     * @param pe
     */
   synchronized void process(PerfEvent pe) {
      
       List<Interval> intervals = IntervalFactory.getInstance().getIntervalForCategory(pe.getCategory());
        
        if(!category_map.containsKey(pe.getCategory())) {
            category_map.put(pe.getCategory(), new HashMap<String, int[]>());
        }
        
        Map<String, int[]> identity_map = category_map.get(pe.getCategory());
        
        if(!identity_map.containsKey(pe.getIdentifier())) {
            int[] c = createCount(intervals.size());
            identity_map.put(pe.getIdentifier(), c);
        }
        
        int[] count = identity_map.get(pe.getIdentifier()); 
        int index = search(pe.getDuration(), intervals);
        count[index]++;
    }
    
   synchronized String toXML() {
        StringBuffer sb = new StringBuffer(1024);
        if(category_map.isEmpty()) return "";
        
        Formatter v = new XMLFormatter(perfLogging);
        format(sb, v);
        
        return sb.toString();
    }
   
    synchronized String toLogfilePresentation() {
        StringBuffer sb = new StringBuffer(1024);
        if(category_map.isEmpty()) return "";
        
        Formatter v = new StringFormatter();
        format(sb, v);
        
        return sb.toString();
    }

    synchronized Map<String, Map<String, int[]>> toMap() {        
        if(category_map.isEmpty()) return null;
        return category_map;        
    }
    
    
    /**
     * @param sb
     */
    private void format(StringBuffer sb, Formatter v) {
        v.printHeader(sb);
        for(Iterator<String> i = category_map.keySet().iterator(); i.hasNext();) {
            String category = (String) i.next();
            v.categoryStart(sb, category);
            Map<String,int[]> identity_map = category_map.get(category);
            for(Iterator<String> j = identity_map.keySet().iterator(); j.hasNext(); ) {
                String identfier = (String) j.next();
                int[] cc = (int[]) identity_map.get(identfier);
                List<Interval> intervals = IntervalFactory.getInstance().getIntervalForCategory(category);
                
                int count = 0;
                int total = getTotal(category, identfier);
                
                v.identfierStart(sb, identfier, total);
                
                for(Iterator<Interval> k = intervals.iterator(); k.hasNext();) {
                    Interval interval = (Interval) k.next();
                   // if(cc[count] > 0) {
                        int per = getPercent(cc, count, total);
                        v.formatCountElement(sb, cc, count, interval, per);
                   // }
                    count++;
                }
                v.identierEnd(sb);
            }
            v.catgeoryStop(sb);
        }
        v.printFooter(sb);
        
    }


    
   


    /**
     * @param cc
     * @param count
     * @param total
     * @return
     */
    private int getPercent(int[] cc, int count, int total) {
        int per = total == 0 ? total : (cc[count] * 100 / total);
        return per;
    }
    
    
    private int getTotal(String category, String ident) {
        Map<String,int[]> identity_map = category_map.get(category);
        int[] cc = (int[]) identity_map.get(ident);
        List<Interval> intervals = IntervalFactory.getInstance().getIntervalForCategory(category);
        int count = 0;
        int total = 0;
        for(Iterator<Interval> k = intervals.iterator(); k.hasNext(); k.next()) {
            if(cc[count] > 0) {
                total += cc[count];
            }
            count++;
        }
        return total;
    }
    
    
    String toStr() {
        return category_map.toString();
    }
  
    
    
    private int search(long time, List<Interval> intervals) {
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

interface Formatter {
     void printHeader(StringBuffer sb);
     void printFooter(StringBuffer sb);
    void formatCountElement(StringBuffer sb, int[] cc, int count, Interval interval, int per);
     void identfierStart(StringBuffer sb, String id, int total);
     void identierEnd(StringBuffer sb);
     void categoryStart(StringBuffer sb, String category);
     void catgeoryStop(StringBuffer sb);
}

class StringFormatter implements Formatter {
   
    
    public void printHeader(StringBuffer sb) {
       sb.append("\n----------------------------------------------"); 
       sb.append("\n*** Performance ***").append("\n");
   }

    public void formatCountElement(StringBuffer sb, int[] cc, int count, Interval interval, int per) {
        sb.append("\t\t["+interval.getFrom()+
                   "-"+interval.getUntil()+"]"+  
                   "=>"+cc[count]+"|"+per).append("\n");
    }

    public void identfierStart(StringBuffer sb, String identifier, int total) {
        sb.append("\t"+identifier+"["+total+"]"+":");
    }

    public void categoryStart(StringBuffer sb, String category) {
        sb.append(category);
    }

    public void identierEnd(StringBuffer sb) {
        sb.append("\n");
    }

    public void catgeoryStop(StringBuffer sb) {
        sb.append("\n");
    }

    public void printFooter(StringBuffer sb) {
        sb.append("----------------------------------------------\n");
    }
}

class XMLFormatter implements Formatter {

    private PerfLogging perfLogging;
    
    public XMLFormatter(PerfLogging perfLogging) {
        this.perfLogging = perfLogging;
    }
    
    public void printHeader(StringBuffer sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<perf>").append("\n");
        sb.append("<status>").append("\n");
        sb.append("<active>").append(perfLogging.isPerfLoggingActive()).
        append("</active>").append("\n");
        
        sb.append("</status>").append("\n");
    }

    public void printFooter(StringBuffer sb) {
        sb.append("</perf>").append("\n");
    }

    public void formatCountElement(StringBuffer sb, int[] cc, int count, Interval interval, int per) {
        sb.append("<count from=\""+interval.getFrom()+
                "\" to=\""+interval.getUntil()+
                "\" per=\""+per+
                "\">"+cc[count++]+"</count>").append("\n");
    }

    public void identfierStart(StringBuffer sb, String id, int total) {
        sb.append("<id name=\""+id+"\"  total=\""+total+"\">").append("\n");
    }

    public void identierEnd(StringBuffer sb) {
        sb.append("</id>").append("\n");
    }

    public void categoryStart(StringBuffer sb, String category) {
        sb.append("<category name=\""+category+"\">").append("\n");
    }

    public void catgeoryStop(StringBuffer sb) {
        sb.append("</category>").append("\n");
    }
    
}
