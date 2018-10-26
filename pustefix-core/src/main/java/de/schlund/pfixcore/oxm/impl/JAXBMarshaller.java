/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.oxm.impl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.pustefixframework.util.BytecodeAPIUtils;
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
            if(BytecodeAPIUtils.isProxy(objectClass)) {
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

    @Override
    public boolean isSupported(Class<?> objectClass) {
        return objectClass.getAnnotation(XmlRootElement.class) != null;
    }

}
