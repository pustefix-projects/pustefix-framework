package de.schlund.pfixxml.serverutil.jserv;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

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

public class ModernJServUtil implements ContainerUtil {
    private Category CAT = Category.getInstance(this.getClass());
    
    public ModernJServUtil() {
    }

    public Object getSessionValue(HttpSession session, String name) {
        Object rc = null;
        try {
            rc = session.getValue(name);
        } catch (NullPointerException e) {
        }
        return rc;
    }

    public void setSessionValue(HttpSession session, String name, Object val) {
        try {
            session.putValue(name, val);
        } catch (NullPointerException e) {
        }
    }

    public Object removeSessionValue(HttpSession session, String name) {
        Object rc = null;
        try {
            synchronized (session) {
                rc = session.getValue(name);
                session.removeValue(name);
            }
        } catch (NullPointerException e) {
        }
        return rc;
    }

    public void saveSessionData(Map store, HttpSession session) {
        try {
            String[] valNames = session.getValueNames();
            for (int i = 0; i < valNames.length; i++) {
                store.put(valNames[i], session.getValue(valNames[i]));
            }
        } catch (NullPointerException e) {
        }
    }

    public void copySessionData(Map store, HttpSession session) {
        try {
            Iterator iter = store.keySet().iterator();
            String key = null;
            Object value = null;
            while (iter.hasNext()) {
                key = (String) iter.next();
                value = store.get(key);
                if (value instanceof NoCopySessionData) {
                    CAT.debug("*** Will not copy a object implementing NoCopySessionData!!! ***");
                } else if (!key.equals(SessionAdmin.LISTENER)) {
                    session.putValue(key, value);
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

        StringBuffer queryBuf = new StringBuffer();
        stripQuerySessionId(null, req.getQueryString(), queryBuf);

        if (0 < queryBuf.length()) {
            rcBuf.append('?').append(queryBuf.toString());
        }
        return rcBuf.toString();
    }

    public String getURLSessionId(HttpServletRequest req, HttpServletResponse res) {
        String urlsess = res.encodeUrl("").substring(1);
        return urlsess;
    }


    protected String stripQuerySessionId(String oldSessionId, String query, StringBuffer rcBuf) {
        String rc = oldSessionId;
        try {
            int sessParamIdx = query.indexOf("JServSessionId");
            
            if (0 <= sessParamIdx) {
                int ampIdx = query.indexOf('&', sessParamIdx);
                if (1 < sessParamIdx) {
                    rcBuf.append(query.substring(0, sessParamIdx - 1));
                }
                if (0 < ampIdx) {
                    rc = query.substring(sessParamIdx, ampIdx);
                    ampIdx++;
                    if (ampIdx < query.length()) {
                        if (0 < rcBuf.length()) ampIdx--;
                        rcBuf.append(query.substring(ampIdx));
                    }
                } else {
                    rc = query.substring(sessParamIdx);
                }
            } else {
                rcBuf.append(query);
            }
        } catch (NullPointerException e) {
        }
        return rc;
    }

    public String encodeURI(HttpServletRequest req, HttpServletResponse res) {
        StringBuffer rcBuf = new StringBuffer();
        String oldSessionId = stripUriSessionId(null, req.getRequestURI(), rcBuf);

        String workSess = res.encodeUrl("");
        if (0 < workSess.length()) {
            workSess = workSess.substring(1);
        } else {
            workSess = oldSessionId;
        }
        if (workSess != null && 0 < workSess.length()) {
            rcBuf.append(';').append(workSess);
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
        
        StringBuffer uriBuf = new StringBuffer();
        String oldSessionId = stripUriSessionId(null, req.getRequestURI(), uriBuf);
        rcBuf.append(uriBuf.toString());

        StringBuffer queryBuf = new StringBuffer();
        oldSessionId = stripQuerySessionId(oldSessionId, req.getQueryString(), queryBuf);
        
        String workSess;
        if (sessid != null) {
            workSess = "/JServSessionId="+sessid;
        } else {
            workSess = res.encodeUrl("");
        }
        
        if (0 < workSess.length()) {
            workSess = workSess.substring(1);
        } else {
            workSess = oldSessionId;
        }
        if (workSess != null && 0 < workSess.length()) {
            rcBuf.append(';').append(workSess);
        }
        
        if (0 < queryBuf.length()) {
            rcBuf.append('?').append(queryBuf.toString());
        }
        return rcBuf.toString();
    }

    
    public String getContextPath(HttpServletRequest req) {
        return "";
    }

    private String tradStripUriSessionId(String oldSessionId, String uri, StringBuffer rcUri) {
        String rc = oldSessionId;
        try {
            if (uri.startsWith("/JServSessionId")) {
                int nextSlash = uri.indexOf('/', 1);
                if (0 < nextSlash) {
                    rc = uri.substring(1, nextSlash);
                    rcUri.append(uri.substring(nextSlash));
                } else {
                    rc = uri.substring(1);
                    rcUri.append('/');
                }
            } else {
                rcUri.append(uri);
            }
        } catch (NullPointerException e) {
        }
        return rc;
    }

    protected String stripUriSessionId(String oldSessionId, String uri, StringBuffer rcUri) {
        String rc = null;
        try {
            // FIXME FIXME: this was super.stripUriSessionId(...) this is just a quick fix
            rc = tradStripUriSessionId(oldSessionId, uri, rcUri);
            String workUri = rcUri.toString();
            int semiIdx = workUri.indexOf(";JServSessionId");
            if (0 <= semiIdx) {
                rc = rcUri.substring(semiIdx + 1);
                rcUri.delete(semiIdx, rcUri.length());
                if (rcUri.length() == 0) rcUri.append('/');
            }
        } catch (NullPointerException e) {
        }
        return rc;
    }



}
