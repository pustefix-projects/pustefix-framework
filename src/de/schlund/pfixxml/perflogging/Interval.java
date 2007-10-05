/*
 * Created on 31.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

/**
 * @author jh
 *
 */
public class Interval {
    private long from;
    private long until;
    
    Interval(long from, long until) {
        this.from = from;
        this.until = until;
    }
    
    boolean contains(long value) {
        boolean ret = (value >= from && value < until);
        //System.out.println("Contains: "+value+":"+ret+"-->"+toString());
        return ret;
    }
    
    boolean isGreater(long value) {
        boolean ret = value >= until;
        //System.out.println("greater: "+value+":"+ret+"-->"+toString());
        return ret;
    }
    
    boolean isLess(long value) {
        boolean ret = value < from;
        //System.out.println("less: "+value+":"+ret+"-->"+toString());
        return ret;
    }
    
    public long getFrom() {
        return from;
    }
    
    public long getUntil() {
        return until;
    }
    
    public String toString() {
        return "["+from+"-"+until+"]";
    }

}
