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

package de.schlund.pfixxml.serverutil;

import java.util.*;
import javax.servlet.http.*;

/**
 *
 *
 */   

public class SessionInfoStruct {
    private int           max_trail_elem = 25;
    private HttpSession   session;
    private long          creationtime;
    private long          lastaccess;
    private long          numberofhits;
    private LinkedList    traillog;
    
   
   /**
    * Create a SessionInfoStruct with the given session, traillog and conutil.
    * @param session
    * @param trailog a trailog from another session used as the inital value for the trailog of this SessionInfoStruct.
                     May be null.
    * @param conutil 
    */
    public SessionInfoStruct(HttpSession session, LinkedList traillog) {
        this.session  = session;
        creationtime  = new Date().getTime();
        lastaccess    = -1L;
        numberofhits  = 0;
        if (traillog != null) {
            this.traillog = traillog;
        } else {
            this.traillog = new LinkedList();
        }
        
        
    }
    
    
    public void updateTimestamp(String servlet, String stylesheet) {
        lastaccess = new Date().getTime();
        numberofhits++;
        synchronized(traillog) {
            traillog.addLast(new TrailElement(servlet,stylesheet,numberofhits));
            if (traillog.size() > max_trail_elem) {
                traillog.removeFirst();
            }
        }
    }

    public LinkedList getTraillog() {return traillog;}
    
    /**
     * Get the number of times the session has been accessed (access to subframes isn't counted)
     * @return value of numberofhits
     */
    public long getNumberOfHits() {return numberofhits;}

    
    public int getMaxTrailElem() {return max_trail_elem;}

    public void setMaxTrailElem(int max) {
        max_trail_elem = max;
    }
    
    /**
     * Get the value of URI part with sessid.
     * @return value of URI part with sessid.
     */
    public String getSessionIdURI() {
        return (String)session.getAttribute(SessionHelper.SESSION_ID_URL);
    }
    
    public HttpSession getSession() {
        return session;
    }

    /**
     * Get the value of creationtime.
     * @return value of creationtime.
     */
    public long getCreationTime() {return creationtime;}
    
    /**
     * Set the value of creationtime.
     * @param v  Value to assign to creationtime.
     */
    public void setCreationTime(long  v) {this.creationtime = v;}
    
    /**
     * Get the value of the last access to the session (access to subframes isn't counted)
     * @return value of lastAccess.
     */
    public long getLastAccess() {return lastaccess;}
    
    /**
     * Set the value of lastAccess.
     * @param v  Value to assign to lastAccess.
     */
    public void setLastAccess(long  v) {this.lastaccess = v;}


    public class TrailElement {
        String servletname;
        String stylesheetname;
        long    count;
        
        public TrailElement(String sname, String ssheet, long c) {
            servletname    = sname;
            stylesheetname = ssheet;
            count          = c;
        }

        public String getServletname() {return servletname;}

        public String getStylesheetname() {return stylesheetname;}

        public long getCounter() {return count;}
    }
}


