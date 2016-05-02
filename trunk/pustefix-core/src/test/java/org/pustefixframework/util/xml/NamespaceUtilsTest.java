package org.pustefixframework.util.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.pustefixframework.test.XmlAssert;
import org.w3c.dom.Document;

public class NamespaceUtilsTest extends TestCase {

    public void testNamespaceChange() throws Exception {
        
        for(int i=1; i<4; i++) {   
            Document oldDoc = getTestDocument("namespace" + i + "-old.xml");
            Document refDoc = getTestDocument("namespace" + i + "-new.xml");
            Document newDoc = NamespaceUtils.setNamespace(oldDoc, "http://www.pustefix-framework.org/bar", "http://www.pustefix-framework.org/bar.xsd");
            XmlAssert.assertEquals(refDoc, newDoc); 
        }
    }
    
    private Document getTestDocument(String name) throws Exception {
        InputStream in = getClass().getResourceAsStream(name);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        return doc;
    }
    
}
