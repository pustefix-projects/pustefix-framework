package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.pustefixframework.util.LocaleUtils;

public class TenantInfo {
    
    private List<Tenant> tenants = new ArrayList<Tenant>();
    private Set<String> languagePrefixes = new HashSet<String>();
    
    public Tenant getMatchingTenant(HttpServletRequest req) {
        for(Tenant tenant: tenants) {
            if(tenant.matches(req)) {
                return tenant;
            }
        }
        return null;
    }
    
    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
        for(Tenant tenant: tenants) {
            for(String lang: tenant.getSupportedLanguages()) {
                if(!lang.equals(tenant.getDefaultLanguage())) {
                    languagePrefixes.add(LocaleUtils.getLanguagePart(lang));
                }
            }
        }
    }
    
    public List<Tenant> getTenants() {
        return tenants;
    }
    
    public boolean isLanguagePrefix(String prefix) {
        return languagePrefixes.contains(prefix);
    }

}
