package org.pustefixframework.container.spring.beans;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import de.schlund.pfixxml.Tenant;

public class TenantAwareProperties extends Properties {

    private static final long serialVersionUID = 1921045153745536955L;
    
    public TenantAwareProperties() {
        super();
    }
    
    public TenantAwareProperties(Properties properties) {
        super(properties);
    }
    
    @Override
	public String getProperty(String key) {
        
        String value = null;
        String tenant = getTenant();
        if(tenant != null) {
            value = super.getProperty(key + "[" + tenant + "]");
        }
        if(value == null) {
            value = super.getProperty(key);
        }
        return value;
	}

    @Override
    public Set<String> stringPropertyNames() {
        
        Set<String> propertyNames = super.stringPropertyNames();
        String tenant = getTenant();
        if(tenant != null) {
            Set<String> filteredNames = new HashSet<String>();
            String suffix = "[" + tenant + "]";
            Iterator<String> it = propertyNames.iterator();
            while(it.hasNext()) {
                String name = it.next();
                if(name.endsWith("]")) {
                    if(name.endsWith(suffix)) {
                        filteredNames.add(name.substring(0, name.length() - suffix.length()));
                    }
                } else {
                    filteredNames.add(name);
                }
            }
            propertyNames = filteredNames;
        }
        return propertyNames;
    }
    
    @Override
    public Enumeration<?> propertyNames() {
        
        Enumeration<?> propertyNames = super.propertyNames();
        String tenant = getTenant();
        if(tenant != null) {
            Set<String> filteredNames = new HashSet<String>();
            String suffix = "[" + tenant + "]";
            while(propertyNames.hasMoreElements()) {
                String name = (String)propertyNames.nextElement();
                if(name.endsWith("]")) {
                    if(name.endsWith(suffix)) {
                        filteredNames.add(name.substring(0, name.length() - suffix.length()));
                    }
                } else {
                    filteredNames.add(name);
                }
            }
            propertyNames = Collections.enumeration(filteredNames);
        }
        return propertyNames;
    }
    
    private String getTenant() {
        
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if(attributes != null) {
            Tenant tenant = (Tenant)attributes.getAttribute("__PFX_TENANT__", RequestAttributes.SCOPE_REQUEST);
            if(tenant == null) {
                return null;
            } else {
                return tenant.getName();
            }
        }
        return null;
    }

}
