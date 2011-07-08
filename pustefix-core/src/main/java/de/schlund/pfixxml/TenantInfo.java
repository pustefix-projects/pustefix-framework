package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class TenantInfo {
    
    private List<Tenant> tenants = new ArrayList<Tenant>();
    
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
    
    public List<Tenant> getTenants() {
        return tenants;
    }

}
