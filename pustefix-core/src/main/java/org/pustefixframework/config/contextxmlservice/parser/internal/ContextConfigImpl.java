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
import java.util.Map.Entry;

import net.sf.cglib.proxy.Enhancer;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.ContextResourceConfig;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.auth.RoleProvider;
import de.schlund.pfixcore.auth.RoleProviderImpl;
import de.schlund.pfixcore.auth.conditions.ConditionGroup;
import de.schlund.pfixcore.auth.conditions.HasRole;
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
    
    private String defaultPage = null;
    private Map<String,String> variantToDefaultPage = new HashMap<String,String>();
    private Map<String,ContextResourceConfigImpl> resourceMap = new HashMap<String,ContextResourceConfigImpl>();
    private LinkedHashMap<Class<?>, ContextResourceConfigImpl> resources = new LinkedHashMap<Class<?>, ContextResourceConfigImpl>();
    private List<ContextResourceConfigImpl> cacheResources = null; 
    private Map<String, PageFlow> pageflows = new HashMap<String, PageFlow>();
    private Map<String, ? extends PageRequestConfig> pagerequests;
    private ArrayList<ContextInterceptor> startinterceptors = new ArrayList<ContextInterceptor>();
    private ArrayList<ContextInterceptor> endinterceptors = new ArrayList<ContextInterceptor>();
    private ArrayList<ContextInterceptor> postRenderInterceptors = new ArrayList<ContextInterceptor>();
    private String navigationFile = null;
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
        this.cacheResources = ref.cacheResources;
        this.conditions = ref.conditions;
        this.defaultAuthConstraint = ref.defaultAuthConstraint;
        this.defaultPage = ref.defaultPage;
        this.defaultStateClass = ref.defaultStateClass;
        this.endInterceptorBeans = ref.endInterceptorBeans;
        this.endinterceptors = ref.endinterceptors;
        this.navigationFile = ref.navigationFile;
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
        for(Class<?> itf:config.getInterfaces()) {
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
    
    public void setPageFlowMap(Map<String, PageFlow> map) {
        this.pageflows = map;
    }
    
    public Map<String, PageFlow> getPageFlowMap() {
        return this.pageflows;
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

    public Map<String, ? extends PageRequestConfig> getPageRequestConfigMap() {
        return pagerequests;
    }

    public void setPageRequestConfigMap(Map<String, ? extends PageRequestConfig> pageRequestConfigMap) {
        this.pagerequests = pageRequestConfigMap;
    }

    public PageRequestConfig getPageRequestConfig(String name) {
        return getPageRequestConfigMap().get(name);
    }
    
}
