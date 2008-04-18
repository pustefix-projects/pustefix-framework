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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.auth.RoleProvider;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.ContextResourceConfig;
import de.schlund.pfixxml.config.PageFlowConfig;

/**
 * Stores configuration for a Context
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextConfigImpl implements ContextConfig, RoleProvider {
    // Caution: This implementation is not thread-safe. However, it
    // does not have to be as a configuration is initialized only once.
    
    private final static Logger LOG = Logger.getLogger(ContextConfigImpl.class);
    
    private String authPage = null;
    private String defaultFlow = null;
    private LinkedHashMap<Class<?>, ContextResourceConfigImpl> resources = new LinkedHashMap<Class<?>, ContextResourceConfigImpl>();
    private List<ContextResourceConfigImpl> cacheResources = null;
    protected HashMap<Class<?>, ContextResourceConfigImpl> interfaceToResource = new HashMap<Class<?>, ContextResourceConfigImpl>(); 
    private HashMap<String, PageFlowConfigImpl> pageflows = new HashMap<String, PageFlowConfigImpl>();
    private List<PageFlowConfigImpl> cachePageflows = null;
    private HashMap<String, PageRequestConfigImpl> pagerequests = new HashMap<String, PageRequestConfigImpl>();
    private List<PageRequestConfigImpl> cachePagerequests = null;
    private ArrayList<Class<? extends ContextInterceptor>> startinterceptors = new ArrayList<Class<? extends ContextInterceptor>>();
    private ArrayList<Class<? extends ContextInterceptor>> endinterceptors = new ArrayList<Class<? extends ContextInterceptor>>();
    private String navigationFile = null;
    private Properties props = new Properties();
    private boolean synchronize = true;
    private Map<String,Role> roles = new HashMap<String,Role>();
    private List<Role> initialRoles = new ArrayList<Role>();
    private Map<String,AuthConstraint> authConstraints = new HashMap<String,AuthConstraint>();
    private AuthConstraint defaultAuthConstraint;
    private boolean doLoadTimeChecks = false;
    
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
    
    public void addContextResource(ContextResourceConfigImpl config) {
        if (resources.containsKey(config.getContextResourceClass())) {
            LOG.warn("Overwriting configuration for context resource " + config.getContextResourceClass().getName());
        }
        resources.put(config.getContextResourceClass(), config);
        cacheResources = null;
    }
    
    public List<ContextResourceConfigImpl> getContextResourceConfigs() {
        List<ContextResourceConfigImpl> list = cacheResources;
        if (list == null) {
            list = new ArrayList<ContextResourceConfigImpl>();
            for (Entry<Class<?>, ContextResourceConfigImpl> e : this.resources.entrySet()) {
                list.add(e.getValue());
            }
            cacheResources = Collections.unmodifiableList(list);
        }
        return list;
    }
    
    public ContextResourceConfig getContextResourceConfig(Class<?> clazz) {
        return this.resources.get(clazz);
    }
    
    public ContextResourceConfig getContextResourceConfigForInterface(Class<?> clazz) {
        return interfaceToResource.get(clazz);
    }
    
    public Map<Class<?>, ContextResourceConfigImpl> getInterfaceToContextResourceMap() {
        return Collections.unmodifiableMap(interfaceToResource);
    }
    
    public void addPageFlow(PageFlowConfigImpl config) {
        if (this.pageflows.containsKey(config.getFlowName())) {
            LOG.warn("Overwriting configuration for pageflow " + config.getFlowName());
        }
        this.pageflows.put(config.getFlowName(), config);
        this.cachePageflows = null;
    }
    
    public List<PageFlowConfigImpl> getPageFlowConfigs() {
        List<PageFlowConfigImpl> list = this.cachePageflows;
        if (list == null) {
            list = new ArrayList<PageFlowConfigImpl>();
            for (Entry<String, PageFlowConfigImpl> entry : this.pageflows.entrySet()) {
                list.add(entry.getValue());
            }
            this.cachePageflows = Collections.unmodifiableList(list);
        }
        return list;
    }
    
    public PageFlowConfig getPageFlowConfig(String name) {
        return this.pageflows.get(name);
    }
    
    public void addPageRequest(PageRequestConfigImpl config) {
        if (this.pagerequests.containsKey(config.getPageName())) {
            LOG.warn("Overwriting configuration for pagerequest" + config.getPageName());
        }
        this.pagerequests.put(config.getPageName(), config);
        this.cachePagerequests = null;
    }
    
    public List<PageRequestConfigImpl> getPageRequestConfigs() {
        List<PageRequestConfigImpl> list = this.cachePagerequests;
        if (list == null) {
            list = new ArrayList<PageRequestConfigImpl>();
            for (Iterator<Entry<String, PageRequestConfigImpl>> i = this.pagerequests.entrySet().iterator(); i.hasNext();) {
                Entry<String, PageRequestConfigImpl> entry = i.next();
                list.add(entry.getValue());
            
            }
            this.cachePagerequests = Collections.unmodifiableList(list);
        }
        return list;
    }
    
    public PageRequestConfigImpl getPageRequestConfig(String name) {
        return this.pagerequests.get(name);
    }
    
    public void addStartInterceptor(Class<? extends ContextInterceptor> clazz) {
        if (this.startinterceptors.contains(clazz)) {
            LOG.warn("Context interceptor " + clazz.getName() + " not added - it is already present");
        } else {
            this.startinterceptors.add(clazz);
        }
    }
    
    public List<Class<? extends ContextInterceptor>> getStartInterceptors() {
        return Collections.unmodifiableList(startinterceptors);
    }
    
    public List<Class<? extends ContextInterceptor>> getEndInterceptors() {
        return Collections.unmodifiableList(endinterceptors);
    }
    
    public void addEndInterceptor(Class<? extends ContextInterceptor> clazz) {
        if (this.endinterceptors.contains(clazz)) {
            LOG.warn("Context interceptor " + clazz.getName() + " not added - it is already present");
        } else {
            this.endinterceptors.add(clazz);
        }       
    }
    
    public void addRole(Role role) {
        roles.put(role.getName(),role);
        if(role.isInitial()) initialRoles.add(role);
    }
    
    public Role getRole(String roleName) {
        return roles.get(roleName);
    }
    
    public Map<String,Role> getRoles() {
        return Collections.unmodifiableMap(roles);
    }
    
    public List<Role> getInitialRoles() {
    	return initialRoles;
    }
    
    public RoleProvider getRoleProvider() {
    	return this;
    }
    
    public boolean hasRoles() {
        return !roles.isEmpty();
    }
    
    public void addAuthConstraint(String id,AuthConstraint authConstraint) {
    	authConstraints.put(id,authConstraint);
    }
    
    public AuthConstraint getAuthConstraint(String id) {
    	return authConstraints.get(id);
    }
    
    public void setDefaultAuthConstraint(AuthConstraint authConstraint) {
       defaultAuthConstraint=authConstraint;
    }
    
    public AuthConstraint getDefaultAuthConstraint() {
       return defaultAuthConstraint;
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
    
    public void setSynchronized(boolean sync) {
        this.synchronize = sync;
    }
    
    public boolean isSynchronized() {
        return this.synchronize;
    }

    public void doFinishing() throws SAXException {
        // Handle page copies
        HashMap<String, PageRequestConfigImpl> newPages = new HashMap<String, PageRequestConfigImpl>();
        
        for (String pagename : this.pagerequests.keySet()) {
            PageRequestConfigImpl config = this.pagerequests.get(pagename);
            String copyfrom = config.getCopyFromPage();
            if (copyfrom != null) {
                // Find all variants for source page and copy them
                for (String fullname : this.pagerequests.keySet()) {
                    if (fullname.startsWith(copyfrom + "::")) {
                        String variantname = fullname.substring(fullname.indexOf("::"));
                        newPages.put(pagename + "::" + variantname, copyPage(this.pagerequests.get(fullname), pagename + "::" + variantname));
                    } else if (fullname.equals(copyfrom)) {
                        newPages.put(pagename, copyPage(this.pagerequests.get(fullname), pagename));
                    }
                }
            }
        }
        
        this.pagerequests.putAll(newPages);
        this.cachePagerequests = null;
        
        if(doLoadTimeChecks) checkAuthConstraints();
    }

    private PageRequestConfigImpl copyPage(PageRequestConfigImpl source, String newName) {
        PageRequestConfigImpl newConfig;
        try {
            newConfig = (PageRequestConfigImpl) source.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        newConfig.setPageName(newName);
        
        return newConfig;
    }
    
    private void checkAuthConstraints() throws SAXException {
        Set<String> authPages = new LinkedHashSet<String>();
        List<PageRequestConfigImpl> pages = getPageRequestConfigs();
        for (PageRequestConfigImpl page : pages) {
            AuthConstraint authConstraint = page.getAuthConstraint();
            if (authConstraint != null && getAuthPage() != null) {
                if (getAuthPage().equals(authConstraint.getAuthPage()))
                    throw new SAXException("Authconstraint authpage isn't allowed to " + "be equal to context authpage: " + getAuthPage());
            }
            if (authConstraint == null) authConstraint = getDefaultAuthConstraint();
            if (authConstraint != null) {
                authPages.clear();
                authPages.add(page.getPageName());
                checkAuthConstraint(authConstraint, authPages);
            }
        }
    }
    
    private void checkAuthConstraint(AuthConstraint authConstraint, Set<String> authPages) throws SAXException {
        String authPage = authConstraint.getAuthPage();
        if (authPage != null) {
            if (authPages.contains(authPage)) {
                StringBuilder sb = new StringBuilder();
                for (String s : authPages)
                    sb.append(s + " -> ");
                sb.append(authPage);
                throw new SAXException("Circular authconstraint@authpage reference: " + sb.toString());
            }
            PageRequestConfigImpl cfg = getPageRequestConfig(authPage);
            if (cfg != null) {
                AuthConstraint ac = cfg.getAuthConstraint();
                if (ac == null) ac = getDefaultAuthConstraint();
                if (ac != null) {
                    authPages.add(authPage);
                    checkAuthConstraint(ac, authPages);
                }
            } else throw new SAXException("Authpage not configured: " + authPage);
        }
    }
    
}
