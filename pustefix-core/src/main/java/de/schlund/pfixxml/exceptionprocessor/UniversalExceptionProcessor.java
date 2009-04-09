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

package de.schlund.pfixxml.exceptionprocessor;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.exceptionprocessor.util.ExceptionDataValue;
import de.schlund.pfixxml.exceptionprocessor.util.ExceptionDataValueHelper;
import de.schlund.pfixxml.exceptionprocessor.util.TextCreatorVisitor;
import de.schlund.pfixxml.exceptionprocessor.util.XMLCreatorVisitor;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * @author jh
 *
 * Get a DOM tree from the passed exception, transform it with a
 * given stylesheet and write the result to the passed outputstream.
 */
public class UniversalExceptionProcessor implements ExceptionProcessor {

    private static final String ERROR_STYLESHEET = "core/xsl/errorrepresentation.xsl";
    private static final Logger LOG = Logger.getLogger(UniversalExceptionProcessor.class);
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor#processException(java.lang.Throwable, de.schlund.pfixxml.exceptionprocessor.ExceptionConfig, de.schlund.pfixxml.PfixServletRequest, javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public void processException(Throwable exception, ExceptionConfig exConfig,
                                 PfixServletRequest pfixReq, ServletContext servletContext,
                                 HttpServletRequest req, HttpServletResponse res, Properties props)
        throws IOException, ServletException {
        Document doc = null;
        String text = null;
        
        LOG.error("Processing throwable: ", exception);
        
        ExceptionDataValue data = ExceptionDataValueHelper.createExceptionDataValue(exception, pfixReq);
        XMLCreatorVisitor xv = new XMLCreatorVisitor();
        data.accept(xv);
        TextCreatorVisitor tv = new TextCreatorVisitor();
        data.accept(tv);
        doc = data.getXMLPresentation();
        text = data.getTextBody();
        
        LOG.error(text);
	
        if(LOG.isDebugEnabled()) {
            LOG.debug("Got following DOM for error-representation: " + Xml.serialize(doc, true, false));
        }
        
        doc = Xml.parse(XsltVersion.XSLT1, doc);
        
        Templates stvalue;
        
        try {
            stvalue = Xslt.loadTemplates(XsltVersion.XSLT1, ResourceUtil.getFileResourceFromDocroot(ERROR_STYLESHEET));
        } catch (TransformerConfigurationException e) {
            throw new ServletException(e);
        }
        
        res.setContentType("text/html");
        
        try {
            Xslt.transform(doc, stvalue, null, new StreamResult(res.getOutputStream()));
        } catch (TransformerException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }
    
}
