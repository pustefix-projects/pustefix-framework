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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.*;

import org.apache.log4j.*;

/**
 *
 *
 */

public class SessionAdmin implements HttpSessionBindingListener, SessionAdminMBean {
    
    public  static String       LISTENER       = "__SESSION_LISTENER__"; 
    public  static String       PARENT_SESS_ID = "__PARENT_SESSION_ID__";
    public  static final String SESSION_IS_SECURE             = "__SESSION_IS_SECURE__";
    private static SessionAdmin instance       = new SessionAdmin();
    private static final Logger LOG            = Logger.getLogger(SessionAdmin.class);
    /** Maps session to it's id. */
    private        HashMap<HttpSession, String> sessionid = new HashMap<HttpSession, String>();
    private        HashMap<String, SessionInfoStruct> sessioninfo = new HashMap<String, SessionInfoStruct>();
    private        HashMap<String,HttpSession>      parentinfo     = new HashMap<String,HttpSession>();
    private        HashMap<String,String>      parentinfo_rev = new HashMap<String,String>();
    
    private SessionAdmin() {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=SessionAdmin"); 
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register SessionAdmin MBean!",x);
        } 
    }

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

    public Set<String> getAllSessionIds() {
        return sessioninfo.keySet();
    }

    public int getSessionNumber() {
        return sessioninfo.size();
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
    
    //accessible via JMX:
    
    public List<SessionData> getSessions(String serverName, String remoteAddr) {
        SessionAdmin admin;
        Iterator<String> iter;
        String id;
        List<SessionData> lst;
        SessionInfoStruct info;
        
        lst = new ArrayList<SessionData>();
        admin = SessionAdmin.getInstance();
        iter = admin.getAllSessionIds().iterator();
        while (iter.hasNext()) {
            id = (String) iter.next();
            info = admin.getInfo(id);
            if (serverName.equals(info.getData().getServerName()) && remoteAddr.equals(info.getData().getRemoteAddr())) {
                lst.add(info.getData());
            }
        }
        return lst;
    }
    
    public void invalidateSession(String id) throws IOException {
        getSession(id).invalidate();
    }
    
    public HttpSession getSession(String id) throws IOException {
        SessionInfoStruct info = getInfo(id);
        if (info == null) {
            throw new IOException("session not found: " + id);
        }
        return info.getSession();
    }

}
