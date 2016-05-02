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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {
    
    private Map<String, String> prefixToURI = new HashMap<String, String>();
    
    public SimpleNamespaceContext() {
        prefixToURI.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
        prefixToURI.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        prefixToURI.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }
    
    public void addNamespace(String prefix, String uri) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix must not be null!");
        }
        if (uri == null) {
            throw new IllegalArgumentException("Namespace URI must not be null!");
        }
        prefixToURI.put(prefix, uri);
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix must not be null!");
        }
        String uri = prefixToURI.get(prefix);
        if (uri == null) {
            return XMLConstants.NULL_NS_URI;
        } else {
            return uri;
        }
    }

    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI must not be null!");
        }
        for (String prefix : prefixToURI.keySet()) {
            if (prefixToURI.get(prefix).equals(namespaceURI)) {
                return prefix;
            }
        }
        return null;
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI must not be null!");
        }
        Set<String> prefixes = new HashSet<String>();
        for (String prefix : prefixToURI.keySet()) {
            if (prefixToURI.get(prefix).equals(namespaceURI)) {
                prefixes.add(prefix);
            }
        }
        return Collections.unmodifiableSet(prefixes).iterator();
    }

}
