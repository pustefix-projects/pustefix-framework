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

import org.springframework.util.AntPathMatcher;
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
    
    private URL url;
    private String name;
    private Map<String,Set<String>> moduleToResourcePaths = new HashMap<String,Set<String>>();
    private Map<String,Set<String>> moduleToResourcePathPatterns = new HashMap<String,Set<String>>();
    
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    
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
    
    /**
     * Adds resource/module overridden by this module.
     * 
     * @param module - Module name
     * @param resourcePath - Resource path (either the full path or an ant-style pattern)
     */
    public void addOverridedResource(String module, String resourcePath) {
        if(resourcePath.contains("?") || resourcePath.contains("*")) {
            Set<String> resList = moduleToResourcePathPatterns.get(module);
            if(resList == null) {
                resList = new HashSet<String>();
                moduleToResourcePathPatterns.put(module, resList);
            }
            resList.add(resourcePath);
        } else {
            Set<String> resList = moduleToResourcePaths.get(module);
            if(resList == null) {
                resList = new HashSet<String>();
                moduleToResourcePaths.put(module, resList);
            }
            resList.add(resourcePath);
        }
    }
    
    public boolean overridesResource(String module, String path) {
        Set<String> overrides = moduleToResourcePaths.get(module);
        if(overrides != null) {
            if(overrides.contains(path)) return true;
        }
        overrides = moduleToResourcePathPatterns.get(module);
        if(overrides != null) {
            for(String pattern: overrides) {
                if(antPathMatcher.match(pattern, path)) return true;
            }
        }
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
        if(root.getLocalName().equals("module-descriptor")) {
            Element nameElem = getSingleChildElement(root, "module-name", true);
            String name = nameElem.getTextContent().trim();
            if(name.equals("")) throw new Exception("Text content of element 'module-name' must not be empty!");
            moduleInfo = new ModuleDescriptor(url, name);
            Element overElem = getSingleChildElement(root, "override-modules", false);
            if(overElem != null) {
                List<Element> modElems = getChildElements(overElem, "module");
                for(Element modElem:modElems) {
                    String modName = modElem.getAttribute("name").trim();
                    if(modName.equals("")) throw new Exception("Element 'module' requires 'name' attribute!");
                    List<Element> resElems = getChildElements(modElem, "resource");
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
    
    
    private static Element getSingleChildElement(Element parent, String localName, boolean mandatory) throws Exception {
        Element elem = null;
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                if(elem != null) throw new Exception("Multiple '" + localName + "' child elements aren't allowed."); 
                elem = (Element)node;
            }
        }
        if(mandatory && elem == null) throw new Exception("Missing '" + localName + "' child element.");
        return elem;
    }
    
    private static List<Element> getChildElements(Element parent, String localName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
}
