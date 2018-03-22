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

import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.config.project.SessionTimeoutInfo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import de.schlund.pfixxml.serverutil.SessionAdmin;

public class SessionTrackingInterceptor implements HandlerInterceptor, ServletContextAware, InitializingBean {

    private static final String SESSION_ATTR_REQUEST_COUNT = "__PFX_REQUEST_COUNT__";
    private static final String SESSION_ATTR_ORIGINAL_TIMEOUT = "__PFX_SESSION_ORIGINAL_TIMEOUT__";

    private ServletContext servletContext;
    private SessionAdmin sessionAdmin;
    private boolean forceSSL;
    private Properties properties;
    private SessionTimeoutInfo sessionTimeoutInfo;

    private SessionTrackingStrategy sessionTrackingStrategy;
    private BotSessionTrackingStrategy botSessionTrackingStrategy;
    private DefaultSessionTrackingStrategyContext defaultContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        SessionTrackingStrategyContext context;
        if(handler instanceof SessionTrackingStrategyContext) {
            context = (SessionTrackingStrategyContext)handler;
        } else {
            context = defaultContext;
        }

        boolean proceed;
        if(BotDetector.isBot(request)) {
            proceed = botSessionTrackingStrategy.handleRequest(request, response, context);
        } else {
            proceed = sessionTrackingStrategy.handleRequest(request, response, context);
        }

        if(sessionTimeoutInfo != null && proceed) {
            HttpSession session = request.getSession(false);
            if(session != null && context.needsSession()) {
                Integer count = (Integer)session.getAttribute(SESSION_ATTR_REQUEST_COUNT);
                if(count == null) {
                    count = 1;
                    session.setAttribute(SESSION_ATTR_ORIGINAL_TIMEOUT, session.getMaxInactiveInterval());
                    session.setMaxInactiveInterval(sessionTimeoutInfo.getInitialTimeout());
                } else {
                    if(count == sessionTimeoutInfo.getRequestLimit()) {
                        Integer origTimeout = (Integer)session.getAttribute(SESSION_ATTR_ORIGINAL_TIMEOUT);
                        if(origTimeout != null) {
                            session.setMaxInactiveInterval(origTimeout);
                        }
                    }
                    count++;
                }
                session.setAttribute(SESSION_ATTR_REQUEST_COUNT, count);
            }
        }

        return proceed;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setSessionAdmin(SessionAdmin sessionAdmin) {
        this.sessionAdmin = sessionAdmin;
    }

    public void setForceSSL(boolean forceSSL) {
        this.forceSSL = forceSSL;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setSessionTimeoutInfo(SessionTimeoutInfo sessionTimeoutInfo) {
        this.sessionTimeoutInfo = sessionTimeoutInfo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if(sessionTrackingStrategy == null) {
            Set<SessionTrackingMode> modes = servletContext.getEffectiveSessionTrackingModes();
            if(modes.contains(SessionTrackingMode.COOKIE)) {
                if(modes.contains(SessionTrackingMode.URL)) {
                    sessionTrackingStrategy = new CookieSessionTrackingStrategy(sessionAdmin, properties);
                } else {
                    sessionTrackingStrategy = new CookieOnlySessionTrackingStrategy(sessionAdmin, properties);
                }
            } else if(modes.contains(SessionTrackingMode.URL)) {
                sessionTrackingStrategy = new URLRewriteSessionTrackingStrategy(sessionAdmin, properties);
            }
        }
        botSessionTrackingStrategy = new BotSessionTrackingStrategy(sessionAdmin, properties);
        defaultContext = new DefaultSessionTrackingStrategyContext(forceSSL);
    }

}
