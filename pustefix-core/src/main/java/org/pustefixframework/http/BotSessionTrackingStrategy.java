package org.pustefixframework.http;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionHelper;

public class BotSessionTrackingStrategy extends AbstractSessionTrackingStrategy {

    public BotSessionTrackingStrategy(SessionAdmin sessionAdmin, Properties properties) {
        super(sessionAdmin, properties);
    }

    public boolean handleRequest(HttpServletRequest req, HttpServletResponse res,
            SessionTrackingStrategyContext context) throws ServletException, IOException {
        
        if(req.getRequestedSessionId() != null && req.isRequestedSessionIdFromURL()) {
            String redirect_uri = SessionHelper.getClearedURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, properties);
            AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
            return false;
        }
        
        PfixServletRequest preq = new PfixServletRequestImpl(req, properties, context);
        
        if(!req.isSecure() && context.needsSSL(preq)) {
            String redirect_uri = SessionHelper.getClearedURL("https", AbstractPustefixRequestHandler.getServerName(req), req, properties);
            AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
            return false;
        }
        
        HttpSession session = null;
        if(context.needsSession()) {
            session = req.getSession(false);
            if(session == null) {
                session = req.getSession(true);
                registerSession(req, session);
                session.setMaxInactiveInterval(30);
            } else if(session.isNew() && session.getAttribute(AbstractPustefixRequestHandler.VISIT_ID) == null) {
                //Assimilate session created within this request but outside of Pustefix
                registerSession(req, session);
                session.setMaxInactiveInterval(30);
            } else {
                if(session.getMaxInactiveInterval() == 30) {
                    session.setMaxInactiveInterval(5 * 60);
                }
            }
            session.setAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION, true);
            preq.updateRequest(req);
        }

        req.setAttribute(PfixServletRequest.class.getName(), preq);
        return true;
    }
    
}
