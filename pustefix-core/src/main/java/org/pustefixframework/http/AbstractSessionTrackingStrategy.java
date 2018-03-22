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
 */
package org.pustefixframework.http;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.pustefixframework.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlund.pfixxml.serverutil.SessionAdmin;

public abstract class AbstractSessionTrackingStrategy implements SessionTrackingStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSessionTrackingStrategy.class);
    private static final Logger LOGGER_VISIT = LoggerFactory.getLogger("LOGGER_VISIT");

    private static final String SESSION_ATTR_USER_AGENT = "__PFX_USER_AGENT__";
    private static final String SESSION_ATTR_REMOTE_IP = "__PFX_REMOTE_IP__";

    private int INC_ID = 0;
    private String TIMESTAMP_ID = "";

    protected SessionAdmin sessionAdmin;
    protected Properties properties;

    public AbstractSessionTrackingStrategy(SessionAdmin sessionAdmin, Properties properties) {
        this.sessionAdmin = sessionAdmin;
        this.properties = properties;
    }

    protected void registerSession(HttpServletRequest req, HttpSession session) {
        if (session != null) {
            synchronized (TIMESTAMP_ID) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String timestamp = sdf.format(new Date());
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumIntegerDigits(3);

                if (timestamp.equals(TIMESTAMP_ID)) {
                    INC_ID++;
                } else {
                    TIMESTAMP_ID = timestamp;
                    INC_ID = 0;
                }
                if (INC_ID >= 1000) {
                    LOG.warn("*** More than 999 connects/sec! ***");
                }
                String sessid = session.getId();
                String mach = "";
                if (sessid.lastIndexOf(".") > 0) {
                    mach = sessid.substring(sessid.lastIndexOf("."));
                }
                session.setAttribute(AbstractPustefixRequestHandler.VISIT_ID, TIMESTAMP_ID + "-" + nf.format(INC_ID) + mach);
            }
            session.setAttribute(SessionUtils.SESSION_ATTR_LOCK, new ReentrantReadWriteLock());
            StringBuffer logbuff = new StringBuffer();
            logbuff.append(session.getAttribute(AbstractPustefixRequestHandler.VISIT_ID) + "|" + session.getId() + "|");
            logbuff.append(LogUtils.makeLogSafe(AbstractPustefixRequestHandler.getServerName(req)) + "|" +
                           LogUtils.makeLogSafe(AbstractPustefixRequestHandler.getRemoteAddr(req)) + "|");
            logbuff.append(LogUtils.makeLogSafe(req.getHeader("user-agent")) + "|");
            if (req.getHeader("referer") != null) {
                logbuff.append(LogUtils.makeLogSafe(req.getHeader("referer")));
            }
            logbuff.append("|");
            if (req.getHeader("accept-language") != null) {
                logbuff.append(LogUtils.makeLogSafe(req.getHeader("accept-language")));
            }
            LOGGER_VISIT.warn(logbuff.toString());
            sessionAdmin.registerSession(session, AbstractPustefixRequestHandler.getServerName(req), req.getRemoteAddr());
        }
    }

    protected boolean checkClientIdentity(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if(session != null) {
            String storedIp = (String)session.getAttribute(SESSION_ATTR_REMOTE_IP);
            if(storedIp != null) {
                String ip = AbstractPustefixRequestHandler.getRemoteAddr(req);
                if(!ip.equals(storedIp)) {
                    LOG.warn("Differing client IP: " + ip + " " + storedIp);
                    return false;
                }
            }
            String storedUserAgent = (String)session.getAttribute(SESSION_ATTR_USER_AGENT);
            if(storedUserAgent != null) {
                String userAgent = req.getHeader("User-Agent");
                if(userAgent == null) userAgent = "-";
                if(!userAgent.equals(storedUserAgent)) {
                    LOG.warn("Differing client useragent: " + userAgent + " " + storedUserAgent);
                    return false;
                }
            }
        }
        return true;
    }

    protected void storeClientIdentity(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if(session != null) {
            String ip = AbstractPustefixRequestHandler.getRemoteAddr(req);
            session.setAttribute(SESSION_ATTR_REMOTE_IP, ip);
            String userAgent = req.getHeader("User-Agent");
            if(userAgent == null) {
                userAgent = "-";
            }
            session.setAttribute(SESSION_ATTR_USER_AGENT, userAgent);
        }
    }

}
