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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
    
    private Set<Resource> fileDependencies = new HashSet<Resource>();
    private long loadTime = 0;

    private Set<String> topLevelPages = new HashSet<String>();
    
    
    private Document siteMapDoc;
    private Element siteMapXMLElement = null;
    
    
    private List<Page> pageList;
    private Map<String, Map<String, String>> aliasMaps;
    private Map<String, Map<String, String>> pageMaps;
    
    public SiteMap(Resource siteMapFile) throws IOException, SAXException, XMLException {
       
        URI uri = siteMapFile.toURI();
        String uriStr = uri.toString();
        if(uriStr.endsWith("depend.xml")) uriStr = uriStr.substring(0, uriStr.length() -10) + "sitemap.xml";
        siteMapFile = ResourceUtil.getResource(uriStr);
        
        loadTime = System.currentTimeMillis();
        Document navitree = Xml.parseMutable(siteMapFile);
        
        IncludesResolver iresolver = new IncludesResolver(null, "config-include");
        // Make sure list of dependencies only contains the file itself
        fileDependencies.clear();
        fileDependencies.add(siteMapFile);
        FileIncludeEventListener listener = new FileIncludeEventListener() {

            public void fileIncluded(FileIncludeEvent event) {
                fileDependencies.add(event.getIncludedFile());
            }

        };
        iresolver.registerListener(listener);
        iresolver.resolveIncludes(navitree);
        
        TransformerFactory tf = TransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            TransformerHandler th;
            try {
                th = stf.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
               throw new XMLException("Error reading sitemap", e);
            }
            DOMResult dr = new DOMResult();
            th.setResult(dr);
            DefaultHandler dh = new TransformerHandlerAdapter(th);
            DefaultHandler ch = new CustomizationHandler(dh);
            XMLReader xreader = XMLReaderFactory.createXMLReader();
            xreader.setContentHandler(ch);
            xreader.setDTDHandler(ch);
            xreader.setEntityResolver(ch);
            xreader.setErrorHandler(ch);
            xreader.parse(new InputSource(new StringReader(Xml.serialize(navitree,false, true))));
            navitree = dr.getNode().getOwnerDocument();
            if (navitree == null) {
                if (dr.getNode() instanceof Document) {
                    navitree = (Document) dr.getNode();
                } else {
                    throw new RuntimeException("XML result is not a Document though it should be");
                }
            }
        } else {
            throw new RuntimeException("TransformerFactory instance does not provide SAXTransformerFactory!");
        }
        
        
        siteMapDoc = navitree;
        
       
        
        readSiteMap(siteMapDoc.getDocumentElement());
        
       
        
        aliasMaps = new HashMap<String, Map<String, String>>();
        pageMaps = new HashMap<String, Map<String, String>>();
        
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
            }
        }
        
    }
   
    
    private void readSiteMap(Element siteMapElem) {
        pageList = new ArrayList<Page>();
        List<Element> pageElems = DOMUtils.getChildElementsByTagName(siteMapElem, "page");
        for(Element pageElem: pageElems) {
            Page page = readPage(pageElem);
            pageList.add(page);
        }
    }
    
    private Page readPage(Element pageElem) {
        String name = pageElem.getAttribute("name").trim();
        Page page = new Page(name);
        List<Element> childAlts = DOMUtils.getChildElementsByTagName(pageElem, "alt");
        for(Element childAlt: childAlts) {
            String altKey = childAlt.getAttribute("key");
            String altName = childAlt.getAttribute("name");
            page.pageAlts.add(new PageAlt(altKey, altName));
        }
        List<Element> childPages = DOMUtils.getChildElementsByTagName(pageElem, "page");
        for(Element childPage: childPages) {
            page.pages.add(readPage(childPage));
        }
        return page;
    }
    
    private void readSiteMapAliases(Document siteMapAliasesDoc) {
        Element root = siteMapAliasesDoc.getDocumentElement();
        String lang = root.getAttribute("lang").trim();
        Map<String, String> pageToAlias = new HashMap<String, String>();
        aliasMaps.put(lang, pageToAlias);
        Map<String, String> aliasToPage = new HashMap<String, String>();
        pageMaps.put(lang, aliasToPage);
        List<Element> aliasElems = DOMUtils.getChildElementsByTagName(root, "alias");
        for(Element aliasElem: aliasElems) {
            String page = aliasElem.getAttribute("page").trim();
            String alias = aliasElem.getTextContent().trim();
            pageToAlias.put(page, alias);
            aliasToPage.put(alias, page);
        }
    }
    
    private Document getDocument(String lang) {
        Document doc = Xml.createDocument();
        Element root = doc.createElement("sitemap");
        doc.appendChild(root);
        for(Page page: pageList) {
            addPage(page, root, lang);
        }
        return doc;
    }
    
    private void addPage(Page page, Element parent, String lang) {
        Element elem = parent.getOwnerDocument().createElement("page");
        elem.setAttribute("name", getAlias(page.name, lang));
        parent.appendChild(elem);
        for(Page child: page.pages) {
            addPage(child, elem, lang);
        }
        for(PageAlt child: page.pageAlts) {
            Element altElem = (Element)elem.cloneNode(true);
            altElem.setAttribute("name", getAlias(child.name, lang));
            parent.appendChild(altElem);
        }
    }
    
    public String getAlias(String name, String lang) {
        if(lang != null) {
            String alias = null;
            Map<String, String> aliases = aliasMaps.get(lang);
            if(aliases != null) {
                alias = aliases.get(name);
            }
            if(alias == null) {
                int ind = lang.indexOf('_');
                if(ind > -1) {
                    lang = lang.substring(0, ind);
                    aliases = aliasMaps.get(lang);
                    if(aliases != null) {
                        alias = aliases.get(name);
                    }
                }
            }
            if(alias != null) {
                name = alias;
            }
        }
        return name;
    }
    
    public String getPageName(String alias, String lang) {
        String page = null;
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
            if(page == null) {
                if(pageList.contains(alias)) {
                    page = alias;
                }
            }
        }
        return page;
    }
    
    public Element getSiteMapXMLElement(XsltVersion xsltVersion) {
        if(siteMapXMLElement == null) {
            // We need a Saxon node here
            siteMapXMLElement = Xml.parse(xsltVersion, siteMapDoc).getDocumentElement();
        }
        return siteMapXMLElement;
    }
    
    
    class Page {
        
        String name;
        List<Page> pages = new ArrayList<Page>();
        List<PageAlt> pageAlts = new ArrayList<PageAlt>();
    
        Page(String name) {
            this.name = name;
        }
        
    }
    
    class PageAlt {
        
        String key;
        String name;
        
        PageAlt(String key, String name) {
            this.key = key;
            this.name = name;
        }
        
    }

}
