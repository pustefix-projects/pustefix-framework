package de.schlund.pfixcore.oxm.impl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import net.sf.cglib.proxy.Enhancer;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.oxm.Marshaller;
import de.schlund.pfixcore.oxm.MarshallingException;

public class JAXBMarshaller implements Marshaller {

    public void marshal(Object obj, Result result) throws MarshallingException {

        if (!(result instanceof DOMResult)) throw new IllegalArgumentException("Result must be of type: " + DOMResult.class.getName());
        DOMResult domResult = (DOMResult) result;

        try {

            Class<?> objectClass = obj.getClass();
            if(Enhancer.isEnhanced(objectClass)) {
                objectClass = objectClass.getSuperclass();
            }
            JAXBContext jc = JAXBContext.newInstance(objectClass);
            javax.xml.bind.Marshaller m = jc.createMarshaller();
            m.marshal(obj, domResult);

            //if the passed in root element is empty and has the same name as the one newly created by JAXB,
            //we try to swallow the element by moving the child nodes to the root element. Thus the behaviour
            //is similar to the OXM default implementation, which reuses the passed in root node too.
            swallowStartElement((Element)domResult.getNode());

        } catch(JAXBException x) {
            throw new MarshallingException("Error while marshalling object.", x);
        }

    }

    private static void swallowStartElement(Element root) {
        NamedNodeMap attrMap = root.getAttributes();
        NodeList childNodes = root.getChildNodes();
        if(attrMap.getLength() == 0 && childNodes.getLength() == 1 && childNodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
            Element child = (Element)childNodes.item(0);
            if(child.getNodeName().equals(root.getNodeName())) {
                NamedNodeMap attrs = child.getAttributes();
                for(int i=0; i<attrs.getLength(); i++) {
                    Attr attrNode = (Attr)attrs.item(i);
                    root.setAttributeNode(((Attr)attrNode.cloneNode(true)));
                }
                while(child.hasChildNodes()) {
                    root.appendChild(child.removeChild(child.getFirstChild()));
                }
                root.removeChild(child);
            }
        }
    }

}
