/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.pustefixframework.webservices.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pustefixframework.webservices.BaseTestCase;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.pustefixframework.webservices.example.CalculatorImpl;
import org.pustefixframework.webservices.example.TestImpl;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * 
 * @author mleidig
 *
 */
public class WebServiceBeanConfigReaderTest extends BaseTestCase {

    @Test
    public void reading() throws Exception {
    
        Map<String,String> services = new HashMap<String,String>();
        services.put("Calculator", CalculatorImpl.class.getName());
        services.put("Test", TestImpl.class.getName());
        services.put("TestNested", TestImpl.class.getName());
        services.put("TestNestedRef", TestImpl.class.getName());
        
        FileResource res = ResourceUtil.getFileResource("docroot:/WEB-INF/spring.xml");
        List<ServiceConfig> serviceList = WebServiceBeanConfigReader.read(res);
        assertEquals(services.size(), serviceList.size());
        
        for(ServiceConfig service:serviceList) {
            String impl = services.get(service.getName());
            assertNotNull(impl);
            assertEquals(impl, service.getImplementationName());
            if(service.getName().equals("TestNested")) {
                assertEquals("MY_AUTHCONSTRAINT", service.getAuthConstraintRef());
            }
        }
   
    }
    
}
