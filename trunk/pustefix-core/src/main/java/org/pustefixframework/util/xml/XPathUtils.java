package org.pustefixframework.util.xml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class XPathUtils {

    public static NamespaceContext createNamespaceContext(final String nsPrefix, final String nsUri) {
        
        return new NamespaceContext() {
   
            public String getNamespaceURI(String prefix) {
                if(prefix.equals(nsPrefix)) {
                    return nsUri;
                } else {
                    return XMLConstants.NULL_NS_URI;
                }
            }
            
            public String getPrefix(String namespace) {
                if(namespace.equals(nsUri)) {
                    return nsPrefix;
                } else {
                    return null;
                }
            }
    
            public Iterator<String> getPrefixes(String namespace) {
                return null;
            }
    
        };
    }
        
}
