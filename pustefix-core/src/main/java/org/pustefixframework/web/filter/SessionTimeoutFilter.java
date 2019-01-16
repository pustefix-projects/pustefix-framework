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
package org.pustefixframework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.pustefixframework.http.BotDetector;
import org.pustefixframework.web.ServletUtils;

/**
 * Servlet filter which supports setting a special session timeout to
 * reduce the number of unused sessions hanging around on the server side,
 * e.g. you can set an initial session timeout (shorter than the normal one)
 * which is only applied initially until a request threshold for the session
 * is reached, or you can set a special session timeout for known bots.
 */
public class SessionTimeoutFilter implements Filter {

    private static final String SESSION_ATTR_SESSION_TIMEOUT = "__PFX_SESSION_TIMEOUT__";
    private static final String SESSION_ATTR_REQUEST_COUNT = "__PFX_REQUEST_COUNT__";

    private int initialSessionTimeout = 0;
    private int initialRequestThreshold = 1;
    private int botSessionTimeout = 0;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String value = filterConfig.getInitParameter("initialSessionTimeout");
        if(value != null) {
            initialSessionTimeout = Integer.parseInt(value);
        }
        value = filterConfig.getInitParameter("initialRequestThreshold");
        if(value != null) {
            initialRequestThreshold = Integer.parseInt(value);
        }
        value = filterConfig.getInitParameter("botSessionTimeout");
        if(value != null) {
            botSessionTimeout = Integer.parseInt(value);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } finally {
            if(request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest)request;
                HttpSession session = req.getSession(false);
                if(session != null) {
                    Object sessionMutex = ServletUtils.getSessionMutex(session);
                    synchronized(sessionMutex) {
                        if(session.isNew() && botSessionTimeout > 0 && BotDetector.isBot(req)) {
                            //Set special timeout for requests coming from known bots.
                            session.setMaxInactiveInterval(botSessionTimeout);
                        }
                        if(initialSessionTimeout > 0) {
                            //Use 'initialTimeout' as session timeout until 'requestLimit' number
                            //of requests are made, then switch back to normal session timeout.
                            Integer attr = (Integer)session.getAttribute(SESSION_ATTR_REQUEST_COUNT);
                            int count = attr == null ? 1 : attr.intValue() + 1;
                            session.setAttribute(SESSION_ATTR_REQUEST_COUNT, count);
                            if(count == 1) {
                                session.setAttribute(SESSION_ATTR_SESSION_TIMEOUT, session.getMaxInactiveInterval());
                                session.setMaxInactiveInterval(initialSessionTimeout);
                            } else if(count == initialRequestThreshold + 1) {
                                Integer origTimeout = (Integer)session.getAttribute(SESSION_ATTR_SESSION_TIMEOUT);
                                if(origTimeout != null) {
                                    session.setMaxInactiveInterval(origTimeout);
                                    session.removeAttribute(SESSION_ATTR_SESSION_TIMEOUT);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Set special session timeout for initial requests.
     * @param initialSessionTimeout - session timeout in seconds
     */
    public void setInitialSessionTimeout(int initialSessionTimeout) {
        this.initialSessionTimeout = initialSessionTimeout;
    }

    /**
     * Set request threshold up to which a special session timeout
     * should be applied.
     * @param initialRequestThreshold - number of requests
     */
    public void setInitialRequestThreshold(int initialRequestThreshold) {
        this.initialRequestThreshold = initialRequestThreshold;
    }

    /**
     * Set special session timeout for requests coming from bots.
     * @param botSessionTimeout - session timeout in seconds
     */
    public void setBotSessionTimeout(int botSessionTimeout) {
        this.botSessionTimeout = botSessionTimeout;
    }

}