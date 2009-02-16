package de.schlund.pfixcore.oxm.helper;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Helper class that provides static methods
 * that are used in the OXM unit tests.
 *  
 * @author  mleidig@schlund.de
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class OxmTestHelper {

    public static Document createResultDocument() {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("result");
            doc.appendChild(root);
            return doc;
        } catch (Exception x) {
            throw new RuntimeException("Can't create document", x);
        }
    }

    public static Document createDocument(String str) {
        try {
            StringReader reader = new StringReader(str);
            InputSource src = new InputSource();
            src.setCharacterStream(reader);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
            return doc;
        } catch (Exception x) {
            throw new RuntimeException("Can't create document", x);
        }
    }

    public static Document createDocument(InputStream in) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            return doc;
        } catch (Exception x) {
            throw new RuntimeException("Can't create document", x);
        }
    }

    public static void printDocument(Document doc) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(System.out));
        } catch (Exception x) {
            throw new RuntimeException("Can't print document", x);
        }
    }   
}