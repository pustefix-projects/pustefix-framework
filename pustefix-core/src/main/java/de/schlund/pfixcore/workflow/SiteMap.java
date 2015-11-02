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

package de.schlund.pfixcore.workflow;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.includes.FileIncludeEvent;
import de.schlund.pfixxml.config.includes.FileIncludeEventListener;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

public class SiteMap {
    
    public final static String XMLNS = "http://www.pustefix-framework.org/2011/namespace/sitemap";
    
    private Logger LOG = Logger.getLogger(SiteMap.class);
    
    private Resource siteMapFile;
    
    private Set<Resource> fileDependencies = new HashSet<Resource>();
    private long lastFileModTime;
    private Map<String, Document> langToDoc = new HashMap<String, Document>();
    private List<Page> pageList = new ArrayList<Page>();
    private Map<String, Page> pageNameToPage = new LinkedHashMap<String, Page>();
    private Map<String, Map<String, String>> aliasMaps = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> pageMaps = new HashMap<String, Map<String, String>>();
    private Map<String, Page> pageAlternativeToPage = new HashMap<String, Page>();
    private Map<String, Page> pageAliasToPage = new HashMap<String, Page>();
    private Map<String, String> pageFlowToPrefix = new HashMap<>();
    private Map<String, String> prefixToPageFlow = new HashMap<>();
    private List<PageGroup> pageGroups = new ArrayList<>();
    private Map<String, PageGroup> prefixToPageGroup = new HashMap<>();
    private Map<String, PageGroup> keyToPageGroup = new HashMap<>();
    private boolean provided;
    
    public SiteMap(File siteMapFile, File[] siteMapAliasFiles) throws IOException, SAXException {
        if(siteMapFile.exists()) {
            Document siteMapDoc = Xml.parseMutable(siteMapFile);
            readSiteMap(siteMapDoc.getDocumentElement());
            for(File siteMapAliasFile: siteMapAliasFiles) {
                readSiteMapAliases(Xml.parseMutable(siteMapAliasFile));
            }
            provided = true;
        }
    }
    
    public SiteMap(Resource siteMapFile) throws IOException, SAXException, XMLException {
        init(siteMapFile);
    }
    
    private void init(Resource file) throws IOException, SAXException, XMLException {
        if(siteMapFile == null) {
            URI uri = file.toURI();
            String uriStr = uri.toString();
            if(uriStr.endsWith("depend.xml")) uriStr = uriStr.substring(0, uriStr.length() -10) + "sitemap.xml";
            siteMapFile = ResourceUtil.getResource(uriStr);
        }
        if(siteMapFile.exists()) {
            
            lastFileModTime = siteMapFile.lastModified();
            Document siteMapDoc = Xml.parseMutable(siteMapFile);
            
            // Make sure list of dependencies only contains the file itself
            fileDependencies.clear();
            fileDependencies.add(siteMapFile);
            FileIncludeEventListener listener = new FileIncludeEventListener() {
    
                public void fileIncluded(FileIncludeEvent event) {
                    fileDependencies.add(event.getIncludedFile());
                }
    
            };
            IncludesResolver iresolver = new IncludesResolver(null, "config-include");
            iresolver.registerListener(listener);
            iresolver.resolveIncludes(siteMapDoc);
            iresolver = new IncludesResolver(XMLNS, "config-include");
            iresolver.registerListener(listener);
            iresolver.resolveIncludes(siteMapDoc);
            
            TransformerFactory tf = TransformerFactory.newInstance();
            if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
                SAXTransformerFactory stf = (SAXTransformerFactory) tf;
                TransformerHandler th;
                try {
                    th = stf.newTransformerHandler();
                } catch (TransformerConfigurationException e) {
                   throw new XMLException("Error reading sitemap", e);
                }
                Document doc;
                try {
                    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                } catch (ParserConfigurationException e) {
                    throw new XMLException("Error creating sitemap document", e);
                }
                DOMResult dr = new DOMResult(doc);
                th.setResult(dr);
                DefaultHandler dh = new TransformerHandlerAdapter(th);
                DefaultHandler ch = new CustomizationHandler(dh);
                XMLReader xreader = XMLReaderFactory.createXMLReader();
                xreader.setContentHandler(ch);
                xreader.setDTDHandler(ch);
                xreader.setEntityResolver(ch);
                xreader.setErrorHandler(ch);
                xreader.parse(new InputSource(new StringReader(Xml.serialize(siteMapDoc,false, true))));
                siteMapDoc = dr.getNode().getOwnerDocument();
                if (siteMapDoc == null) {
                    if (dr.getNode() instanceof Document) {
                        siteMapDoc = (Document) dr.getNode();
                    } else {
                        throw new RuntimeException("XML result is not a Document though it should be");
                    }
                }
            } else {
                throw new RuntimeException("TransformerFactory instance does not provide SAXTransformerFactory!");
            }
            
            DOMUtils.mergeChildElements(siteMapDoc.getDocumentElement(), "page", "name");
            readSiteMap(siteMapDoc.getDocumentElement());
            
            Resource res = ResourceUtil.getResource("/WEB-INF/sitemap-aliases.xml");
            if(res.exists()) {
                readSiteMapAliases(Xml.parseMutable(res));
            }
                
            ModuleInfo moduleInfo = ModuleInfo.getInstance();
            Set<String> moduleNames = moduleInfo.getModules();
            Iterator<String> it = moduleNames.iterator();
            while(it.hasNext()) {
                res = ResourceUtil.getResource("module://" + it.next() + "/conf/sitemap-aliases.xml");
                if(res.exists()) {
                    readSiteMapAliases(Xml.parseMutable(res));
                    fileDependencies.add(res);
                }
            }
        
            for(Resource fileDependency: fileDependencies) {
                long lastMod = fileDependency.lastModified();
                if(lastMod > lastFileModTime) lastFileModTime = lastMod;
            }
            
            provided = true;
        }
    }
    
    public synchronized boolean tryReinit() throws Exception {
        if (needsReload()) {
            LOG.info("##### Reloading sitemap #####");
            reload();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean needsReload() {
        for (Resource file : fileDependencies) {
            if (file.lastModified() > lastFileModTime) {
                return true;
            }
        }
        return false;
    }
    
    private void reload() throws IOException, SAXException, XMLException  {
        fileDependencies.clear();
        lastFileModTime = 0;
        langToDoc.clear();
        pageList.clear();
        pageNameToPage.clear();
        aliasMaps.clear();
        pageMaps.clear();
        pageAlternativeToPage.clear();
        pageAliasToPage.clear();
        init(siteMapFile);
    }
    
    public boolean isProvided() {
        return provided;
    }
    
    private void readSiteMap(Element siteMapElem) {
        List<Element> pageGroupElems = DOMUtils.getChildElementsByTagName(siteMapElem, "page-group");
        for(Element pageGroupElem: pageGroupElems) {
            PageGroup pageGroup = readPageGroup(pageGroupElem, null);
            pageGroups.add(pageGroup);
            prefixToPageGroup.put(pageGroup.name, pageGroup);
            keyToPageGroup.put(pageGroup.key, pageGroup);
        }
        List<Element> pageElems = DOMUtils.getChildElementsByTagName(siteMapElem, "page");
        for(Element pageElem: pageElems) {
            Page page = readPage(pageElem);
            pageList.add(page);
        }
        List<Element> pageFlowElems = DOMUtils.getChildElementsByTagName(siteMapElem, "pageflow");
        for(Element pageFlowElem: pageFlowElems) {
            String name = pageFlowElem.getAttribute("name").trim();
            if(!name.isEmpty()) {
            	String prefix = pageFlowElem.getAttribute("prefix").trim();
            	pageFlowToPrefix.put(name, prefix);
            	prefixToPageFlow.put(prefix, name);
            }
        }
    }
    
    private PageGroup readPageGroup(Element pageGroupElem, PageGroup parentPageGroup) {
        PageGroup pageGroup = new PageGroup();
        pageGroup.key = pageGroupElem.getAttribute("key").trim();
        pageGroup.name = pageGroupElem.getAttribute("name").trim();
        pageGroup.parent = parentPageGroup;
        List<Element> childGroups = DOMUtils.getChildElementsByTagName(pageGroupElem, "page-group");
        for(Element childGroup: childGroups) {
            PageGroup subGroup = readPageGroup(childGroup, pageGroup);
            pageGroup.pageGroups.add(subGroup);
            prefixToPageGroup.put(subGroup.name, subGroup);
            keyToPageGroup.put(subGroup.key, subGroup);
        }
        List<Element> childPages = DOMUtils.getChildElementsByTagName(pageGroupElem, "page");
        for(Element childPage: childPages) {
            Page page = readPage(childPage);
            pageGroup.pages.add(page);
        }
        return pageGroup;
    }
    
    private Page readPage(Element pageElem) {
        String name = pageElem.getAttribute("name").trim();
        Page page = new Page(name);
        String internal = pageElem.getAttribute("internal").trim();
        if(internal.length() > 0) {
            page.internal = Boolean.valueOf(internal);
        }
        NamedNodeMap map = pageElem.getAttributes();
        if(map != null) {
            for(int i=0; i<map.getLength(); i++) {
                Node attrNode = map.item(i);
                String attrName = attrNode.getNodeName();
                if(!("name".equals(attrName)||("internal").equals(attrName))) {
                    page.customAttributes.put(attrName, attrNode.getNodeValue());
                }
            }
        }
        pageNameToPage.put(page.name, page);
        String alias = pageElem.getAttribute("alias").trim();
        if(alias.length() > 0) {
            page.alias = alias;
            pageAliasToPage.put(alias, page);
        }
        List<Element> childAlts = DOMUtils.getChildElementsByTagName(pageElem, "alt");
        for(Element childAlt: childAlts) {
            String altKey = childAlt.getAttribute("key");
            String altName = childAlt.getAttribute("name");
            boolean defaultAlt = Boolean.valueOf(childAlt.getAttribute("default"));
            page.pageAltKeyToName.put(altKey, altName);
            page.pageNameToAltKey.put(altName, altKey);
            pageAlternativeToPage.put(altName, page);
            
            PageAlternative pageAlt = new PageAlternative();
            pageAlt.key = altKey;
            pageAlt.name = altName;
            page.pageAltKeyMap.put(altKey, pageAlt);
            if(defaultAlt) {
            	page.defaultPageAlt = pageAlt;
            }
            NamedNodeMap altMap = childAlt.getAttributes();
            if(altMap != null) {
                for(int i=0; i<altMap.getLength(); i++) {
                    Node attrNode = altMap.item(i);
                    String attrName = attrNode.getNodeName();
                    if(!("name".equals(attrName)||("key").equals(attrName)||"default".equals(attrName))) {
                        pageAlt.customAttributes.put(attrName, attrNode.getNodeValue());
                    }
                }
            }
            
        }
        List<Element> childPages = DOMUtils.getChildElementsByTagName(pageElem, "page");
        for(Element childPage: childPages) {
            page.pages.add(readPage(childPage));
        }
        return page;
    }
    
    private void readSiteMapAliases(Document siteMapAliasesDoc) {
    	
        Element root = siteMapAliasesDoc.getDocumentElement();
        readSiteMapAliases(root);
        List<Element> aliasGroupElems = DOMUtils.getChildElementsByTagName(root, "alias-group");
        for(Element aliasGroupElem: aliasGroupElems) {
        	readSiteMapAliases(aliasGroupElem);
        }
        
    }
    
    private void readSiteMapAliases(Element siteMapAliasesGroup) {
    	String lang = siteMapAliasesGroup.getAttribute("lang").trim();
        if(lang.length() > 0) {
        	Map<String, String> pageToAlias = new HashMap<String, String>();
            aliasMaps.put(lang, pageToAlias);
            Map<String, String> aliasToPage = new HashMap<String, String>();
            pageMaps.put(lang, aliasToPage);
            List<Element> aliasElems = DOMUtils.getChildElementsByTagName(siteMapAliasesGroup, "alias");
	        for(Element aliasElem: aliasElems) {
	            String page = aliasElem.getAttribute("page").trim();
	            String alias = aliasElem.getTextContent().trim();
	            pageToAlias.put(page, alias);
	            aliasToPage.put(alias, page);
	        }
        }
    }
    
    private Document getDocument(String lang) {
        Document doc = Xml.createDocument();
        Element root = doc.createElement("sitemap");
        doc.appendChild(root);
        if(pageList != null) {
            for(Page page: pageList) {
                addPage(page, root, lang);
            }
        }
        return doc;
    }
    
    private void addPage(Page page, Element parent, String lang) {
        Element elem = parent.getOwnerDocument().createElement("page");
        elem.setAttribute("name", page.name);
        Map<String, String> cusAttrs = page.customAttributes;
        Iterator<String> it = cusAttrs.keySet().iterator();
        while(it.hasNext()) {
            String attrName = it.next();
            String attrVal = cusAttrs.get(attrName);
            elem.setAttribute(attrName, attrVal);
        }
        String alias = getAlias(page.name, lang);
        if(!page.name.equals(alias)) elem.setAttribute("alias", alias);
        parent.appendChild(elem);
        for(String pageAltKey: page.pageAltKeyToName.keySet()) {
            Element altElem = parent.getOwnerDocument().createElement("alt");
            altElem.setAttribute("name", getAlias(page.pageAltKeyToName.get(pageAltKey), lang));
            altElem.setAttribute("key", pageAltKey);
            PageAlternative pageAlt = page.pageAltKeyMap.get(pageAltKey);
            for(String attrName: pageAlt.customAttributes.keySet()) {
                String attrVal = pageAlt.customAttributes.get(attrName);
                altElem.setAttribute(attrName, attrVal);
            }
            elem.appendChild(altElem);
        }
        for(Page child: page.pages) {
            addPage(child, elem, lang);
        }
    }
    
    public List<String> getPageNames(boolean excludeInternal) {
        List<String> pageNames = new ArrayList<String>();
        for(Page page: pageList) {
            getPageNames(page, excludeInternal, pageNames);
        }
        return pageNames;
    }
    
    private void getPageNames(Page page, boolean excludeInternal, List<String> pageNames) {
        if(!excludeInternal || !page.internal) {
            pageNames.add(page.name);
            for(Page child: page.pages) {
                getPageNames(child, excludeInternal, pageNames);
            }
        }
    }
    
    public String getAlias(final String name, final String lang) {
        String aliasName = name;
        if(pageNameToPage != null) {
            Page page = pageNameToPage.get(name);
            if(page != null && page.alias != null) {
                aliasName = page.alias;
            }
        }
        String aliasTranslated = null;
        if(lang != null) {
            if(aliasMaps != null) {
                Map<String, String> aliases = aliasMaps.get(lang);
                if(aliases != null) {
                    aliasTranslated = aliases.get(aliasName);
                }
                if(aliasTranslated == null) {
                    int ind = lang.indexOf('_');
                    if(ind > -1) {
                        String mainLang = lang.substring(0, ind);
                        aliases = aliasMaps.get(mainLang);
                        if(aliases != null) {
                            aliasTranslated = aliases.get(aliasName);
                        }
                    }
                }
            }
        }
        if(aliasTranslated != null) {
            return aliasTranslated;
        } else {
            return aliasName;
        }
    }
    
    public String getAlias(String name, String lang, String pageAlternativeKey) {
        if(pageAlternativeKey == null || pageAlternativeKey.equals("")) {
        	Page page = pageNameToPage.get(name);
        	if(page == null || page.defaultPageAlt == null) {
        		return getAlias(name, lang);
        	} else {
        		pageAlternativeKey = page.defaultPageAlt.key;
        	}
        }
        String pageName = name;
        String altPageName = getPageAlternative(name, pageAlternativeKey);
        if(altPageName != null) {
        	pageName = altPageName;
        }
        return getAlias(pageName, lang);
    }
    
    private String getPageAlternative(String pageName, String pageAlternativeKey) {
        Page page = pageNameToPage.get(pageName);
        if(page != null) {
            return page.pageAltKeyToName.get(pageAlternativeKey);
        }
        return null;
    }
    
    public List<String> getPageAlternativeAliases(String name, String lang) {
        Page page = pageNameToPage.get(name);
        if(page != null && page.pageAltKeyToName.size() > 0) {
            List<String> aliases = new ArrayList<String>();
            for(String pageAltKey: page.pageAltKeyToName.keySet()) {
                aliases.add(getAlias(name, lang, pageAltKey));
            }
            return aliases;
        }
        return null;
    }
    
    public Map<String, String> getPageFlowPrefixes() {
    	return pageFlowToPrefix;
    }
    
    public Set<String> getPageAlternativeKeys(String pageName) {
        if(pageName != null) {
            Page page = pageNameToPage.get(pageName);
            if(page != null && page.pageAltKeyToName.size() > 0) {
                return page.pageAltKeyToName.keySet();
            }
        }
        return null;
    }
    
    public String getPageFlowAlias(String name, String lang) {
    	//TODO: language alias support
    	String alias = pageFlowToPrefix.get(name);
    	if(alias != null) {
    		return alias;
    	} else {
    		return name;
    	}
    }
    
    public String getPageFlow(String alias, String lang) {
    	//TODO: language alias support
    	String pageFlow = prefixToPageFlow.get(alias);
    	if(pageFlow == null) {
    	    return alias;
    	} else {
    	    return pageFlow;
    	}
    }
    
    public List<PageGroup> getPageGroups() {
        return pageGroups;
    }
    
    public PageGroup getPageGroup(String key) {
        return keyToPageGroup.get(key);
    }
    
    public PageGroup getPageGroup(String alias, String lang) {
        
        //TODO: language alias support
        PageGroup pageGroup = null;
        String[] groups = alias.split("/");
        if(groups.length > 0) {
            pageGroup = prefixToPageGroup.get(groups[0]);
            if(pageGroup != null) {
                if(groups.length > 1) {
                    for(int i = 1; i < groups.length; i++) {
                        pageGroup = pageGroup.getPageGroup(groups[i]);
                        if(pageGroup == null) {
                            return null;
                        }
                    }
                }
            }
        }
        return pageGroup;
    } 
    
    
    
    public PageLookupResult getPageName(String alias, String lang) {
        String aliasPageName = alias;
        String page = null;
        String aliasKey = null;
        if(lang != null) {
            Map<String, String> pages = pageMaps.get(lang);
            if(pages != null) {
                page = pages.get(alias);
            }
            if(page == null) {
                int ind = lang.indexOf('_');
                if(ind > -1) {
                    lang = lang.substring(0, ind);
                    pages = pageMaps.get(lang);
                    if(pages != null) {
                        page = pages.get(alias);
                    }
                }
            }
            if(page != null) {
                if(pageAliasToPage != null) {
                    Page p = pageAliasToPage.get(page);
                    if(p != null) page = p.name;
                }
            }
        }
        if(page == null) {
            if(pageAliasToPage != null) {
                Page p = pageAliasToPage.get(alias);
                if(p != null) page = p.name;
            }
            if (page == null) page = alias;
        }
        if(pageAlternativeToPage != null) {
            Page pageAlt = pageAlternativeToPage.get(page);
            if(pageAlt != null) {
                aliasKey = pageAlt.pageNameToAltKey.get(page);
                page = pageAlt.name;
            } else {
            	Page p = pageNameToPage.get(page);
            	if(p != null && p.defaultPageAlt != null) {
            		aliasKey = p.defaultPageAlt.key;
            	}
            }
        }
        if(page.equals(alias) && page.contains("/")) {
            aliasPageName = page.substring(0, page.lastIndexOf('/'));
            return getPageName(aliasPageName, lang);
        }
        return new PageLookupResult(page, aliasKey, aliasPageName);
    }
    
    public Element getSiteMapXMLElement(XsltVersion xsltVersion, String language) {
        Document doc = null;
        synchronized(langToDoc) {
            doc = langToDoc.get(language + "@" + xsltVersion);
        }
        if(doc == null) {
            doc = Xml.parse(xsltVersion, getDocument(language));
            synchronized(langToDoc) {
                langToDoc.put(language + "@" + xsltVersion, doc);
            }
        }
        return doc.getDocumentElement();
    }
    
    public boolean hasDefaultPageAlternative(String pageName) {
        Page page = pageNameToPage.get(pageName);
        if(page != null) {
            return page.defaultPageAlt != null;
        }
        return false;
    }
    
    public String getDefaultPageAlternativeKey(String pageName) {
        Page page = pageNameToPage.get(pageName);
        if(page != null && page.defaultPageAlt != null) {
            return page.defaultPageAlt.key;
        }
        return null;
    }
    
    public class Page {
        
        String name;
        boolean internal;
        Map<String, String> customAttributes = new HashMap<String, String>();
        String alias;
        List<Page> pages = new ArrayList<Page>();
        Map<String, String> pageAltKeyToName = new LinkedHashMap<String, String>();
        Map<String, String> pageNameToAltKey = new HashMap<String, String>();
        Map<String, PageAlternative> pageAltKeyMap = new LinkedHashMap<String, PageAlternative>();
        PageAlternative defaultPageAlt;
        
        Page(String name) {
            this.name = name;
        }
        
    }
    
    public class PageAlternative {
    	
    	String key;
    	String name;
    	
    	Map<String, String> customAttributes = new HashMap<String, String>();
    	
    }
    
    public class PageGroup {
        
        public String key;
        public String name;
        public PageGroup parent;
        
        public List<PageGroup> pageGroups = new ArrayList<>();
        List<Page> pages = new ArrayList<>();

        PageGroup getPageGroup(String name) {
            for(PageGroup pageGroup: pageGroups) {
                if(pageGroup.name.equals(name)) {
                    return pageGroup;
                }
            }
            return null;
        }
        
        public PageGroup lookup(String pageName) {
            return lookup(this, pageName);
        }
        
        PageGroup lookup(PageGroup pageGroup, String pageName) {
            for(Page page: pageGroup.pages) {
                if(page.name.equals(pageName)) {
                    return pageGroup;
                }
            }
            if(pageGroup.parent != null) {
                return lookup(pageGroup.parent, pageName);
            } else {
                return null;
            }
        }
        
        public String getPrefix() {
            String prefix = null;
            PageGroup group = this;
            while(group != null) {
                prefix = group.name + (prefix == null ? "" : "/" + prefix);
                group = group.parent;
            }
            return prefix;
        }
          
    }
    
    public class PageLookupResult {
        
        PageLookupResult(String pageName, String pageAlternativeKey, String aliasPageName) {
            this.pageName = pageName;
            this.pageAlternativeKey = pageAlternativeKey;
            this.aliasPageName = aliasPageName;
        }
        
        String pageName;
        String pageAlternativeKey;
        String aliasPageName;
        
        public String getPageName() {
            return pageName;
        }
        
        public String getPageAlternativeKey() {
            return pageAlternativeKey;
        }
        
        public String getAliasPageName() {
            return aliasPageName;
        }
    
    }

}
