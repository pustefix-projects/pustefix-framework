package org.pustefixframework.container.spring.beans;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.schlund.pfixxml.Tenant;

public class TenantAwarePropertiesTest extends TestCase {

    public void test() {
        
        TenantAwareProperties props = new TenantAwareProperties();
        
        props.setProperty("foo", "1");
        props.setProperty("bar", "2");
        props.setProperty("baz", "3");
        
        Set<String> propNames = new HashSet<String>();
        propNames.add("foo");
        propNames.add("bar");
        propNames.add("baz");
        
        assertEquals(propNames, props.stringPropertyNames());
        
        props.setProperty("bar[DE]", "222");
        
        assertEquals("2", props.getProperty("bar"));
        
        setTenant("DE");
        
        assertEquals("222", props.getProperty("bar"));
        assertEquals(propNames, props.stringPropertyNames());
      
        props.setProperty("hey[UK]", "4");
        
        assertNull(props.getProperty("hey"));
        assertEquals(propNames, props.stringPropertyNames());
        
        setTenant("UK");
        
        propNames.add("hey");
        
        assertEquals(propNames, props.stringPropertyNames());
        
        
    }
    
    private void setTenant(String name) {
        
        Tenant tenant = new Tenant(name);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT, tenant);
        RequestAttributes attributes = new ServletRequestAttributes(req);
        RequestContextHolder.setRequestAttributes(attributes);
    }
    
    @Override
    protected void tearDown() throws Exception {
        
        RequestContextHolder.setRequestAttributes(null);
    }
    
}
