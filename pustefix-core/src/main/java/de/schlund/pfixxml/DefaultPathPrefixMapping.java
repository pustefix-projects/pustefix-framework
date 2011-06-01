package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultPathPrefixMapping implements PathPrefixMapping {

    private List<Prefix> prefixes = new ArrayList<Prefix>();
    private Map<String, Prefix> pathToPrefix = new HashMap<String, Prefix>();
    
    public DefaultPathPrefixMapping(final Document doc) {
        NodeList pathNodes = doc.getElementsByTagName("page-prefix");
        for(int i=0; i<pathNodes.getLength(); i++) {
            Element prefixElem = (Element)pathNodes.item(i);
            Prefix prefix = new Prefix();
            prefix.name = prefixElem.getTextContent().trim();
            if(prefix.name.startsWith("/")) prefix.name = prefix.name.substring(1);
            if(prefix.name.endsWith("/")) prefix.name = prefix.name.substring(0, prefix.name.length()-1);
            NamedNodeMap attrMap = prefixElem.getAttributes();
            for(int j=0; j<attrMap.getLength(); j++) {
                Node attrNode = attrMap.item(j);
                prefix.attributes.put(attrNode.getNodeName(), attrNode.getNodeValue());
            }
            prefixes.add(prefix);
        }
    }
    
    public String getPrefix(final Map<String, String> pageSelectors) {
        String bestMatchPrefix = "";
        if(pageSelectors != null && pageSelectors.size() > 0) {
            int bestMatchNo = 0;
            int bestMatchNoCheck = Integer.MAX_VALUE;
            for(Prefix prefix: prefixes) {
                Iterator<String> prefixAttrs = prefix.attributes.keySet().iterator();
                boolean match = true;
                int matchNo = 0;
                while(prefixAttrs.hasNext() && match) {
                    String prefixAttr = prefixAttrs.next();
                    String prefixAttrVal = prefix.attributes.get(prefixAttr);
                    String pageSelectorVal = pageSelectors.get(prefixAttr);
                    if(pageSelectorVal != null) {
                        if(pageSelectorVal.equals(prefixAttrVal)) {
                            matchNo++;
                        } else {
                            match = false;
                        }
                    }
                }
                if(match) {
                    int noCheck = prefix.attributes.size() + pageSelectors.size() - matchNo * 2; 
                    if(matchNo > bestMatchNo || (matchNo == bestMatchNo && noCheck < bestMatchNoCheck)) {
                        bestMatchPrefix = prefix.name;
                        bestMatchNo = matchNo;
                        bestMatchNoCheck = noCheck;
                    }
                }
            }
        }
        return bestMatchPrefix;
    }
    
    public String unmap(final String page, Map<String, String> pageSelectors) {
        String resultPage = page;
        int ind = page.lastIndexOf('/');
        if(ind > -1) {
            String path = page.substring(0, ind);
            for(Prefix prefix: prefixes) {
                if(path.startsWith(prefix.name)) {
                    
                }
               
            }
        }
        if(page.contains("/")) {
            
        }
        return resultPage;
    }
    
    class Prefix {
        
        String name;
        Map<String, String> attributes = new HashMap<String, String>();
        
    }
    
}
