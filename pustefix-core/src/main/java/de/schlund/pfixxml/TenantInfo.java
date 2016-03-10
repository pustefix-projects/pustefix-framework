package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.util.LocaleUtils;

import de.schlund.pfixxml.config.EnvironmentProperties;

public class TenantInfo {
    
    private Logger LOG = Logger.getLogger(TenantInfo.class);
    
    private List<Tenant> tenants = new ArrayList<Tenant>();
    private Map<String, Tenant> nameToTenant = new HashMap<String, Tenant>();
    private Set<String> languagePrefixes = new HashSet<String>();
    private Map<String, Tenant> domainPrefixToTenant;
    private Map<Tenant, String> tenantToDomainPrefix;
    
    /**
     * Get the tenant from the current request.
     * 
     * The tenant lookup is only done once for a request and cached
     * as request attribute.
     * 
     * @param request  the current request
     * @return the current tenant or null if no tenants are configured
     */
    public Tenant getTenant(HttpServletRequest request) {
        Tenant tenant = (Tenant)request.getAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT);
        if(tenant == null && !tenants.isEmpty()) {
            tenant = getMatchingTenant(request);
            if(tenant == null) {
                //check if tenant was provided as cookie (only allowed at development time)
                if(!"prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
                    Cookie[] cookies = request.getCookies();
                    if(cookies != null) {
                        for(Cookie cookie: cookies) {
                            if(cookie.getName().equals(TenantScope.REQUEST_ATTRIBUTE_TENANT)) {
                                String tenantName = cookie.getValue();
                                if(tenantName != null) {
                                    tenant = getTenant(tenantName);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(tenant == null) {
                    tenant = tenants.get(0);
                }
            }
            request.setAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT, tenant);
            if(LOG.isDebugEnabled()) {
                LOG.debug("Set tenant for current request: " + tenant.getName());
            }
        }
        return tenant;
    }
    
    public Tenant getMatchingTenant(HttpServletRequest req) {
        for(Tenant tenant: tenants) {
            if(tenant.matches(req)) {
                return tenant;
            }
        }
        return null;
    }
    
    public Tenant getTenant(String name) {
    	return nameToTenant.get(name);
    }
    
    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
        for(Tenant tenant: tenants) {
            for(String lang: tenant.getSupportedLanguages()) {
                if(!lang.equals(tenant.getDefaultLanguage())) {
                    languagePrefixes.add(LocaleUtils.getLanguagePart(lang));
                }
            }
            nameToTenant.put(tenant.getName(), tenant);
        }
        domainPrefixToTenant = detectDomainPrefixes();
        tenantToDomainPrefix = new HashMap<Tenant, String>();
        for(Entry<String, Tenant> entry: domainPrefixToTenant.entrySet()) {
        	tenantToDomainPrefix.put(entry.getValue(), entry.getKey());
        }
    }
    
    public List<Tenant> getTenants() {
        return tenants;
    }
    
    public boolean isLanguagePrefix(String prefix) {
        return languagePrefixes.contains(prefix);
    }
    
    public Map<String, Tenant> getTenantsByDomainPrefix() {
    	return domainPrefixToTenant;
    }
    
    public Map<Tenant, String> getDomainPrefixesByTenant() {
    	return tenantToDomainPrefix;
    }
    
    private Map<String, Tenant> detectDomainPrefixes() {
    	
    	//try to get unique tenant domain prefix by checking tenant name for prefixes/suffixes 
    	char separator = getTenantPrefixSeparator();
    	if(separator > 0) {
    		//try to get unique tenant prefix
    		Map<String, Tenant> prefixes = new HashMap<String, Tenant>();
    		Iterator<Tenant> it = tenants.iterator();
    		while(it.hasNext()) {
    			Tenant tenant = it.next();
    			String name = tenant.getName();
    			String prefix = name.substring(0, name.indexOf(separator)).toLowerCase();
    			if(prefixes.put(prefix, tenant) != null) {
    				break;
        		} else if(!it.hasNext()) {
        			return prefixes;
        		}
    		}
    		//try to get unique tenant suffix
    		prefixes.clear();
    		it = tenants.iterator();
    		while(it.hasNext()) {
    			Tenant tenant = it.next();
    			String name = tenant.getName();
    			String prefix = name.substring(name.indexOf(separator) + 1).toLowerCase();
    			if(prefixes.put(prefix, tenant) != null) {
    				break;
        		} else if(!it.hasNext()) {
        			return prefixes;
        		}
    		}
    	}
    	//no unique tenant name prefixes/suffixes found -> use complete tenant name
    	Map<String, Tenant> prefixes = new HashMap<String, Tenant>();
    	for(Tenant tenant: tenants) {
    		prefixes.put(tenant.getName().toLowerCase(), tenant);
    	}
    	return prefixes;
    }

    private char getTenantPrefixSeparator() {
    	if(hasTenantPrefixSeparator('_')) {
    		return '_';
    	} else if(hasTenantPrefixSeparator('-')) {
    		return '-';
    	}
    	return 0;
    }
    
    private boolean hasTenantPrefixSeparator(char separator) {
    	for(Tenant tenant: tenants) {
    		if(tenant.getName().indexOf(separator) == -1) {
    			return false;
    		}
    	}
    	return true;
    }

}
