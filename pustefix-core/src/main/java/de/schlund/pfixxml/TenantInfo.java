package de.schlund.pfixxml;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class TenantInfo {
    
    private List<Tenant> tenants;
    
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
    }

}
