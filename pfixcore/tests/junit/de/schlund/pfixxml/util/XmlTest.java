package de.schlund.pfixxml.util;

import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import de.schlund.pfixxml.util.Xml;
import junit.framework.TestCase;

public class XmlTest extends TestCase {
    public void testCreateDocument() {
        assertNotNull(Xml.createDocument());
    }

    //-- parse tests 
    
    public void testXmlReaderConfig() throws Exception {
        XMLReader reader;
        
        reader = Xml.createXMLReader();
        assertFalse(reader.getFeature("http://xml.org/sax/features/validation"));
        assertTrue(reader.getFeature("http://xml.org/sax/features/namespaces"));
    }
    
    public void testDocumentBuilderConfig() {
        DocumentBuilder builder;
        
        builder = Xml.createDocumentBuilder();
        assertFalse(builder.isValidating());
        assertTrue(builder.isNamespaceAware());
    }

    public void testParse() throws Exception {
        parse("<ok/>");
        try {
            parse("<wrong>");
            fail();
        } catch (SAXException e) {
            // ok
        }
    }
    
    public void testComments() throws Exception {
        // make sure to get comments
    	Document doc = parse("<hello><!-- commend --></hello>");
    	Node ele = doc.getDocumentElement();
    	NodeList lst = ele.getChildNodes();
    	assertEquals(1, lst.getLength());
    	assertTrue(lst.item(0) instanceof Comment);
    }
    
    //-- serialize tests
    
    public void testSerializeSimple() throws Exception {
        assertEquals("<ok/>\n", serialize("<ok/>", false));
    }

    public void testSerializePreserve() throws Exception {
        final String STR = "<a><b/> \n<c/></a>\n";
        assertEquals(STR, serialize(STR, false));
    }
    public void testSerializePP() throws Exception {
        assertEquals("<a>\n  <b/>\n  <c/>\n</a>\n", serialize("<a><b/> \n <c/></a>", true));
    }

    public void testSerializeExplicitNamespace() throws Exception {
        Document doc = parse(serialize("<ns:ok xmlns:ns='foo'/>", false));
        assertEquals("foo", doc.getDocumentElement().getNamespaceURI());
    }
    public void testSerializeImplicitNamespace() throws Exception {
        Document doc = parse(serialize("<ok xmlns='bar'/>", false));
        assertEquals("bar", doc.getDocumentElement().getNamespaceURI());
    }
    public void testSerializeText() throws Exception {
        Document doc = parse("<a>foo</a>");
        assertEquals("foo\n", Xml.serialize(XPath.selectNode(doc, "/a/node()"), false, false));
    }

    //-- helper code
    
    private static String serialize(String doc, boolean pp) throws Exception {
        return Xml.serialize(parse(doc), pp, false);
    }
    
    private static Document parse(String str) throws Exception {
        return Xml.parseStringMutable(str);
    }
}
