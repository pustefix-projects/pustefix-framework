package org.pustefixframework.util.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.pustefixframework.test.XmlAssert;
import org.w3c.dom.Document;

public class DOMUtilsTest extends TestCase {

    public void testMerge() throws Exception {
        
        InputStream in = getClass().getResourceAsStream("merge-before.xml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        DOMUtils.mergeChildElements(doc.getDocumentElement(), "child", "id");
        DOMUtils.removeWhitespace(doc.getDocumentElement());

        in = getClass().getResourceAsStream("merge-after.xml");
        Document refDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        DOMUtils.removeWhitespace(refDoc);
        
        XmlAssert.assertEquals(refDoc, doc);
    }
    
}
