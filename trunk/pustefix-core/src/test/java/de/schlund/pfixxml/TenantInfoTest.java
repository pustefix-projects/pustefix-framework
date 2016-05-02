package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class TenantInfoTest extends TestCase {

    public void testDomainPrefixDetection() {

        TenantInfo tenantInfo = new TenantInfo();
        List<Tenant> tenants = new ArrayList<Tenant>();
        tenants.add(new Tenant("DE_tenant"));
        tenants.add(new Tenant("FR_tenant"));
        tenants.add(new Tenant("US_tenant"));
        tenantInfo.setTenants(tenants);
        Map<String, Tenant> prefPrefixes = tenantInfo.getTenantsByDomainPrefix();
        Set<String> result = new HashSet<String>();
        result.add("de");
        result.add("fr");
        result.add("us");
        assertEquals(result, prefPrefixes.keySet());

        tenants.add(new Tenant("UK"));
        tenantInfo.setTenants(tenants);
        prefPrefixes = tenantInfo.getTenantsByDomainPrefix();
        result = new HashSet<String>();
        result.add("de_tenant");
        result.add("fr_tenant");
        result.add("us_tenant");
        result.add("uk");
        assertEquals(result, prefPrefixes.keySet());
 
        tenants.clear();
        tenants.add(new Tenant("tenant-DE"));
        tenants.add(new Tenant("tenant-FR"));
        tenants.add(new Tenant("tenant-US"));
        tenantInfo.setTenants(tenants);
        prefPrefixes = tenantInfo.getTenantsByDomainPrefix();
        result = new HashSet<String>();
        result.add("de");
        result.add("fr");
        result.add("us");
        assertEquals(result, prefPrefixes.keySet());

    }

}
