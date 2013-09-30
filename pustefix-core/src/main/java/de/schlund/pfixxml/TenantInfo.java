package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.pustefixframework.util.LocaleUtils;

public class TenantInfo {
    
    private List<Tenant> tenants = new ArrayList<Tenant>();
    private Map<String, Tenant> nameToTenant = new HashMap<String, Tenant>();
    private Set<String> languagePrefixes = new HashSet<String>();
    private Map<String, Tenant> domainPrefixToTenant;
    private Map<Tenant, String> tenantToDomainPrefix;
    
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
