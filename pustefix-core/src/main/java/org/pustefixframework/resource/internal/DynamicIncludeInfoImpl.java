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
package org.pustefixframework.resource.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pustefixframework.config.Constants;
import org.springframework.util.AntPathMatcher;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DynamicIncludeInfoImpl implements DynamicIncludeInfo {
    
    private String name;
    private int dynamicSearchLevel;

    private Map<String,Set<String>> moduleToResourcePaths = new HashMap<String,Set<String>>();
    private Map<String,Set<String>> moduleToResourcePathPatterns = new HashMap<String,Set<String>>();
    
    private Dictionary<String,String> filterAttributes = new Hashtable<String,String>();
    
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    
    public DynamicIncludeInfoImpl(String name, int dynamicSearchLevel) {
        this.name = name;
        this.dynamicSearchLevel = dynamicSearchLevel;
    }
    
    public String getModuleName() {
        return name;
    }
    
    public int getDynamicSearchLevel() {
        return dynamicSearchLevel;
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
    
    
    public void addFilterAttribute(String name, String value) {
        filterAttributes.put(name, value);
    }
         
    public Dictionary<String,String> getFilterAttributes() {
        return filterAttributes;
    }
    
    @Override
    public String toString() {
        return "MODULE " + name;
    }
        
    public static DynamicIncludeInfoImpl create(String moduleName, Element element) {
        DynamicIncludeInfoImpl dynInfo;
        if(element.getNamespaceURI().equals(Constants.NS_MODULE) && element.getLocalName().equals("dynamic-includes")) {
            int level = -1;
            Element searchElem = getSingleChildElement(element, Constants.NS_MODULE, "auto-search", false);
            if(searchElem != null) {
                String levelStr = searchElem.getAttribute("level").trim();
                if(levelStr.length() > 0) level = Integer.parseInt(levelStr);
            }
            dynInfo = new DynamicIncludeInfoImpl(moduleName, level);
            Element overrideElem = getSingleChildElement(element, Constants.NS_MODULE, "override-modules", false);
            if(overrideElem != null) {
                List<Element> filterAttrElems = getChildElements(overrideElem, Constants.NS_MODULE, "filter-attribute");
                for(Element filterAttrElem: filterAttrElems) {
                    String filterAttrName = filterAttrElem.getAttribute("name");
                    if(filterAttrName.equals("")) throw new IllegalArgumentException("Element 'filter-attribute' requires 'name' attribute!");
                    String filterAttrValue = filterAttrElem.getAttribute("value");
                    if(filterAttrValue.equals("")) throw new IllegalArgumentException("Element 'filter-attribute' requires 'value' attribute!");
                    dynInfo.addFilterAttribute(filterAttrName, filterAttrValue);
                }
                List<Element> modElems = getChildElements(overrideElem, Constants.NS_MODULE, "module");
                for(Element modElem:modElems) {
                    String modName = modElem.getAttribute("name").trim();
                    if(modName.equals("")) throw new IllegalArgumentException("Element 'module' requires 'name' attribute!");
                    List<Element> resElems = getChildElements(modElem, Constants.NS_MODULE, "resource");
                    for(Element resElem:resElems) {
                        String resPath = resElem.getAttribute("path").trim();
                        if(resPath.equals("")) throw new IllegalArgumentException("Element 'resource' requires 'path' attribute!");
                        if(!resPath.startsWith("/")) resPath = "/" + resPath;
                        if(!resPath.startsWith("/PUSTEFIX-INF")) resPath = "/PUSTEFIX-INF" + resPath;
                        dynInfo.addOverridedResource(modName, resPath);
                    }
                }
            }
        } else throw new IllegalArgumentException("Unexpected module descriptor element: " + element.getNodeName());
        return dynInfo;
    }
    
    private static Element getSingleChildElement(Element parent, String nsuri, String localName, boolean mandatory) {
        Element elem = null;
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI().equals(nsuri) 
                    && node.getLocalName().equals(localName)) {
                if(elem != null) throw new IllegalArgumentException("Multiple '" + localName + "' child elements aren't allowed."); 
                elem = (Element)node;
            }
        }
        if(mandatory && elem == null) throw new IllegalArgumentException("Missing '" + localName + "' child element.");
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
