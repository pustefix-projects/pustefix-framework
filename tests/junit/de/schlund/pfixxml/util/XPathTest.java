/*
 * Created on May 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.util;

import java.util.List;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import junit.framework.TestCase;

public class XPathTest extends TestCase {
    private List lst;
    private Node node;
    
    public void testElements() throws Exception {
        lst = select("<x><a/><a/></x>", "/x/a");
        assertEquals(2, lst.size());
        assertEquals("a", ((Node) lst.get(0)).getLocalName());
        assertEquals("a", ((Node) lst.get(1)).getLocalName());
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
        assertSame(node, lst.get(0));
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

    private static List select(String doc, String xpath) throws Exception {
        return XPath.select(parse(doc), xpath);
    }

    private static Document parse(String doc) {
        try {
            return Xml.parseString(doc);
        } catch (SAXException e) {
            fail("wrong document: " + doc + ":" + e.getMessage());
            return null; // dummy
        }
    }
}
