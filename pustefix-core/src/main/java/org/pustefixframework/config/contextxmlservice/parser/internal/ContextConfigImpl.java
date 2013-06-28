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

package org.pustefixframework.config.contextxmlservice.parser.internal;

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

import net.sf.cglib.proxy.Enhancer;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.ContextResourceConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.auth.RoleProvider;
import de.schlund.pfixcore.auth.RoleProviderImpl;
import de.schlund.pfixcore.auth.conditions.ConditionGroup;
import de.schlund.pfixcore.auth.conditions.HasRole;
import de.schlund.pfixcore.auth.conditions.NavigationCase;
import de.schlund.pfixcore.auth.conditions.Not;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixxml.Variant;

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
    private String defaultStateParentBeanName;
    private State defaultState = null;
    
    private String defaultPage = null;
    private Map<String,String> variantToDefaultPage = new HashMap<String,String>();
    private Map<String,ContextResourceConfigImpl> resourceMap = new HashMap<String,ContextResourceConfigImpl>();
    private LinkedHashMap<Class<?>, ContextResourceConfigImpl> resources = new LinkedHashMap<Class<?>, ContextResourceConfigImpl>();
    private List<ContextResourceConfigImpl> cacheResources = null; 
    private Map<String, PageFlow> pageflows = new HashMap<String, PageFlow>();
    private HashMap<String, PageRequestConfigImpl> pagerequests = new HashMap<String, PageRequestConfigImpl>();
    private List<PageRequestConfigImpl> cachePagerequests = null;
    private ArrayList<ContextInterceptor> startinterceptors = new ArrayList<ContextInterceptor>();
    private ArrayList<ContextInterceptor> endinterceptors = new ArrayList<ContextInterceptor>();
    private ArrayList<ContextInterceptor> postRenderInterceptors = new ArrayList<ContextInterceptor>();
    private Properties props = new Properties();
    private boolean synchronize = true;
    private Map<String,AuthConstraint> authConstraints = new HashMap<String,AuthConstraint>();
    private AuthConstraint defaultAuthConstraint;
    private Map<String,Condition> conditions = new HashMap<String,Condition>();
    private RoleProvider roleProvider = new RoleProviderImpl();
    private boolean authConstraintRefsResolved = false;
    private List<String> startInterceptorBeans = new ArrayList<String>();
    private List<String> endInterceptorBeans = new ArrayList<String>();
    private List<String> postRenderInterceptorBeans = new ArrayList<String>();
    
    public ContextConfigImpl() {
        // Default constructor
    }
    
    /**
     * Copy constructor. Does not perform deep copy!
     * 
     * @param ref reference to copy settings from
     */
    public ContextConfigImpl(ContextConfigImpl ref) {
        this.authConstraintRefsResolved = ref.authConstraintRefsResolved;
        this.authConstraints = ref.authConstraints;
        this.cachePagerequests = ref.cachePagerequests;
        this.cacheResources = ref.cacheResources;
        this.conditions = ref.conditions;
        this.defaultAuthConstraint = ref.defaultAuthConstraint;
        this.defaultPage = ref.defaultPage;
        this.defaultState = ref.defaultState;
        this.defaultStateParentBeanName = ref.defaultStateParentBeanName;
        this.defaultStateClass = ref.defaultStateClass;
        this.endInterceptorBeans = ref.endInterceptorBeans;
        this.endinterceptors = ref.endinterceptors;
        this.pageflows = ref.pageflows;
        this.pagerequests = ref.pagerequests;
        this.postRenderInterceptorBeans = ref.postRenderInterceptorBeans;
        this.postRenderInterceptors = ref.postRenderInterceptors;
        this.props = ref.props;
        this.resourceMap = ref.resourceMap;
        this.resources = ref.resources;
        this.roleProvider = ref.roleProvider;
        this.startInterceptorBeans = ref.startInterceptorBeans;
        this.startinterceptors = ref.startinterceptors;
        this.synchronize = ref.synchronize;
        this.variantToDefaultPage = ref.variantToDefaultPage;
    }
    
    public void setDefaultPage(String page) {
        this.defaultPage = page;
    }
    
    public void setDefaultPage(String variantName, String page) {
        variantToDefaultPage.put(variantName, page);
    }
    
    public String getDefaultPage(Variant variant) {
        String page = null;
        if(variant != null) {
            page = variantToDefaultPage.get(variant.getVariantId());
            for(String variantId : variant.getVariantFallbackArray()) {
              page = variantToDefaultPage.get(variantId);
              if(page != null) break;
            }
        }
        if(page == null) page = defaultPage;
        return page;
    }
    
    public void setDefaultStateType(Class<? extends State> clazz) {
        this.defaultStateClass = clazz;
    }

    public Class<? extends State> getDefaultStateType() {
        return this.defaultStateClass;
    }
    
    public void setDefaultStateParentBeanName(String defaultStateParentBeanName) {
        this.defaultStateParentBeanName = defaultStateParentBeanName;
    }

    public String getDefaultStateParentBeanName() {
        return defaultStateParentBeanName;
    }

    public State getDefaultState() {
        return defaultState;
    }
        
    public void setDefaultState(State defaultState) {
        this.defaultState = defaultState;
    }
    
    public void addContextResource(ContextResourceConfigImpl config) {
        if (resources.containsKey(config.getContextResourceClass())) {
            LOG.warn("Overwriting configuration for context resource " + config.getContextResourceClass().getName());
        }
        resources.put(config.getContextResourceClass(), config);
        for(Class<?> itf:config.getInterfaces()) {
            if (resourceMap.containsKey(itf.getName())) {
                LOG.warn("Overwriting implementation for interface " + itf.getName());
            }
            resourceMap.put(itf.getName(), config);
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
        if(Enhancer.isEnhanced(clazz)) {
            clazz = clazz.getSuperclass();
        }
        return getContextResourceConfig(clazz.getName());
    }
    
    public ContextResourceConfig getContextResourceConfig(String name) {
        return resourceMap.get(name);
    }
    
    public List<PageFlow> getPageFlows() {
        return new ArrayList<PageFlow>(this.pageflows.values());
    }
    
    public void setPageFlowMap(Map<String, PageFlow> map) {
        this.pageflows = map;
    }
    
    public PageFlow getPageFlow(String name) {
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
    
    public void addStartInterceptorBean(String beanName) {
        this.startInterceptorBeans.add(beanName);
    }
    
    public List<String> getStartInterceptorBeans() {
        return this.startInterceptorBeans;
    }
    
    public void setStartInterceptors(List<? extends ContextInterceptor> interceptors) {
        this.startinterceptors.clear();
        this.startinterceptors.addAll(interceptors);
    }
    
    public List<? extends ContextInterceptor> getStartInterceptors() {
        return Collections.unmodifiableList(startinterceptors);
    }
    
    public void addEndInterceptorBean(String beanName) {
        this.endInterceptorBeans.add(beanName);
    }
    
    public List<String> getEndInterceptorBeans() {
        return this.endInterceptorBeans;
    }
    
    public void setEndInterceptors(List<? extends ContextInterceptor> interceptors) {
        this.endinterceptors.clear();
        this.endinterceptors.addAll(interceptors);
    }
    
    public List<? extends ContextInterceptor> getEndInterceptors() {
        return Collections.unmodifiableList(endinterceptors);
    }
    
    public void addPostRenderInterceptorBean(String beanName) {
        this.postRenderInterceptorBeans.add(beanName);
    }
    
    public List<String> getPostRenderInterceptorBeans() {
        return this.postRenderInterceptorBeans;
    }
    
    public void setPostRenderInterceptors(List<? extends ContextInterceptor> interceptors) {
        this.postRenderInterceptors.clear();
        this.postRenderInterceptors.addAll(interceptors);
    }
    
    public List<? extends ContextInterceptor> getPostRenderInterceptors() {
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
            
            for(NavigationCase naviCase: condAuth.getNavigation()) {
                cond = naviCase.getCondition();
                if(cond != null) {
                    refList.add(condAuth.getId());
                    resCond = resolveAuthConstraintRefs(cond,refList);
                    refList.remove(condAuth.getId());
                    if(cond!=resCond) naviCase.setCondition(resCond);
                }
            }
        }
        return condition;
    }
      
    public void checkAuthConstraints() throws Exception {
        Set<String> authPages = new LinkedHashSet<String>();
        List<PageRequestConfigImpl> pages = getPageRequestConfigs();
        for (PageRequestConfigImpl page : pages) {
            AuthConstraint authConstraint = page.getAuthConstraint();
            if (authConstraint == null) authConstraint = getDefaultAuthConstraint();
            if (authConstraint != null) {
                authPages.clear();
                authPages.add(page.getPageName());
                checkAuthConstraint(authConstraint, authPages, page.getPageName());
            }
        }
    }
    
    private void checkAuthConstraint(AuthConstraint authConstraint, Set<String> authPages, String lastAuthPage) throws Exception {
        for (String authPage : traverseAuthPages(authConstraint)) {
            if (authPage != null && !authPage.equals(lastAuthPage)) {
                if (authPages.contains(authPage)) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : authPages)
                        sb.append(s + " -> ");
                    sb.append(authPage);
                    throw new Exception("Circular authconstraint@authpage reference: " + sb.toString());
                }
                PageRequestConfigImpl cfg = getPageRequestConfig(authPage);
                if (cfg != null) {
                    AuthConstraint ac = cfg.getAuthConstraint();
                    if (ac == null) ac = getDefaultAuthConstraint();
                    if (ac != null) {
                        authPages.add(authPage);
                        checkAuthConstraint(ac, authPages, authPage);
                    }
                } else throw new Exception("Authpage not configured: " + authPage);
            }
        }
    }

    private Iterable<String> traverseAuthPages(final AuthConstraint authConstraint) {
        return new Iterable<String>() {

            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    private Iterator<NavigationCase> navCases =
                        authConstraint.getNavigation().iterator();

                    private boolean visitedDefaultAuthPage;

                    private boolean isDefaultPageVisitable() {
                        return !visitedDefaultAuthPage && authConstraint.getDefaultAuthPage() != null;
                    }

                    public boolean hasNext() {
                        return isDefaultPageVisitable() ||
                               navCases.hasNext();
                    }

                    public String next() {
                        if (isDefaultPageVisitable()) {
                            String result = authConstraint.getDefaultAuthPage();
                            visitedDefaultAuthPage = true;
                            return result;
                        }
                        return navCases.next().getPage();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException(); 
                    }
                };
            }
        };
    }

}
