package de.schlund.pfixxml.serverutil.tomcat4;

import de.schlund.pfixxml.serverutil.ContainerUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.PfixServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Enumeration;
import java.util.Map;
import java.util.Iterator;

/**
 *
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

public class TomcatUtil
    implements ContainerUtil {

    private static final String ENC_STR = "jsessionid";

    public TomcatUtil() {
    }

    public Object getSessionValue(HttpSession session, String name) {
        Object rc = null;
        try {
            rc = session.getAttribute(name);
        } catch (NullPointerException e) {
        }
        return rc;
    }

    public void setSessionValue(HttpSession session, String name, Object val) {
        try {
            session.setAttribute(name, val);
        } catch (NullPointerException e) {
        }
    }

    public Object removeSessionValue(HttpSession session, String name) {
        Object rc = null;
        try {
            synchronized (session) {
                rc = session.getAttribute(name);
                session.removeAttribute(name);
            }
        } catch (NullPointerException e) {
        }
        return rc;
    }

    public void saveSessionData(Map store, HttpSession session) {
        try {
            Enumeration enum = session.getAttributeNames();
            while (enum.hasMoreElements()) {
                String valName = (String)enum.nextElement();
                store.put(valName, session.getAttribute(valName));
            }
        } catch (NullPointerException e) {
        }
    }

    public void copySessionData(Map store, HttpSession session) {
        try {
            Iterator iter = store.keySet().iterator();
            String key = null;
            while (iter.hasNext()) {
                key = (String)iter.next();
                if (!key.equals(SessionAdmin.LISTENER)) {
                    session.setAttribute(key, store.get(key));
                }
            }
        } catch (NullPointerException e) {
        }
    }

    public String getClearedURI(PfixServletRequest req, HttpServletResponse res) {
        StringBuffer rcBuf = new StringBuffer();
        stripUriSessionId(null, req.getRequestURI(res), rcBuf);
        return rcBuf.toString();
    }

    public String getClearedURI(HttpServletRequest req, HttpServletResponse res) {
        StringBuffer rcBuf = new StringBuffer();
        stripUriSessionId(null, req.getRequestURI(), rcBuf);
        return rcBuf.toString();
    }

    public String getClearedURL(String scheme, String host,
            HttpServletRequest req, HttpServletResponse res) {
        if (scheme == null) scheme = req.getScheme();
        if (host == null) host = req.getServerName();
        StringBuffer rcBuf = new StringBuffer();
        rcBuf.append(scheme).append("://").append(host);
        
        stripUriSessionId(null, req.getRequestURI(), rcBuf);

        String query = req.getQueryString();
        if (query != null && 0 < query.length()) {
            rcBuf.append('?').append(query);
        }
        return rcBuf.toString();
    }

    public String getURLSessionId(HttpServletRequest req, HttpServletResponse res) {
        String rc = ENC_STR + "=" + req.getSession(false).getId();
        return rc;
    }

    public String encodeURI(HttpServletRequest req, HttpServletResponse res) {
        StringBuffer rcBuf = new StringBuffer();

        String oldSessionId = stripUriSessionId(null, req.getRequestURI(), rcBuf);
        
        HttpSession session = req.getSession(false);
        if (session != null) {
            rcBuf.append(';').append(ENC_STR).append('=').append(session.getId());
        } else if (oldSessionId != null && 0 < oldSessionId.length()) {
            rcBuf.append(';').append(ENC_STR).append('=').append(oldSessionId);
        }

        return rcBuf.toString();
    }

    public String encodeURL(String scheme, String host,
            HttpServletRequest req, HttpServletResponse res) {
        return encodeURL(scheme, host, req, res, null);
    }

    
    public String encodeURL(String scheme, String host,
            HttpServletRequest req, HttpServletResponse res, String sessid) {
        if (scheme == null) scheme = req.getScheme();
        if (host == null) host = req.getServerName();
        StringBuffer rcBuf = new StringBuffer();
        rcBuf.append(scheme).append("://").append(host);
        
        String oldSessionId = stripUriSessionId(null, req.getRequestURI(), rcBuf);
        
        HttpSession session = req.getSession(false);
        if (sessid != null) {
            rcBuf.append(';').append(ENC_STR).append('=').append(sessid);
        } else if (session != null) {
            rcBuf.append(';').append(ENC_STR).append('=').append(session.getId());
        } else if (oldSessionId != null && 0 < oldSessionId.length()) {
            rcBuf.append(';').append(ENC_STR).append('=').append(oldSessionId);
        }
       
        String query = req.getQueryString();
        if (query != null && 0 < query.length()) {
            rcBuf.append('?').append(query);
        }
        return rcBuf.toString();
    }

    public String getContextPath(HttpServletRequest req) {
        return req.getContextPath();
    }

    protected String stripUriSessionId(String oldSessionId, String uri, StringBuffer rcUri) {
        String rc = oldSessionId;
        try {
            int semiIdx = uri.indexOf(";jsessionid");
            if (0 <= semiIdx) {
                rc = uri.substring(semiIdx + 1);
                uri = uri.substring(0, semiIdx);
            }
            
            if (uri.startsWith("/jsessionid")) {
                int nextSlash = uri.indexOf('/', 1);
                if (0 < nextSlash) {
                    rc = uri.substring(1, nextSlash);
                    uri = uri.substring(nextSlash);
                } else {
                    rc = uri.substring(1);
                }
            }

            if (uri.length() == 0) uri = "/";

            rcUri.append(uri);
        } catch (NullPointerException e) {
        }
        return rc;
    }

}
