package de.schlund.pfixcore.example;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.beans.InsertStatus;

/**
 * ContextResource getting XML data from file.
 * Can be used for mocking during development.
 */
public class XMLFileContextResource {
    
    private Resource xmlFile;
    
    public void setXmlFile(Resource xmlFile) {
        this.xmlFile = xmlFile;
    }
    
    @InsertStatus
    public void toXML(Element root) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream in = xmlFile.getInputStream();
            Document doc = db.parse(in);
            Element docRoot = doc.getDocumentElement();
            
            
            NamedNodeMap attrs = docRoot.getAttributes();
            for(int i=0; i<attrs.getLength(); i++) {
               Node node = attrs.item(i);
               root.setAttribute(node.getNodeName(), node.getNodeValue());
            }
            NodeList nodes = docRoot.getChildNodes();
            for(int i=0; i<nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Node imported = root.getOwnerDocument().importNode(node, true);
                root.appendChild(imported);
            }
        
        } catch(Exception x) {
            throw new RuntimeException("Error while trying to add XML data from file", x);
        }
    }

}
