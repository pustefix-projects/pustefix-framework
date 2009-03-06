/*
 * Created on May 24, 2004
 *
 */
package de.schlund.pfixxml.util;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.icl.saxon.om.AbstractNode;
import com.icl.saxon.om.NodeInfo;

import de.schlund.pfixxml.resources.ResourceUtil;

public class XsltTest extends TestCase {

    protected XsltVersion getXsltVersion() {
        return XsltVersion.XSLT1;
    }

    // -- make sure we have several bug fixes

    public void testBugfix962737_IncorrectNsPrefix() throws Exception {
        Document doc = transform("incorrectnsprefix");
        NodeList lst = doc.getDocumentElement().getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "message");
        assertEquals(1, lst.getLength());
        assertEquals("alias:message", ((AbstractNode) lst.item(0)).getDisplayName());
    }

    // -- test extensions

    public void testExtension() throws Exception {
        Document doc = transform("extension");
        Element hello = (Element) doc.getDocumentElement().getElementsByTagName("hello").item(0);
        assertEquals("foo", hello.getAttribute("attr"));
    }

    public void testUriEncodingWithHtmlOutput() throws Exception {
        StreamResult result;
        StringWriter writer;
        Document doc;

        // Xml.serialize does *not* encode urls, thus, I have to use Saxon's
        // stream serialization
        writer = new StringWriter();
        result = new StreamResult(writer);
        transform("html", result);
        doc = Xml.parseString(getXsltVersion(), writer.getBuffer().toString());
        assertEquals("m%C3%BCller", ((Attr) XPath.selectNode(doc, "/html/a/@href")).getValue());
    }

    // -- helper code

    private Document transform(String name) throws Exception {
        DOMResult result;

        result = new DOMResult();
        transform(name, result);
        return (Document) result.getNode();
    }

    private void transform(String name, Result result) throws Exception {
        final String PREFIX = "src/test/java/de/schlund/pfixxml/util/"; // TODO: windows
        final String xml = name + ".xml";
        final String xsl = name + ".xsl";
        Document doc;
        Templates trafo;

        doc = Xml.parse(getXsltVersion(), new File(PREFIX + xml));
        trafo = Xslt.loadTemplates(getXsltVersion(), ResourceUtil.getFileResource("file://" + (new File(PREFIX + xsl)).getAbsolutePath()));
        Xslt.transform(doc, trafo, null, result);
    }

    public static NodeInfo toDocumentExtension(String str) throws TransformerException {
        return (NodeInfo) Xml.parseString(XsltVersion.XSLT1, str);
    }
}
