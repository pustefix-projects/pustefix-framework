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

import java.util.List;

import org.pustefixframework.webservices.BaseTestCase;
import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class WebServiceBeanConfigReaderTest extends BaseTestCase {

    public void testRead() throws Exception {
    
        FileResource res = ResourceUtil.getFileResource("pfixroot:/conf/spring.xml");
        List<ServiceConfig> serviceList = WebServiceBeanConfigReader.read(res);
        for(ServiceConfig service:serviceList) {
            System.out.println("***** "+service.getName()+" "+service.getInterfaceName()+" "+service.getImplementationName());
        }
        
    }
    
}
