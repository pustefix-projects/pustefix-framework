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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.auth.RoleProvider;
import de.schlund.pfixcore.auth.RoleProviderImpl;
import de.schlund.pfixcore.auth.conditions.ConditionGroup;
import de.schlund.pfixcore.auth.conditions.HasRole;
import de.schlund.pfixcore.auth.conditions.Not;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.ContextResourceConfig;
import de.schlund.pfixxml.config.PageFlowConfig;

/**
 * Stores configuration for a Context
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextConfigImpl implements ContextConfig {
    // Caution: This implementation is not thread-safe. However, it
    // does not have to be as a configuration is initialized only once.
    
    private final static Logger LOG = Logger.getLogger(ContextConfigImpl.class);
    private Class<? extends State> defaultStateClass = null;
    
    private String defaultPage = null;
    
    private Map<String,ContextResourceConfigImpl> resourceMap = new HashMap<String,ContextResourceConfigImpl>();
    private LinkedHashMap<Class<?>, ContextResourceConfigImpl> resources = new LinkedHashMap<Class<?>, ContextResourceConfigImpl>();
    private List<ContextResourceConfigImpl> cacheResources = null; 
    private HashMap<String, PageFlowConfigImpl> pageflows = new HashMap<String, PageFlowConfigImpl>();
    private List<PageFlowConfigImpl> cachePageflows = null;
    private HashMap<String, PageRequestConfigImpl> pagerequests = new HashMap<String, PageRequestConfigImpl>();
    private List<PageRequestConfigImpl> cachePagerequests = null;
    private ArrayList<Class<? extends ContextInterceptor>> startinterceptors = new ArrayList<Class<? extends ContextInterceptor>>();
    private ArrayList<Class<? extends ContextInterceptor>> endinterceptors = new ArrayList<Class<? extends ContextInterceptor>>();
    private ArrayList<Class<? extends ContextInterceptor>> postRenderInterceptors = new ArrayList<Class<? extends ContextInterceptor>>();
    private String navigationFile = null;
    private Properties props = new Properties();
    private boolean synchronize = true;
    private Map<String,AuthConstraint> authConstraints = new HashMap<String,AuthConstraint>();
    private AuthConstraint defaultAuthConstraint;
    private Map<String,Condition> conditions = new HashMap<String,Condition>();
    private RoleProvider roleProvider = new RoleProviderImpl();
    private boolean doLoadTimeChecks = false;
    private boolean authConstraintRefsResolved = false;
    
    
    public void setDefaultPage(String page) {
        this.defaultPage = page;
    }
    
    public String getDefaultPage() {
        return this.defaultPage;
    }
    
    public void setDefaultState(Class<? extends State> clazz) {
        this.defaultStateClass = clazz;
    }

    public Class<? extends State> getDefaultState() {
        return this.defaultStateClass;
    }

    public void addContextResource(ContextResourceConfigImpl config) {
        if (resources.containsKey(config.getContextResourceClass())) {
            LOG.warn("Overwriting configuration for context resource " + config.getContextResourceClass().getName());
        }
        resources.put(config.getContextResourceClass(), config);
        resourceMap.put(config.getContextResourceClass().getName(), config);
        for(Class<?> itf:config.getInterfaces()) {
            resourceMap.put(itf.getClass().getName(), config);
        }
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
    
    public ContextResourceConfig getContextResourceConfig(String name) {
        return resourceMap.get(name);
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
    
    public void addPostRenderInterceptor(Class<? extends ContextInterceptor> clazz) {
        if (this.postRenderInterceptors.contains(clazz)) {
            LOG.warn("Postrender interceptor " + clazz.getName() + " not added - it is already present");
        } else {
            this.postRenderInterceptors.add(clazz);
        }
    }
    
    public List<Class<? extends ContextInterceptor>> getPostRenderInterceptors() {
        return Collections.unmodifiableList(postRenderInterceptors);
    }
    
    public RoleProvider getRoleProvider() {
        return roleProvider;
    }
    
    public void setCustomRoleProvider(RoleProvider customProvider) {
        roleProvider = customProvider;
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
    
    public Condition getCondition(String id) {
        return conditions.get(id);
    }
    
    public void addCondition(String id, Condition condition) {
        conditions.put(id,condition);
    }
    
    public Element getAuthConstraintAsXML(Document doc, AuthConstraint authConstraint) {
        Element element = doc.createElement("authconstraint");
        Condition condition = authConstraint.getCondition();
        if (condition != null) {
            element.appendChild(getConditionAsXML(doc, condition));
        }
        return element;
    }
    
    private String getConditionId(Condition condition) {
        Iterator<Entry<String,Condition>> entries = conditions.entrySet().iterator();
        while(entries.hasNext()) {
            Entry<String,Condition> entry = entries.next();
            if(entry.getValue()==condition) return entry.getKey();
        }
        return null;
    }
    
    private Element getConditionAsXML(Document doc, Condition condition) {
        Element result = null;
        if (ConditionGroup.class.isAssignableFrom(condition.getClass())) {
            ConditionGroup group = (ConditionGroup) condition;
            result = doc.createElement(condition.getClass().getSimpleName().toLowerCase());
            if (group.getConditions() != null) {
                for (Condition subCond : group.getConditions()) {
                    result.appendChild(getConditionAsXML(doc, subCond));
                }
            }
        } else if (HasRole.class.isAssignableFrom(condition.getClass())) {
            HasRole hasRole = (HasRole) condition;
            result = doc.createElement("hasrole");
            result.setAttribute("name", hasRole.getRoleName());
        } else if (Not.class.isAssignableFrom(condition.getClass())) {
            result = doc.createElement("not");
            Condition subCond = ((Not) condition).get();
            if (subCond != null) result.appendChild(getConditionAsXML(doc, subCond));
        } else {
            String condId = getConditionId(condition);
            result = doc.createElement("condition");
            result.setAttribute("ref", condId);
        }
        return result;
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
        if(!authConstraintRefsResolved) resolveAuthConstraintRefs();
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
    
    public boolean authConstraintRefsResolved() {
        return authConstraintRefsResolved;
    }
    
    public void resolveAuthConstraintRefs() {
        LinkedHashSet<String> refList = new LinkedHashSet<String>();
        for(AuthConstraint constraint:authConstraints.values()) {
            refList.clear();
            resolveAuthConstraintRefs(constraint, refList);
        }
        authConstraintRefsResolved = true;
    }
    
    private Condition resolveAuthConstraintRefs(Condition condition, LinkedHashSet<String> refList) {
        if(condition instanceof AuthConstraintRef) {
            AuthConstraintRef ref = (AuthConstraintRef)condition;
            if(refList.contains(ref.getRef())) {
                StringBuilder sb = new StringBuilder();
                for(String str:refList) {
                    if(str.equals(ref.getRef())) sb.append("( ");
                    sb.append(str+" -> ");
                }
                sb.append(ref.getRef()+" )");
                throw new RuntimeException("Circular authconstraint reference: "+sb.toString());
            }
            AuthConstraint constraint = getAuthConstraint(ref.getRef());
            if(constraint == null) throw new RuntimeException("Referenced authconstraint not found: "+ref.getRef());
            Condition constraintCond = constraint.getCondition();
            if(constraintCond == null) throw new RuntimeException("Referenced authconstraint has no condition: "+ref.getRef());
            refList.add(ref.getRef());
            condition = resolveAuthConstraintRefs(constraintCond, refList);
            refList.remove(ref.getRef());
        } else if(condition instanceof ConditionGroup) {
            ConditionGroup condGroup = (ConditionGroup)condition;
            List<Condition> conds = condGroup.getConditions();
            for(Condition cond:conds) {
                Condition resCond = resolveAuthConstraintRefs(cond,refList);
                if(cond!=resCond) {
                    int ind=conds.indexOf(cond);
                    if(ind == -1) throw new RuntimeException("Condition can't be found: "+cond);
                    conds.set(ind, resCond);
                }
            }
        } else if(condition instanceof Not) {
            Not condNot = (Not)condition;
            Condition cond = condNot.get();
            Condition resCond = resolveAuthConstraintRefs(cond,refList);
            if(cond!=resCond) condNot.set(resCond);
        } else if(condition instanceof AuthConstraint) {
            AuthConstraint condAuth = (AuthConstraint)condition;
            Condition cond = condAuth.getCondition();
            refList.add(condAuth.getId());
            Condition resCond = resolveAuthConstraintRefs(cond,refList);
            refList.remove(condAuth.getId());
            if(cond!=resCond) condAuth.setCondition(resCond);
        }
        return condition;
    }
      
    private void checkAuthConstraints() throws SAXException {
        Set<String> authPages = new LinkedHashSet<String>();
        List<PageRequestConfigImpl> pages = getPageRequestConfigs();
        for (PageRequestConfigImpl page : pages) {
            AuthConstraint authConstraint = page.getAuthConstraint();
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
