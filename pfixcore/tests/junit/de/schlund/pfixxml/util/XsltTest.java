/*
 * Created on May 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.util;

import java.io.File;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import net.sf.saxon.om.AbstractNode;
import net.sf.saxon.om.NodeInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import junit.framework.TestCase;

public class XsltTest extends TestCase {
    //-- make sure we have several bug fixes
    
    public void testBugfix962737_IncorrectNsPrefix() throws Exception {
        Document doc = transform("incorrectnsprefix");
        NodeList lst =  doc.getDocumentElement().getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "message");
        assertEquals(1, lst.getLength());
        assertEquals("alias:message", ((AbstractNode) lst.item(0)).getDisplayName());
    }

    //-- test extensions
    
    public void testExtension() throws Exception {
        Document doc = transform("extension");
        Element hello = (Element) doc.getDocumentElement().getElementsByTagName("hello").item(0);
        assertEquals("foo", hello.getAttribute("attr"));
    }

    //-- helper code
    
    private static Document transform(String name) throws Exception {
        return transform(name + ".xml", name + ".xsl");
    }
    private static Document transform(String xml, String xsl) throws Exception {
        final String PREFIX = "tests/junit/de/schlund/pfixxml/util/"; // TODO: windows
        Document doc;
        Templates trafo;
        DOMResult result;
        
        doc    = Xml.parse(new File(PREFIX + xml));
        trafo  = Xslt.loadTemplates(Path.create(new File(PREFIX + xsl)));
        result = new DOMResult();
        Xslt.transform(doc, trafo, null, result);
        return (Document) result.getNode();
    }
    
	public static NodeInfo toDocumentExtension(String str) throws TransformerException {
	    return (NodeInfo) Xml.parseString(str);
	}
}
