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
 */

package de.schlund.pfixxml.serverutil;

import java.util.LinkedList;

import javax.servlet.http.HttpSession;

/**
 *
 *
 */   

public class SessionInfoStruct {
    private int           max_trail_elem = 25;
    private HttpSession   session;
    private SessionData   data;
    private long          numberofhits;
    private LinkedList<TrailElement> traillog;
    
   
   /**
    * Create a SessionInfoStruct with the given session, traillog and conutil.
    * @param session
    * @param trailog a trailog from another session used as the inital value for the trailog of this SessionInfoStruct.
                     May be null.
    * @param conutil 
    */
    public SessionInfoStruct(HttpSession session, LinkedList<TrailElement> traillog, String serverName, String remoteAddr) {
        this.session  = session;
        this.data = new SessionData(session.getId(), serverName, remoteAddr);
        numberofhits  = 0;
        if (traillog != null) {
            this.traillog = traillog;
        } else {
            this.traillog = new LinkedList<TrailElement>();
        }
    }
    
    public SessionData getData() {
        return data;
    }
    
    public void updateTimestamp(String servlet, String stylesheet) {
        data.updateTimestamp();
        numberofhits++;
        synchronized(traillog) {
            traillog.addLast(new TrailElement(servlet,stylesheet,numberofhits));
            if (traillog.size() > max_trail_elem) {
                traillog.removeFirst();
            }
        }
    }

    public LinkedList<SessionInfoStruct.TrailElement> getTraillog() {return traillog;}
    
    /**
     * Get the number of times the session has been accessed (access to subframes isn't counted)
     * @return value of numberofhits
     */
    public long getNumberOfHits() {return numberofhits;}

    
    public int getMaxTrailElem() {return max_trail_elem;}

    public void setMaxTrailElem(int max) {
        max_trail_elem = max;
    }
    
    public HttpSession getSession() {
        return session;
    }

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


