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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import de.schlund.pfixxml.PfixServletRequest;

import java.util.Map;

/**
 * Describe interface <code>ContainerUtil</code> here.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public interface ContainerUtil {
    public static final String SESSION_ID_URL = "__SESSION_ID_URL__";
    
    public Object getSessionValue(HttpSession session, String name);
    public void   setSessionValue(HttpSession session, String name, Object val);
    public Object removeSessionValue(HttpSession session, String name);
    public void   saveSessionData(Map store, HttpSession session);
    public void   copySessionData(Map store, HttpSession session);
    public String getURLSessionId(HttpServletRequest req, HttpServletResponse res);
    public String getClearedURI(PfixServletRequest req, HttpServletResponse res);
    public String getClearedURL(String scheme, String host, HttpServletRequest req, HttpServletResponse res);
    public String encodeURL(String scheme, String host,
                            HttpServletRequest req, HttpServletResponse res);
    public String encodeURL(String scheme, String host,
                            HttpServletRequest req, HttpServletResponse res, String id);
    public String encodeURI(HttpServletRequest req, HttpServletResponse res);
    public String getContextPath(HttpServletRequest req);
}
