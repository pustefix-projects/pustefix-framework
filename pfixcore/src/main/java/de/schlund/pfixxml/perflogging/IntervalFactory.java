/*
 * Created on 08.06.2005
 *
 */
package de.schlund.pfixxml.perflogging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jh
 *
 */
public class IntervalFactory {
    private static IntervalFactory instance = new IntervalFactory();
    private int interval_size = 20;
    private int increase_factor = 2;
    private HashMap<String, List<Interval> > interval_category_map;
    
    
    private IntervalFactory() {
        interval_category_map = new HashMap<String, List<Interval>>();
    }
    
    
    
    public static IntervalFactory getInstance() {
        return instance;
    }
    
    public List<Interval> getIntervalForCategory(String category) {
        if(!interval_category_map.containsKey(category)) {
            List<Interval> intervals = createInterval();
            interval_category_map.put(category, intervals);
        }
        return interval_category_map.get(category);
    }
    
    Map<String, List<Interval>> getAllIntervals() {
        return interval_category_map;
    }
    
    private List<Interval> createInterval() {
        List<Interval> intervals = new ArrayList<Interval>(interval_size); 
        intervals.add(0, new Interval(0, 1));
        intervals.add(1, new Interval(1, increase_factor));
        
        for(int i=2; i<interval_size -1; i++) {
            long pre_until = (intervals.get(i-1)).getUntil(); 
            intervals.add(i,new Interval(
                     pre_until, pre_until * increase_factor)); 
        }
        
        intervals.add(interval_size -1, new Interval(
                (intervals.get(interval_size -2)).getUntil(), Long.MAX_VALUE));
        
       /* for(int i=0; i<interval_size; i++) {
            System.out.print(i+":->"+intervals.get(i)+"|");
            System.out.println();
        }*/
        return intervals;
    }
   
}
