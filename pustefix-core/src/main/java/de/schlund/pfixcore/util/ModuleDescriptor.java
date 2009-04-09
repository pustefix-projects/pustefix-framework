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
package de.schlund.pfixcore.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ModuleDescriptor {
    
    private static String NAMESPACE_MODULE_DESCRIPTOR = "http://pustefix.sourceforge.net/moduledescriptor200702";
    
    private URL url;
    private String name;
    private Map<String,Set<String>> overrideMap = new HashMap<String,Set<String>>();
    
    public ModuleDescriptor(URL url, String name) {
        this.url = url;
        this.name = name;
    }
    
    public URL getURL() {
        return url;
    }
    
    public String getName() {
        return name;
    }
    
    public void addOverridedResource(String module, String resourcePath) {
        Set<String> resList = overrideMap.get(module);
        if(resList == null) {
            resList = new HashSet<String>();
            overrideMap.put(module,resList);
        }
        resList.add(resourcePath);
    }
    
    public Set<String> getOverridedResources(String module) {
        return overrideMap.get(module);
    }
    
    public boolean overridesResource(String module, String path) {
        Set<String> overrides = overrideMap.get(module);
        if(overrides != null) return overrides.contains(path);
        return false;
    }
    
    @Override
    public String toString() {
        return "MODULE " + name;
    }
    
        
    public static ModuleDescriptor read(URL url) throws Exception {
        ModuleDescriptor moduleInfo;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(url.openStream());
        Element root = document.getDocumentElement();
        if(root.getNamespaceURI().equals(NAMESPACE_MODULE_DESCRIPTOR) && root.getLocalName().equals("module-descriptor")) {
            Element nameElem = getSingleChildElement(root, NAMESPACE_MODULE_DESCRIPTOR, "module-name", true);
            String name = nameElem.getTextContent().trim();
            if(name.equals("")) throw new Exception("Text content of element 'module-name' must not be empty!");
            moduleInfo = new ModuleDescriptor(url, name);
            Element overElem = getSingleChildElement(root, NAMESPACE_MODULE_DESCRIPTOR, "override-modules", false);
            if(overElem != null) {
                List<Element> modElems = getChildElements(overElem, NAMESPACE_MODULE_DESCRIPTOR, "module");
                for(Element modElem:modElems) {
                    String modName = modElem.getAttribute("name").trim();
                    if(modName.equals("")) throw new Exception("Element 'module' requires 'name' attribute!");
                    List<Element> resElems = getChildElements(modElem, NAMESPACE_MODULE_DESCRIPTOR, "resource");
                    for(Element resElem:resElems) {
                        String resPath = resElem.getAttribute("path").trim();
                        if(resPath.equals("")) throw new Exception("Element 'resource' requires 'path' attribute!");
                        moduleInfo.addOverridedResource(modName, resPath);
                    }
                }
            }
        } else throw new Exception("Illegal module descriptor");
        return moduleInfo;
    }
    
    
    private static Element getSingleChildElement(Element parent, String nsuri, String localName, boolean mandatory) throws Exception {
        Element elem = null;
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI().equals(nsuri) 
                    && node.getLocalName().equals(localName)) {
                if(elem != null) throw new Exception("Multiple '" + localName + "' child elements aren't allowed."); 
                elem = (Element)node;
            }
        }
        if(mandatory && elem == null) throw new Exception("Missing '" + localName + "' child element.");
        return elem;
    }
    
    private static List<Element> getChildElements(Element parent, String nsuri, String localName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI().equals(nsuri) 
                    && node.getLocalName().equals(localName)) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
}
