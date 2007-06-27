/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.config.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RulesBase;
import org.apache.commons.digester.WithDefaultsRulesWrapper;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.DirectOutputServletConfig;
import de.schlund.pfixxml.config.includes.FileIncludeEvent;
import de.schlund.pfixxml.config.includes.FileIncludeEventListener;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.util.Xml;

/**
 * Stores configuration for a Pustefix servlet
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DirectOutputServletConfigImpl extends ServletManagerConfigImpl implements
        SSLOption, DirectOutputServletConfig {
    private final static String CONFIG_NS = "http://pustefix.sourceforge.net/properties200401";

    private final static String CUS_NS = "http://www.schlund.de/pustefix/customize";
    
    private final static Logger LOG = Logger.getLogger(DirectOutputServletConfigImpl.class);

    private String servletName = null;

    private boolean editMode = false;

    private String externalName;
    
    private boolean sync = true;

    private HashMap<String, DirectOutputPageRequestConfigImpl> pages = new HashMap<String, DirectOutputPageRequestConfigImpl>();
    
    private List<DirectOutputPageRequestConfigImpl> cachePages = null;
    
    private Set<FileResource> fileDependencies = new HashSet<FileResource>();

    private long loadTime = 0;

    public static DirectOutputServletConfigImpl readFromFile(FileResource file,
            Properties globalProperties) throws SAXException, IOException {
        final DirectOutputServletConfigImpl config = new DirectOutputServletConfigImpl();

        // Initialize configuration properties with global default properties
        config.setProperties(globalProperties);

        Digester digester = new Digester();
        WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper(
                new RulesBase());
        digester.setRules(rules);
        rules.addDefault(new DefaultMatchRule());
        digester.setRuleNamespaceURI(CONFIG_NS);

        Rule servletInfoRule = new DirectOutputServletInfoRule(config);
        Rule sslRule = new SSLRule();
        Rule foreignContextRule = new DirectForeignContextRule(config);
        Rule pagerequestRule = new DirectPagerequestRule(config);
        Rule pagerequestStateRule = new DirectPagerequestStateRule(config);
        Rule pagerequestPropertyRule = new DirectPagerequestPropertyRule(config);
        Rule servletPropertyRule = new ServletPropertyRule(config);
        // Dummy rule doing nothing
        Rule dummyRule = new Rule() {
        };

        digester.addRule("directoutputserver", dummyRule);
        digester.addRule("directoutputserver/directoutputservletinfo",
                servletInfoRule);
        // Still allowed for compatibility reasons
        digester.addRule("directoutputserver/directoutputservletinfo/editmode",
                dummyRule);
        digester.addRule("directoutputserver/directoutputservletinfo/ssl",
                sslRule);
        digester.addRule("directoutputserver/foreigncontext",
                foreignContextRule);
        digester.addRule("directoutputserver/directoutputpagerequest",
                pagerequestRule);
        digester.addRule(
                "directoutputserver/directoutputpagerequest/directoutputstate",
                pagerequestStateRule);
        digester.addRule(
                "directoutputserver/directoutputpagerequest/properties",
                dummyRule);
        digester.addRule(
                "directoutputserver/directoutputpagerequest/properties/prop",
                pagerequestPropertyRule);
        digester.addRule("directoutputserver/properties", dummyRule);
        digester.addRule("directoutputserver/properties/prop",
                servletPropertyRule);

        CustomizationHandler cushandler = new CustomizationHandler(
                digester,
                CONFIG_NS,
                CUS_NS,
                new String[] { 
                        "/directoutputserver/directoutputservletinfo",
                        "/directoutputserver/directoutputpagerequest/properties",
                        "/directoutputserver/properties" });
        
        String confDocXml = null;
        config.loadTime = System.currentTimeMillis();

        Document confDoc = Xml.parseMutable(file);
        IncludesResolver iresolver = new IncludesResolver(CONFIG_NS, "config-include");
        // Make sure list of dependencies only contains the file itself
        config.fileDependencies.clear();
        config.fileDependencies.add(file);
        FileIncludeEventListener listener = new FileIncludeEventListener() {

            public void fileIncluded(FileIncludeEvent event) {
                config.fileDependencies.add(event.getIncludedFile());
            }

        };
        iresolver.registerListener(listener);
        iresolver.resolveIncludes(confDoc);
        confDocXml = Xml.serialize(confDoc, false, true);
        
        SAXParser parser;
        try {
            SAXParserFactory spfac = SAXParserFactory.newInstance();
            spfac.setNamespaceAware(true);
            parser = spfac.newSAXParser();
            parser.parse(new InputSource(new StringReader(confDocXml)), cushandler);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not initialize SAXParser!");
        }

        return config;
    }

    public void setServletName(String name) {
        this.servletName = name;
    }

    public String getServletName() {
        return this.servletName;
    }

    public void setEditMode(boolean enabled) {
        this.editMode = enabled;
    }

    public boolean isEditMode() {
        return this.editMode;
    }

    public void setExternalServletName(String externalName) {
        this.externalName = externalName;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getExternalServletName()
     */
    public String getExternalServletName() {
        return this.externalName;
    }
    
    public void setSynchronized(boolean sync) {
        this.sync = sync;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#isSynchronized()
     */
    public boolean isSynchronized() {
        return sync;
    }

    public void addPageRequest(DirectOutputPageRequestConfigImpl config) {
        if (this.pages.containsKey(config.getPageName())) {
            LOG.warn("Overwriting configuration for direct output pagerequest" + config.getPageName());
        }
        this.pages.put(config.getPageName(), config);
        this.cachePages = null;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getPageRequests()
     */
    public List<DirectOutputPageRequestConfigImpl> getPageRequests() {
        List<DirectOutputPageRequestConfigImpl> list = this.cachePages;
        if (list == null) {
            list = new ArrayList<DirectOutputPageRequestConfigImpl>();
            for (Iterator i = this.pages.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                list.add((DirectOutputPageRequestConfigImpl) entry.getValue());
            }
            list = Collections.unmodifiableList(list);
            this.cachePages = list;
        }
        return list;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getPageRequest(java.lang.String)
     */
    public DirectOutputPageRequestConfigImpl getPageRequest(String page) {
        return this.pages.get(page);
    }
    
    public boolean needsReload() {
        for (FileResource file : fileDependencies) {
            if (file.lastModified() > loadTime) {
                return true;
            }
        }
        return false;
    }
}
