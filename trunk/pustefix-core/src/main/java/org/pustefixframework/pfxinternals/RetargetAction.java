package org.pustefixframework.pfxinternals;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.targets.TargetGenerator;

public class RetargetAction implements Action {

    private Logger LOG = Logger.getLogger(RetargetAction.class);
    
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
        
        TargetGenerator targetGenerator = pageContext.getApplicationContext().getBean(TargetGenerator.class);
        try {
            targetGenerator.forceReinit();
            LOG.info("Reloaded TargetGenerator with cleared cache.");
        } catch (Exception e) {
            LOG.error("Error while clearing TargetGenerator cache", e);
        }
        String referer = req.getHeader("Referer");
        if(referer != null && !referer.contains("pfxinternals")) {
            res.sendRedirect(referer);
            return;
        } else {
            res.sendRedirect(req.getContextPath()+ "/pfxinternals/actions");
            return;
        }
        
    }
    
}
