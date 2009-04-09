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

package de.schlund.pfixxml.serverutil;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 *
 */

public class SessionAdmin implements HttpSessionBindingListener, SessionAdminMBean, InitializingBean {
    
    public  static String       LISTENER       = "__SESSION_LISTENER__"; 
    public  static String       PARENT_SESS_ID = "__PARENT_SESSION_ID__";
    public  static final String SESSION_IS_SECURE             = "__SESSION_IS_SECURE__";
    private static final Logger LOG            = Logger.getLogger(SessionAdmin.class);
    /** Maps session to it's id. */
    private        HashMap<HttpSession, String> sessionid = new HashMap<HttpSession, String>();
    private        HashMap<String, SessionInfoStruct> sessioninfo = new HashMap<String, SessionInfoStruct>();
    private        HashMap<String,HttpSession>      parentinfo     = new HashMap<String,HttpSession>();
    private        HashMap<String,String>      parentinfo_rev = new HashMap<String,String>();
    private String projectName;
    
    
    public void afterPropertiesSet() throws Exception {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=SessionAdmin,project="+projectName);
            if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register SessionAdmin MBean!",x);
        } 
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
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
    public void registerSession(HttpSession session, LinkedList<SessionInfoStruct.TrailElement> trailog, String serverName, String remoteAddr) {
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

    public String getExternalSessionId(HttpSession session) {
        return retrieveExternalSessionId(session);
    }
    
    public static String retrieveExternalSessionId(HttpSession session) {
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
        Iterator<String> iter;
        String id;
        List<SessionData> lst;
        SessionInfoStruct info;
        
        lst = new ArrayList<SessionData>();
        iter = getAllSessionIds().iterator();
        while (iter.hasNext()) {
            id = (String) iter.next();
            info = getInfo(id);
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

    /**
     * Returns the total number of sessions from all JMX-registered SessionAdmin instances
     */
    public static int getTotalSessionNumber() {
        int sessionNo = 0;
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName queryName;
        try {
            queryName = new ObjectName("Pustefix:type=SessionAdmin,project=" + "*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Illegal JMX object query name", e);
        }
        Set<ObjectName> objectNames = mbeanServer.queryNames(queryName, null);
        for(ObjectName objectName:objectNames) {
            try {
                if(mbeanServer.isRegistered(objectName)) {
                    sessionNo += (Integer)mbeanServer.getAttribute(objectName, "SessionNumber");
                }
            } catch (JMException e) {
                LOG.error("Can't get session number from '" + objectName + "'.", e);
            } 
        }
        return sessionNo;
    }
    
}
