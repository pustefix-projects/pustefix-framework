package org.pustefixframework.utils.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DOMUtilsTest extends TestCase {

	public void testFormatting() throws Exception {
	
		String expected = getFormatted("formatted.xml");
		
		for(int i=1; i<5; i++) {
			assertEquals(expected, getUnformatted("unformatted"+i+".xml"));
		}
		for(int i=1; i<3; i++) {
			assertEquals(expected, getPreformatted("preformatted"+i+".xml", "baz"));
		}
		
	}
	
	private String getUnformatted(String name) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getInputStream(name));
		DOMUtils.format(doc.getDocumentElement(), 0, 2);
		Transformer trf = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		trf.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}
	
	private String getPreformatted(String name, String elementName) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getInputStream(name));
		Element element = (Element)doc.getElementsByTagName(elementName).item(0);
		int startIndent = 0;
		Node parent = element.getParentNode();
		while((parent = parent.getParentNode()) != null) {
			startIndent += 2;
		}
		DOMUtils.format(element, startIndent, 2);
		Transformer trf = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		trf.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}
	
	private String getFormatted(String name) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getInputStream(name));
		Transformer trf = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		trf.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}
	
    private InputStream getInputStream(String fileName) {
        InputStream in=getClass().getClassLoader().getResourceAsStream("org/pustefixframework/utils/xml/"+fileName);
        if(in==null) {
            try {
                in=new FileInputStream("src/test/java/org/pustefixframework/utils/xml/"+fileName);
            } catch(FileNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return in;
    }
	
}
