/*
 * Created on 14.10.2003
 *  
 */
package de.schlund.pfixxml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import de.schlund.pfixxml.targets.SPCacheStatistic;
import de.schlund.pfixxml.testenv.XMLSerializeUtil;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *  
 */
public class CacheStatisticServlet extends HttpServlet {

    private static int OUTPUTXML=0;
    private static int OUTPUTTEXT=1;
    /**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int outmode = OUTPUTXML;
        
        String outputmode_param = req.getParameter("mode");
      
        if(outputmode_param != null) {
            if(outputmode_param.equals("text")) {
                outmode = OUTPUTTEXT;
            }
        }
        boolean reset = false;
        
        String reset_param = req.getParameter("doreset");
        if(reset_param != null) {
            if(reset_param.equals("true")) {
                reset = true;
            }
        }
        
      
        
        String output;
        
        if(outmode == OUTPUTXML) {
            Document doc;
            try {
                doc = SPCacheStatistic.getInstance().getCacheStatisticAsXML();
            } catch (ParserConfigurationException e) {
                throw new ServletException(e);
            }
            res.setContentType("text/xml");
            output = XMLSerializeUtil.getInstance().serializeToString(doc);
        } else if(outmode == OUTPUTTEXT) {
            output = SPCacheStatistic.getInstance().getCacheStatisticAsString(); 
            res.setContentType("text/plain");
        } else {
            throw new ServletException("No outputmode set.");
        }
        ServletOutputStream ostream = res.getOutputStream();
        ostream.println(output);
        
        if(reset) {
            SPCacheStatistic.getInstance().reset();
        }
    }

    /**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

}
