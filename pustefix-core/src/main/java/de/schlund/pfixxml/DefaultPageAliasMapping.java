package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultPageAliasMapping implements PageAliasMapping {

    private Logger LOG = Logger.getLogger(DefaultPageAliasMapping.class);
    
    private Map<String, List<Alias>> pageToAliases = new HashMap<String, List<Alias>>();
    private Map<String, Set<String>> aliasToPages = new HashMap<String, Set<String>>();
    
    public DefaultPageAliasMapping(final Document doc) {
        NodeList pageNodes = doc.getElementsByTagName("page");
        for(int i=0; i<pageNodes.getLength(); i++) {
            Element pageElem = (Element)pageNodes.item(i);
            String pageName = pageElem.getAttribute("name");
            List<Element> aliasElems = DOMUtils.getChildElementsByTagName(pageElem, "alias");
            List<Alias> aliases = new ArrayList<Alias>();
            for(Element aliasElem: aliasElems) {
                getAliases(aliasElem, new HashMap<String, String>(), aliases);
            }
            pageToAliases.put(pageName, aliases);
            for(Alias alias: aliases) {
                Set<String> pages = aliasToPages.get(alias.name);
                if(pages == null) {
                    pages = new HashSet<String>();
                    aliasToPages.put(alias.name, pages);
                }
                pages.add(pageName);
            }
        }
    }
    
    private void getAliases(final Element aliasElem, final Map<String, String> parentAliasAttrs, final List<Alias> aliases) {
        Map<String, String> aliasAttrs = new HashMap<String, String>(parentAliasAttrs);
        NamedNodeMap attrMap = aliasElem.getAttributes();
        for(int i=0; i<attrMap.getLength(); i++) {
            Node attrNode = attrMap.item(i);
            aliasAttrs.put(attrNode.getNodeName(), attrNode.getNodeValue());
        }
        List<Element> childAliasElems = DOMUtils.getChildElementsByTagName(aliasElem, "alias");
        if(childAliasElems.size() == 0) {
            String aliasName = aliasElem.getTextContent().trim();
            if(aliasName.startsWith("/")) aliasName = aliasName.substring(1);
            if(aliasName != null) {
                Alias alias = new Alias();
                alias.name = aliasName;
                alias.attributes = aliasAttrs;
                aliases.add(alias);
            } else {
                LOG.warn("Found empty page alias!");
            }
        } else {
           for(Element childAliasElem: childAliasElems) {
               getAliases(childAliasElem, aliasAttrs, aliases);
           }
        }
    }
    
    public String getAlias(final String page, final Map<String, String> pageSelectors) {
        //TODO: cache lookup results
        String resultAlias = page;
        if(pageSelectors != null) {
            List<Alias> aliases = pageToAliases.get(page);
            if(aliases != null) {
                Alias bestMatchAlias = getBestMatchingAlias(aliases, pageSelectors);
                if(bestMatchAlias != null) resultAlias = bestMatchAlias.name;
            }
        }
        return resultAlias;
    }
    
    private Alias getBestMatchingAlias(List<Alias> aliases, Map<String, String> pageSelectors) {
        Alias bestMatchAlias = null;
        int bestMatchNo = 0;
        int bestMatchNoCheck = Integer.MAX_VALUE;
        for(Alias alias: aliases) {
            Iterator<String> aliasAttrs = alias.attributes.keySet().iterator();
            boolean match = true;
            int matchNo = 0;
            while(aliasAttrs.hasNext() && match) {
                String aliasAttr = aliasAttrs.next();
                String aliasAttrVal = alias.attributes.get(aliasAttr);
                String pageSelectorVal = pageSelectors.get(aliasAttr);
                if(pageSelectorVal != null) {
                    if(pageSelectorVal.equals(aliasAttrVal)) {
                        matchNo++;
                    } else {
                        match = false;
                    }
                } 
            }
            if(match && matchNo > 0) {
                int noCheck = alias.attributes.size() + pageSelectors.size() - matchNo * 2; 
                if(matchNo > bestMatchNo || (matchNo == bestMatchNo && noCheck < bestMatchNoCheck)) {
                    bestMatchAlias = alias;
                    bestMatchNo = matchNo;
                    bestMatchNoCheck = noCheck;
                }
            }
        }
        return bestMatchAlias;
    }
    
    public String getPage(final String alias, final Map<String, String> pageSelectors) {
        //TODO: cache lookup results
        String resultPage = alias;
        Set<String> pages = aliasToPages.get(alias);
        if(pages != null) {
            String bestMatchPage = null;
            int bestMatchNo = 0;
            int bestMatchNoCheck = Integer.MAX_VALUE;
            for(String page: pages) {
                List<Alias> aliasObjs = pageToAliases.get(page);
                for(Alias aliasObj: aliasObjs) {
                    if(aliasObj.name.equals(alias)) {
                        boolean match = true;
                        int matchNo = 0;
                        Iterator<String> aliasAttrs = aliasObj.attributes.keySet().iterator();
                        while(aliasAttrs.hasNext() && match) {
                            String aliasAttr = aliasAttrs.next();
                            String aliasAttrVal = aliasObj.attributes.get(aliasAttr);
                            String pageSelectorVal = pageSelectors.get(aliasAttr);
                            if(pageSelectorVal != null) {
                                if(pageSelectorVal.equals(aliasAttrVal)) {
                                    matchNo++;
                                } else {
                                    match = false;
                                }
                            } 
                        }
                        if(match && matchNo > 0) {
                            int noCheck = aliasObj.attributes.size() + pageSelectors.size() - matchNo * 2; 
                            if(matchNo > bestMatchNo || (matchNo == bestMatchNo && noCheck < bestMatchNoCheck)) {
                                bestMatchPage = page;
                                bestMatchNo = matchNo;
                                bestMatchNoCheck = noCheck;
                            }
                        }
                    }
                }
            }
            if(bestMatchPage != null) resultPage = bestMatchPage;
        }
        return resultPage;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> pages = pageToAliases.keySet().iterator();
        while(pages.hasNext()) {
            String page = pages.next();
            sb.append(page).append("\n");
            Iterator<Alias> aliases = pageToAliases.get(page).iterator();
            while(aliases.hasNext()) {
                Alias alias = aliases.next();
                sb.append("   ").append(alias.name).append(" [");
                Iterator<String> attrs = alias.attributes.keySet().iterator();
                while(attrs.hasNext()) {
                    String attr = attrs.next();
                    sb.append(attr).append("=").append(alias.attributes.get(attr));
                    if(attrs.hasNext()) sb.append(", ");
                }
                sb.append("]\n");
            }
        }
        return sb.toString();
    }
    
    class Alias {
        
        String name;
        Map<String, String> attributes = new HashMap<String, String>();
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("[");
            Iterator<String> it = attributes.keySet().iterator();
            while(it.hasNext()) {
                String attrName = it.next();
                String attrVal = attributes.get(attrName);
                sb.append(attrName).append("=").append(attrVal);
                if(it.hasNext()) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
        
    }
    
}
