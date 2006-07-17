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

package de.schlund.pfixxml.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Stores configuration for a Context
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextConfig {
    
    private Class contextClass = null;
    private String authPage = null;
    private String defaultFlow = null;
    private LinkedHashMap<Class, ContextResourceConfig> resources = new LinkedHashMap<Class, ContextResourceConfig>();
    private HashMap<String, PageFlowConfig> pageflows = new HashMap<String, PageFlowConfig>();
    private HashMap<String, PageRequestConfig> pagerequests = new HashMap<String, PageRequestConfig>();
    private String navigationFile = null;
    private Properties props = new Properties();

    public void setContextClass(Class clazz) {
        this.contextClass = clazz; 
    }
    
    public Class getContextClass() {
        return this.contextClass;
    }

    public void setAuthPage(String page) {
        this.authPage = page;
    }
    
    public String getAuthPage() {
        return this.authPage;
    }

    public void setDefaultFlow(String flow) {
        this.defaultFlow = flow;
    }
    
    public String getDefaultFlow() {
        return this.defaultFlow;
    }
    
    public void addContextResource(ContextResourceConfig config) {
        resources.put(config.getContextResourceClass(), config);
    }
    
    public Collection<ContextResourceConfig> getContextResourceConfigs() {
        return this.resources.values();
    }
    
    public ContextResourceConfig getContextResourceConfig(Class clazz) {
        return this.resources.get(clazz);
    }
    
    public void addPageFlow(PageFlowConfig config) {
        this.pageflows.put(config.getFlowName(), config);
    }
    
    public PageFlowConfig[] getPageFlowConfigs() {
        ArrayList<PageFlowConfig> list = new ArrayList<PageFlowConfig>();
        for (Iterator i = this.pageflows.entrySet().iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            list.add((PageFlowConfig) entry.getValue());
        }
        return list.toArray(new PageFlowConfig[0]);
    }
    
    public PageFlowConfig getPageFlowConfig(String name) {
        return this.pageflows.get(name);
    }
    
    public void addPageRequest(PageRequestConfig config) {
        this.pagerequests.put(config.getPageName(), config);
    }
    
    public PageRequestConfig[] getPageRequestConfigs() {
        ArrayList<PageRequestConfig> list = new ArrayList<PageRequestConfig>();
        for (Iterator i = this.pagerequests.entrySet().iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            list.add((PageRequestConfig) entry.getValue());
        }
        return list.toArray(new PageRequestConfig[0]);
    }
    
    public PageRequestConfig getPageRequestConfig(String name) {
        return this.pagerequests.get(name);
    }
    
    public void setNavigationFile(String filename) {
        this.navigationFile  = filename;
    }
    
    public String getNavigationFile() {
        return this.navigationFile;
    }

    public void setProperties(Properties properties) {
        this.props = properties;
    }
    
    public Properties getProperties() {
        return this.props;
    }

    public void doFinishing() {
        // Handle page copies
        HashSet<PageRequestConfig> newPages = new HashSet<PageRequestConfig>();
        
        for (Iterator i = this.pagerequests.keySet().iterator(); i.hasNext();) {
            String pagename = (String) i.next();
            PageRequestConfig config = this.pagerequests.get(pagename);
            String copyfrom = config.getCopyFromPage();
            if (copyfrom != null) {
                // Find all variants for source page and copy them
                for (Iterator j = this.pagerequests.keySet().iterator(); j.hasNext();) {
                    String fullname = (String) j.next();
                    if (fullname.startsWith(copyfrom + "::")) {
                        String variantname = fullname.substring(fullname.indexOf("::"));
                        newPages.add(copyPage(this.pagerequests.get(fullname), pagename + "::" + variantname));
                    } else if (fullname.equals(copyfrom)) {
                        newPages.add(copyPage(this.pagerequests.get(fullname), pagename));
                    }
                }
            }
        }
    }

    private PageRequestConfig copyPage(PageRequestConfig source, String newName) {
        PageRequestConfig newConfig;
        try {
            newConfig = (PageRequestConfig) source.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        newConfig.setPageName(newName);
        
        return newConfig;
    }
}
