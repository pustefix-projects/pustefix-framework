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

package de.schlund.pfixxml.exceptionhandler;

import java.util.Calendar;


/**
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 *  <br/>
 * This class handles incoming exceptions which will be checked by their stacktrace.
 */
class STraceCheckerContainer {

    //~ Instance/static variables ..............................................

    private int burst_          =0;
    private Calendar cal_       =null;
    private int currentburst_   =0;
    private int fullcount_      =0;
    private int limit_          =0;
    private String limitdim_    =null;
    private int oldcurrentburst_=0;
    private long startmilli_    =0;
    private String[] strace_    =null;

    //~ Constructors ...........................................................

    /**
     * Creates a new STraceCheckerContainer object.
     * @param limit see  <see>InstanceCheckerContainer#InstanceCheckerContainer(...)</see>
     * @param limit_dim see <see>InstanceCheckerContainer#InstanceCheckerContainer(...)</see>
     * @param burst see <see>InstanceCheckerContainer#InstanceCheckerContainer(...)</see>
     */
    STraceCheckerContainer(int limit, String limitdim, int burst) {
        this.limit_   =limit;
        this.limitdim_=limitdim;
        this.burst_   =burst;
        init();
    }

    //~ Methods ................................................................

    /**
     * Returns the current burst.
     * 
     * @return int
     */
    int getCurrentburst() {
        return currentburst_;
    }

    /**
     * Returns the fullcount.
     * 
     * @return int
     */
    int getFullcountnReset() {
        int tmp=fullcount_;
        fullcount_=0;
        return tmp;
    }

    /**
     * Get the point of time this checker matched the last time.
     * 
     * @return the point of time as a long.
     */
    long getLastMatch() {
        return startmilli_;
    }

    /**
     * Sets the stacktrace.
     * 
     * @param strace the stacktrace to set.
     */
    void setStrace(String[] strace) {
        this.strace_=strace;
    }

    /**
     * Get the current strace.
     * 
     * @return a String[] containing the lines of a stack trace.
     */
    String[] getStrace() {
        return strace_;
    }

    /**
     * Checks if the limit allows a match. 
     * 
     * @return An int as result. Returns FULL if the limit is exeeded. 
     * Returns MATCH if limit is not exeeded. Returns TRIGGER_FULL if limit is 
     * not exceeded but a generation of a report should be triggered.
     */
    int doesMatch() {
        PFUtil.getInstance().debug(
                "StraceChecker at entry : currentburst: " + currentburst_);
        long nowmilli=0;
        int diff     =0;
        int rate     =0;
        float sdiff  =0;
        int ds       =0;
        int status   =PFUtil.FULL;
        PFUtil tutil =PFUtil.getInstance();
        cal_         =Calendar.getInstance();
        nowmilli     =cal_.getTime().getTime();
        diff         =new Long(nowmilli - startmilli_).intValue();
        rate         =tutil.getRate(limitdim_);
        sdiff        =limit_ * diff / rate;
        ds           =new Double(Math.ceil(sdiff)).intValue();
        currentburst_+=ds;
        PFUtil.getInstance().debug(
                "STraceChecker: -->\nlastmatch=" + startmilli_ + "\n now=" + 
                nowmilli + "\n diff=" + diff + "\n sdiff=" + sdiff + 
                "\n ds=" + ds + " currentburst=" + currentburst_);
        if(currentburst_>burst_)
            currentburst_=burst_;
        if(currentburst_>0) {
            currentburst_--;
            cal_            =Calendar.getInstance();
            startmilli_     =cal_.getTime().getTime();
            status          =PFUtil.MATCH;
            if(oldcurrentburst_==0) {
                status=PFUtil.TRIGGER_MATCH;
            }
            oldcurrentburst_=currentburst_;
        } else {
            fullcount_++;
            status=PFUtil.FULL;
        }
        PFUtil.getInstance().debug(
                "StraceChecker at return : currentburst: " + currentburst_);
        return status;
    }

    /**
     * Initialises internal objects
     */
    private void init() {
        // 'this' is created by a InstanceChecker and already matched one time
        currentburst_   =burst_ - 1;
        oldcurrentburst_=currentburst_;
        cal_            =Calendar.getInstance();
        startmilli_     =cal_.getTime().getTime();
    }
} //STraceCheckerContainer