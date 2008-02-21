/*
 * Created on May 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.util;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathTest extends TestCase {
    
    private List<Node> lst;
    private Node node;
    
    public void testElements() throws Exception {
        lst = select("<x><a/><a/></x>", "/x/a");
        assertEquals(2, lst.size());
        assertEquals("a", ((Node) lst.get(0)).getLocalName());
        assertEquals("a", ((Node) lst.get(1)).getLocalName());
        node = selectNode("<x><a/></x>","/x/b");
        assertNull(node);
        node = selectNode("<x><a/></x>","/x/a");
        assertEquals("a",node.getLocalName());
        node = selectNode("<x><a id=\"1\"/><a id=\"2\"/></x>","/x/a");
        assertEquals("1",((Element)node).getAttribute("id"));
    }

    public void testAttributes() throws Exception {
        lst = select("<x><a attr='foo'/><b attr='bar'/></x>", "//@attr");
        assertEquals(2, lst.size());
        assertEquals("foo", ((Attr) lst.get(0)).getValue());
        assertEquals("bar", ((Attr) lst.get(1)).getValue());
    }

    public void testContext() throws Exception {
        lst = select("<x><a/><a/></x>", "/x/a");
        node = (Node) lst.get(0);
        lst = XPath.select(node, ".");
        assertEquals(1, lst.size());
        checkNodeEquality(node, (Node)lst.get(0));
    }
    
    protected void checkNodeEquality(Node node1,Node node2) {
        assertSame(node1,node2);
    }
    
    public void testBoolean() throws Exception {
        Document doc = parse("<x/>");

        assertEquals(false, XPath.test(doc, "false"));
        assertEquals(false, XPath.test(doc, "/y"));
        assertEquals(false, XPath.test(doc, "0"));
        assertEquals(false, XPath.test(doc, "''"));

        assertEquals(true, XPath.test(doc, "' '"));
        assertEquals(true, XPath.test(doc, "7"));
        assertEquals(true, XPath.test(doc, "/x"));
    }

    public void testVersion2() throws Exception {
        // things that xpath 2.0 reports as an error (xpath 1.0 didn't object ...)
        try {
            select("<x/>", "0='0'");
            fail();
        } catch (TransformerException e) {
            // ok
        }
    }

    private List<Node> select(String doc, String xpath) throws Exception {
        return XPath.select(parse(doc), xpath);
    }

    private Node selectNode(String doc, String xpath) throws Exception {
        return XPath.selectNode(parse(doc), xpath);
    }
    
    private Document parse(String doc) throws Exception {
        //return createDOM(doc);
        try {
            return Xml.parseStringMutable(doc);
        } catch (SAXException e) {
            fail("wrong document: " + doc + ":" + e.getMessage());
            return null; // dummy
        }
    }
    
    protected Document createDOM(String xml) throws Exception {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        return createDOM(dbf,xml);
    }
    
    protected Document createDOM(DocumentBuilderFactory dbf,String xml) throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        DocumentBuilder db=dbf.newDocumentBuilder();
        InputSource src=new InputSource(new StringReader(xml));
        Document doc=db.parse(src);
        return doc;
    }
    
}
