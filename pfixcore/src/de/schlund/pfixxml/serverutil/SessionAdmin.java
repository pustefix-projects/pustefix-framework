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

import org.apache.log4j.*;

/**
 *
 *
 */

public class SessionAdmin implements HttpSessionBindingListener {
    public  static String       LISTENER       = "__SESSION_LISTENER__"; 
    public  static String       PARENT_SESS_ID = "__PARENT_SESSION_ID__";
    public  static final String SESSION_IS_SECURE             = "__SESSION_IS_SECURE__";
    private static SessionAdmin instance       = new SessionAdmin();
    private static final Logger LOG            = Logger.getLogger(SessionAdmin.class);
    /** Maps session to it's id. */
    private        HashMap<HttpSession, String> sessionid = new HashMap<HttpSession, String>();
    private        HashMap<String, SessionInfoStruct> sessioninfo = new HashMap<String, SessionInfoStruct>();
    private        HashMap      parentinfo     = new HashMap();
    private        HashMap      parentinfo_rev = new HashMap();
    
    private SessionAdmin() {}

    public String toString() {
        return "[Number of active Sessions: " + sessioninfo.keySet().size() + "]";
    }
    
    public void registerSession(HttpSession sess, String serverName, String remoteAddr) {
        registerSession(sess, null, serverName, remoteAddr);
    }
    
    /**
     * Register a new session.
     * @param session the session to register.
     * @param trailog a trailog from another session.
     * @param conutil. 
     */
    public void registerSession(HttpSession session, LinkedList trailog, String serverName, String remoteAddr) {
        SessionInfoStruct info = new SessionInfoStruct(session, trailog, serverName, remoteAddr);
        
        synchronized (sessioninfo) {
            session.setAttribute(LISTENER, this);
            sessionid.put(session, session.getId());
            sessioninfo.put(session.getId(), info);
        }
        synchronized (parentinfo) {
            synchronized (parentinfo_rev) {
                String parentid = (String)session.getAttribute(PARENT_SESS_ID);
                if (parentid != null && !parentid.equals("")) {
                    parentinfo.put(parentid, session);
                    parentinfo_rev.put(session.getId(), parentid);
                }
            }
        }
    }

    public boolean idWasParentSession(String id) {
        synchronized (parentinfo) {
            if (parentinfo.get(id) != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    public HttpSession getChildSessionForParentId(String id) {
        synchronized (parentinfo) {
            return (HttpSession) parentinfo.get(id);
        }
    }
    
    public void valueBound(HttpSessionBindingEvent event) {}
    public void valueUnbound(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        synchronized (sessioninfo) {
            // CAUTITION: as of Tomcat 5.5.10, session.getId throws an exception 
            // (see http://issues.apache.org/bugzilla/show_bug.cgi?id=36994)
            // This is why we need the sessionid maps.
            String      id      = sessionid.get(session);
            SessionInfoStruct sessinf = sessioninfo.get(id);
            if (sessinf != null) {
                synchronized (parentinfo) {
                    synchronized (parentinfo_rev) {
                        String parentid = (String) parentinfo_rev.get(id);
                        if (parentid != null) {
                            parentinfo.remove(parentid);
                            parentinfo_rev.remove(id);
                            LOG.debug("\n\n********* Removed parentid mapping " + parentid +
                                      "->" + id + " *********\n");
                        }
                    }
                }
            }
            sessioninfo.remove(id);
            sessionid.remove(session);
            LOG.debug("\n\n********* Invalidated Session " + id + " *********\n");
        }
    }

    public void touchSession(String servlet, String stylesheet, HttpSession sess) {
        synchronized (sessioninfo) {
            SessionInfoStruct info = (SessionInfoStruct) sessioninfo.get(sess.getId());
            if (info != null) {
                info.updateTimestamp(servlet, stylesheet);
            } else {
                LOG.warn("*** SessionInfoStruct for Session " + sess + " was NULL!!!");
            }
        }
    }

    public Set getAllSessionIds() {
        return sessioninfo.keySet();
    }

    public SessionInfoStruct getInfo(HttpSession sess) {
        synchronized (sessioninfo) {
            return ((SessionInfoStruct) sessioninfo.get(sess.getId()));
        }
    }
    
    public SessionInfoStruct getInfo(String sessid) {
        synchronized (sessioninfo) {
            return ((SessionInfoStruct) sessioninfo.get(sessid));
        }
    }

    public static SessionAdmin getInstance() {
        return instance;
    }

    public String getExternalSessionId(HttpSession session) {
        String result = "NOSUCHSESSION";
        if (session != null) { 
            Boolean secure   = (Boolean) session.getAttribute(SESSION_IS_SECURE);
            String  parentid = (String) session.getAttribute(PARENT_SESS_ID); 
            if (secure != null && secure.booleanValue() && parentid != null) {
                result = parentid;
            } else if (secure == null || !secure.booleanValue()) {
                result = session.getId();
            }
        }
        return result;
    }


}
