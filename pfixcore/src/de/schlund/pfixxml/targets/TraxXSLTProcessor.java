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
*
*/
package de.schlund.pfixxml.targets;

import com.icl.saxon.Controller;
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.tinytree.TinyDocumentImpl;

import java.io.File;
import java.io.OutputStream;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Category;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


public class TraxXSLTProcessor implements PustefixXSLTProcessor {
    private static Category               CAT             = 
            Category.getInstance(TraxXSLTProcessor.class.getName());
    private static Object                 FUCKUP_LOCK     = new Object();
    private static DocumentBuilderFactory dbfac           = DocumentBuilderFactory.newInstance();
    private static TraxXSLTProcessor      instance        = new TraxXSLTProcessor();
    private static String                 TRANS_FAC_XALAN = 
            "org.apache.xalan.processor.TransformerFactoryImpl";
    private static String                 TRANS_FAC_SAXON = "com.icl.saxon.TransformerFactoryImpl";
    private static String                 DOCB_FAC_XERCES = 
            "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
    private static String                 SAXP_FAC_XERCES = 
            "org.apache.xerces.jaxp.SAXParserFactoryImpl";
    private static String                 DOCB_FAC_SAXON  = 
            "com.icl.saxon.om.DocumentBuilderFactoryImpl";
    private static String                 SAXP_FAC_SAXON  = 
            "com.icl.saxon.aelfred.SAXParserFactoryImpl";
    public static String                  TRANS_FAC_KEY   = 
            "javax.xml.transform.TransformerFactory";
    public static String                  DOCB_FAC_KEY    = 
            "javax.xml.parsers.DocumentBuilderFactory";
    public static String                  SAXP_FAC_KEY    = "javax.xml.parsers.SAXParserFactory";
    public static String                  TRANS_FAC_VALUE = TRANS_FAC_SAXON;
    public static String                  DOCB_FAC_VALUE  = DOCB_FAC_XERCES;
    public static String                  SAXP_FAC_VALUE  = SAXP_FAC_XERCES;
    
   

    static {
        if (CAT.isInfoEnabled()) {
            StringBuffer b = new StringBuffer(100);
            b.append("\nSetting ").append(TRANS_FAC_KEY).append(" to ").append(TRANS_FAC_VALUE).append(
                    "\n").append("Setting ").append(DOCB_FAC_KEY).append(" to ").append(
                    DOCB_FAC_VALUE).append("\n").append("Setting ").append(SAXP_FAC_KEY).append(
                    " to ").append(SAXP_FAC_VALUE);
            CAT.info(b.toString());
        }
        System.getProperties().put(TRANS_FAC_KEY, TRANS_FAC_VALUE);
        System.getProperties().put(DOCB_FAC_KEY, DOCB_FAC_VALUE);
        System.getProperties().put(SAXP_FAC_KEY, SAXP_FAC_VALUE);
        dbfac.setNamespaceAware(true);
        dbfac.setValidating(false);
    }

    private TraxXSLTProcessor() {
        //
    }

    public static TraxXSLTProcessor getInstance() {
        return instance;
    }

    public void applyTrafoForOutput(Object xmlobj, Object xslobj, Map params, OutputStream out)
                             throws Exception {
        Document    doc   = (Document) xmlobj;
        Templates   xsl   = (Templates) xslobj;
        Transformer trafo = xsl.newTransformer();
        long        start;
        if (params != null) {
            for (Iterator e = params.keySet().iterator(); e.hasNext();) {
                String name  = (String) e.next();
                String value = (String) params.get(name);
                if (name != null && value != null) {

                    //if(CAT.isDebugEnabled())
                    //  CAT.debug("*** Setting param " + name + " to value " + value);
                    trafo.setParameter(name, value);
                }
            }
        }
        start = new Date().getTime();
        if (TRANS_FAC_VALUE.equals(TRANS_FAC_SAXON)) {
            trafo.transform( (TinyDocumentImpl) xmlobj, new StreamResult(out));
        } else {
            synchronized (FUCKUP_LOCK) {
                // is this really fast ??
                trafo.transform(new DOMSource(doc), new StreamResult(out));
            }
        }
        if (CAT.isDebugEnabled())
            CAT.debug("      ===========> Transforming and serializing took "
                      + (new Date().getTime() - start) + " ms.");
    }

    // FIXME: We need to change the processing in AbstractXMLServer to not simply use spdoc.getDocument(), but instead
    //        route the Document through this method first. But we also need to make sure that this happens only once,
    //        and the resulting Object is stored for reuse (this applies to frame handling).
    //
    public Document xmlObjectFromDocument(Document doc) throws Exception {
       if (TRANS_FAC_VALUE.equals(TRANS_FAC_SAXON)) {
            Document o = tinyTreeFromDocument(doc);
           /* if (CAT.isDebugEnabled())
                CAT.debug("SAXON detected. Returning " + o == null ? "null" : o.getClass().getName());*/
            return o;
        } else if (TRANS_FAC_VALUE.equals(TRANS_FAC_XALAN)) {
           /* if (CAT.isDebugEnabled())
                CAT.debug("XALAN detected. Returning " + doc == null ? "null" : doc.getClass().getName());*/
            return doc;
        }
        CAT.warn("Could not detect current transfomer!");
        return null;
    }

    public Object xslObjectFromDisc(String path) throws Exception {
        TransformerFactory transFac = TransformerFactory.newInstance();
       /* if (CAT.isDebugEnabled())
            CAT.debug("TransformerFactory is: " + transFac.getClass().getName());
            */
        Object val = transFac.newTemplates(new StreamSource("file://" + path));
        return val;
    }

    public Document xmlObjectFromDisc(String path) throws Exception {
       /* if (CAT.isDebugEnabled())
            CAT.debug("DocumentBuilderFactory is: " + dbfac.getClass().getName());
        Document doc = dbfac.newDocumentBuilder().parse(path);
        
        return tinyTreeFromDocument(doc);*/
        SAXSource saxS = new SAXSource();
        saxS.setInputSource(new InputSource("file://"+path));
        Controller saxonController = new Controller();
        Builder builder = saxonController.makeBuilder();
        DocumentInfo dInfo = builder.build(saxS);
        return (Document) dInfo;
    }

    private TinyDocumentImpl tinyTreeFromDocument(Document doc)
                                          throws Exception {
        if(doc == null) {
            // thats a request to an unkown page! 
            // return null, cause we  want a 404 and no NPExpection
            if(CAT.isDebugEnabled()) {
                CAT.debug("Having a null-document as parameter. Unkown page? Returning null...");
            }
            return null;
        }
        long start = 0;
        long stop = 0;
        if (CAT.isInfoEnabled())
            start = System.currentTimeMillis();
        DOMSource domSource = new DOMSource(doc);
        Controller saxonController = new Controller();
        saxonController.setTreeModel(Builder.TINY_TREE);
        TransformerFactoryImpl tfFImpl      = new TransformerFactoryImpl();
        SAXSource        saxSource = tfFImpl.getSAXSource(domSource, false);
        Builder bi = saxonController.makeBuilder();
        
        /*if (CAT.isInfoEnabled()) {
            stop = System.currentTimeMillis();
            CAT.info("Initialisation :" + (stop - start));
        }
        if (CAT.isDebugEnabled())
            CAT.debug("TransformerFactoryImpl is: " + tfFImpl.getClass().getName());
        */
        
        DocumentInfo     dInfo   = bi.build(saxSource);
        TinyDocumentImpl tinyDoc = (TinyDocumentImpl) dInfo;
        if (CAT.isInfoEnabled()) {
            stop = System.currentTimeMillis();
            StringBuffer b = new StringBuffer(100);
            b.append("Conversion from ").append(doc.getClass().getName()).append(" to ").append(tinyDoc.getClass().getName())
             .append(" took ").append(stop - start).append("ms");
            CAT.info(b.toString());
        }
        return tinyDoc;
    }
    
}
