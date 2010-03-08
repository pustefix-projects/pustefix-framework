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

package de.schlund.pfixxml.config.includes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.util.DOMBasedNamespaceContext;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Generics;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

public class IncludesResolver {
    private List<FileIncludeEventListener> listeners = new ArrayList<FileIncludeEventListener>();

    private String namespace;
    
    private String includeTag;
    
    private final static String TAGNAME = "config-include";
    
    private final static String CONFIG_FRAGMENTS_NS = "http://www.pustefix-framework.org/2008/namespace/config-fragments";
    
    private final static String CONFIG_FRAGMENTS_ROOT_TAG = "config-fragments";
    
    private ThreadLocal<Set<Tupel<String, String>>> includesList = new ThreadLocal<Set<Tupel<String, String>>>();

    public IncludesResolver(String namespace) {
        this(namespace, TAGNAME);
    }
    
    public IncludesResolver(String namespace, String includeTag) {
        this.namespace = namespace;
        this.includeTag = includeTag;
    }

    public void registerListener(FileIncludeEventListener listener) {
        listeners.add(listener);
    }

    public void resolveIncludes(Document doc) throws SAXException {
        List<Element> nodes;
        try {
            nodes = Generics.convertList(XPath.select(doc, "//*[local-name()='" + includeTag + "']"));
        } catch (TransformerException e) {
            throw new RuntimeException("Unexpected XPath error!");
        }
        for (Element elem : nodes) {
            if ((namespace == null && elem.getNamespaceURI() != null) || (namespace != null && !namespace.equals(elem.getNamespaceURI()))) {
                continue;
            }

            String xpath = null, refid = null, section = null;
            if (elem.hasAttribute("xpath")) {
                    xpath = elem.getAttribute("xpath");
            }
            if (elem.hasAttribute("refid")) {
                refid = elem.getAttribute("refid");
            }
            if (elem.hasAttribute("section")) {
                section = elem.getAttribute("section");
            }
            
            if (xpath != null) {
                if (refid != null || section != null) {
                    throw new SAXException("Only one of the \"xpath\", \"refid\" or \"section\" attributes may be supplied to the include tag!");
                }
                // Just use the supplied XPath expression
            } else if (refid != null) {
                if (section != null || xpath != null) {
                    throw new SAXException("Only one of the \"xpath\", \"refid\" or \"section\" attributes may be supplied to the include tag!");
                }
                xpath = "/*[local-name() = '" + CONFIG_FRAGMENTS_ROOT_TAG + "' and namespace-uri()='" + CONFIG_FRAGMENTS_NS + "']/*[@id='" + refid + "']/node()";
            } else if (section != null) {
                if (xpath != null || refid != null) {
                    throw new SAXException("Only one of the \"xpath\", \"refid\" or \"section\" attributes may be supplied to the include tag!");
                }
                if (!checkSectionType(section)) {
                    throw new SAXException("\"" + section + "\" is not a valid include section type!");
                }
                xpath = "/*[local-name()='" + CONFIG_FRAGMENTS_ROOT_TAG + "' and namespace-uri()='" + CONFIG_FRAGMENTS_NS + "']/*[local-name()='" + section + "' and namespace-uri()='" + CONFIG_FRAGMENTS_NS + "']/node()";
            } else {
                throw new SAXException("One of the \"xpath\", \"refid\" or \"section\" attributes must be set for the include tag!");
            }

            String filepath = elem.getAttribute("file");
            if (filepath == null) {
                throw new SAXException("The attribute \"file\" must be set for the include tag!");
            }

            // Look if the same include has been performed ealier in the recursion
            // If yes, we have a cyclic dependency
            Set<Tupel<String, String>> list = includesList.get();
            if (list == null) {
                list = new HashSet<Tupel<String, String>>();
                includesList.set(list);
            }
            if (list.contains(filepath + "#" + xpath)) {
                throw new SAXException("Cyclic dependency in include detected: " + filepath.toString());
            }
            
            FileResource includeFile = ResourceUtil.getFileResourceFromDocroot(filepath);
            Document includeDocument;
            try {
                includeDocument = Xml.parseMutable(includeFile);
            } catch (IOException e) {
                throw new SAXException("I/O exception on included file " + includeFile.toString(), e);
            }
            
            if (!CONFIG_FRAGMENTS_NS.equals(includeDocument.getDocumentElement().getNamespaceURI()) || !CONFIG_FRAGMENTS_ROOT_TAG.equals(includeDocument.getDocumentElement().getLocalName())) {
                throw new SAXException("File " + filepath + " seems not to be a valid configuration fragments file!");
            }

            list.add(new Tupel<String, String>(filepath, xpath));
            try {
                resolveIncludes(includeDocument);
            } finally {
                list.remove(new Tupel<String, String>(filepath, xpath));
            }
            
            NodeList includeNodes;
            try {
                javax.xml.xpath.XPath xpathProc = XPathFactory.newInstance().newXPath();
                xpathProc.setNamespaceContext(new DOMBasedNamespaceContext(elem));
                includeNodes = (NodeList) xpathProc.evaluate(xpath, includeDocument, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new SAXException("XPath Expression invalid: " + xpath);
            }
            
            for (int i=0; i < includeNodes.getLength(); i++) {
                Node node = includeNodes.item(i);
                Node newNode = doc.importNode(node, true);
                elem.getParentNode().insertBefore(newNode, elem);
            }
            elem.getParentNode().removeChild(elem);
            
            // Trigger event
            FileIncludeEvent ev = new FileIncludeEvent(this, includeFile);
            for (FileIncludeEventListener listener : listeners) {
                listener.fileIncluded(ev);
            }
        }
    }

    private boolean checkSectionType(String section) {
        if (section.equals("targets") || section.equals("navigation") || section.equals("pageflows") || 
            section.equals("pagerequests") || section.equals("properties") || section.equals("interceptors") || 
            section.equals("scriptedflows") || section.equals("roles") || section.equals("authconstraints") || 
            section.equals("conditions") || section.equals("resources") || section.equals("directoutputpagerequests")) {
            return true;
        } else {
            return false;
        }
    }
    
    private class Tupel<A, B> {
        private A obj1;
        private B obj2;
        
        public Tupel(A v1, B v2) {
            obj1 = v1;
            obj2 = v2;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            
            if (!(obj instanceof Tupel)) {
                return false;
            }
            Tupel<?, ?> tupel = (Tupel<?, ?>) obj;
            
            return obj1.equals(tupel.obj1) && obj2.equals(tupel.obj2);
        }
        
        @Override
        public int hashCode() {
            return obj1.hashCode() + obj2.hashCode();
        }
    }
}
