/*
 * Created on 30.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;



/**
 * @author jh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PerfLogServlet extends HttpServlet {
    private String  PARAM_ACTIVATE = "active"; 
    private String  PARAM_DUMP = "dump";
    private String ON = "1";
    private String OFF = "0";
    private static Logger LOG = Logger.getLogger(PerfLogServlet.class);
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/plain");
        
        if(req.getParameter(PARAM_DUMP) != null && req.getParameter(PARAM_DUMP).equals(ON)) {
            LOG.info("Triggering dump");
            PerfLogging.getInstance().triggerDump();
        }
        
        if(req.getParameter(PARAM_ACTIVATE) != null) {
            if(req.getParameter(PARAM_ACTIVATE).equals(ON)) {
                LOG.info("Activating perflogging");
                PerfLogging.getInstance().activatePerflogging();
            } else if(req.getParameter(PARAM_ACTIVATE).equals(OFF)) {
                LOG.info("Inactivating perflogging");
                PerfLogging.getInstance().inactivatePerflogging();
            }
        }
        
        ServletOutputStream os = res.getOutputStream();
        os.println(PerfStatistic.getInstance().toXML());
    }
}
