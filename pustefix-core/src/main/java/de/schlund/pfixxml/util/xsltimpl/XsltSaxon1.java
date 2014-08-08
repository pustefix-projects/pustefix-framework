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

package de.schlund.pfixxml.util.xsltimpl;

import java.io.StringReader;
import java.io.Writer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.icl.saxon.Controller;
import com.icl.saxon.FeatureKeys;
import com.icl.saxon.PreparedStyleSheet;
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.output.Emitter;
import com.icl.saxon.tree.DocumentImpl;

import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltMessageWriter;
import de.schlund.pfixxml.util.XsltSupport;

/**
 * @author mleidig@schlund.de
 */
public class XsltSaxon1 implements XsltSupport {

    private TransformerFactory ifactory = new TransformerFactoryImpl();
       
    // pretty-print script by M. Kay, see 
    // http://www.cafeconleche.org/books/xmljava/chapters/ch17s02.html#d0e32721
    private static final String ID = "<?xml version='1.0'?>"
        + "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.1'>"
        + "  <xsl:output method='xml' indent='yes'/>"
        + "  <xsl:strip-space elements='*'/>"
        + "  <xsl:template match='/'>" + "    <xsl:copy-of select='.'/>"
        + "  </xsl:template>" + "</xsl:stylesheet>";
    
    private static Templates PP_XSLT;
    
    static {
        Source src = new SAXSource(Xml.createXMLReader(), new InputSource(new StringReader(ID)));
        TransformerFactoryImpl factory = new TransformerFactoryImpl();
       
        try {
            factory.setAttribute(FeatureKeys.LINE_NUMBERING, true);
            PP_XSLT = factory.newTemplates(src);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public TransformerFactory getSharedTransformerFactory() {
        return ifactory;
    }
    
    public TransformerFactory getThreadTransformerFactory() {
        return new TransformerFactoryImpl();
    }
    
    public Templates getPrettyPrinterTemplates() {
        return PP_XSLT;
    }
    
    public boolean isInternalTemplate(Templates templates) {
        return templates instanceof PreparedStyleSheet;
    }
    
    public void doTracing(Transformer transformer, Writer traceWriter) {
        Saxon1TraceListener tl=new Saxon1TraceListener(Saxon1TraceListener.Format.VERBOSE, traceWriter);
        Controller c=(Controller)transformer;
        c.setTraceListener(tl);
        c.setLineNumbering(true);
    }
    
    public String getSystemId(Templates templates) {
    	if(templates instanceof PreparedStyleSheet) {
    		DocumentImpl doc = ((PreparedStyleSheet)templates).getStyleSheetDocument();
    		if(doc != null) {
    			return doc.getSystemId();
    		}
    	}
    	return null;
    }
    
    @Override
    public XsltMessageWriter recordMessages(Transformer transformer) {
        Controller c = (Controller)transformer;
        XsltMessageWriter w = new XsltMessageWriter();
        Emitter emitter = c.getMessageEmitter();
        if(emitter == null) {
            try {
                emitter = c.makeMessageEmitter();
            } catch (TransformerException e) {
                throw new RuntimeException("Error creating XSLT message emitter", e);
            }
        }
        emitter.setWriter(w);
        return w;
    }
    
    @Override
    public void doErrorListening(Transformer transformer, boolean traceLocation) {
        if(traceLocation) {
            Saxon1LocationTraceListener tl = new Saxon1LocationTraceListener();
            ErrorListener el = new Saxon1ErrorListener(tl);
            transformer.setErrorListener(el);
            ((Controller)transformer).addTraceListener(tl);
        } else {
            transformer.setErrorListener(new ErrorListenerBase());
        }
    }

}
