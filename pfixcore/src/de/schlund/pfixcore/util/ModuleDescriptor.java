package de.schlund.pfixcore.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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
    
    private static String NAMESPACE_MODULE_DESCRIPTOR = "http://pustefix.sourceforge.net/moduledescriptor200702";
    
    private URL url;
    private String name;
    private Map<String,Set<String>> moduleToResourcePaths = new HashMap<String,Set<String>>();
    private Map<String,Set<String>> moduleToResourcePathPatterns = new HashMap<String,Set<String>>();
    private Dictionary<String,String> filterAttributes = new Hashtable<String,String>();
    
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
                List<Element> filterAttrElems = getChildElements(overElem, NAMESPACE_MODULE_DESCRIPTOR, "filter-attribute");
                for(Element filterAttrElem: filterAttrElems) {
                    String filterAttrName = filterAttrElem.getAttribute("name");
                    if(filterAttrName.equals("")) throw new Exception("Element 'filter-attribute' requires 'name' attribute!");
                    String filterAttrValue = filterAttrElem.getAttribute("value");
                    if(filterAttrValue.equals("")) throw new Exception("Element 'filter-attribute' requires 'value' attribute!");
                    moduleInfo.addFilterAttribute(filterAttrName, filterAttrValue);
                }
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
