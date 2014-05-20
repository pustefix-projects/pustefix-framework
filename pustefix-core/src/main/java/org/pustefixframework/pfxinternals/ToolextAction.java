package org.pustefixframework.pfxinternals;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.targets.TargetGenerator;

public class ToolextAction implements Action {

    private Logger LOG = Logger.getLogger(ToolextAction.class);
    
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
        
        TargetGenerator targetGenerator = pageContext.getApplicationContext().getBean(TargetGenerator.class);
        
        boolean toolExtEnabled = targetGenerator.getToolingExtensions();
        targetGenerator.setToolingExtensions(!toolExtEnabled);
        try {
            targetGenerator.forceReinit(); 
            pageContext.addMessage(PageContext.MessageLevel.INFO, 
                    (toolExtEnabled?"Disabled":"Enabled") + " TargetGenerator tooling extensions.");
        } catch (Exception e) {
            pageContext.addMessage(PageContext.MessageLevel.ERROR, "Error during TargetGenerator reload");
            LOG.error(e, e);
        }
        String referer = req.getHeader("Referer");
        if(referer != null && !referer.contains("pfxinternals")) {
            res.sendRedirect(referer);
        } else {
            res.sendRedirect(req.getContextPath()+ "/pfxinternals/actions");
        }
        
    }
    
}
