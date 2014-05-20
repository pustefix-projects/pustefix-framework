package org.pustefixframework.pfxinternals;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.serverutil.SessionAdmin;

public class InvalidateAction implements Action {
    
    private Logger LOG = Logger.getLogger(InvalidateAction.class);
    
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
        
        SessionAdmin sessionAdmin = pageContext.getApplicationContext().getBean(SessionAdmin.class);
        
        String session = req.getParameter("session");
        String page = req.getParameter("page");
        if(session == null) {
            sessionAdmin.invalidateSessions();
            LOG.info("Invalidated sessions.");
        } else {
            sessionAdmin.invalidateSession(session);
            LOG.info("Invalidated session: " + session);
        }
        if(page == null) {
            res.sendRedirect(req.getContextPath()+ "/pfxinternals/actions");
        } else {
            String url = req.getRequestURI();
            url = url.replace("pfxinternals", req.getParameter("page"));
            res.sendRedirect(url.toString());
        }
        
    }

}
