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

    /**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Document doc;
        try {
            doc = SPCacheStatistic.getInstance().getXMLCacheStatistic();
        } catch (ParserConfigurationException e) {
            throw new ServletException(e);
        }

        String str = XMLSerializeUtil.getInstance().serializeToString(doc);

        res.setContentType("text/xml");
        ServletOutputStream ostream = res.getOutputStream();
        ostream.println(str);
    }

    /**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

}
