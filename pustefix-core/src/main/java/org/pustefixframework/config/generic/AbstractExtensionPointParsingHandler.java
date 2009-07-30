/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.generic;

import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.PageFlowExtensionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * Abstract base class for parsing handlers that handle extension point
 * declarations.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractExtensionPointParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"id", "type"}, new String[] {"version", "cardinality"});
        
        String id = element.getAttribute("id").trim();
        String type = element.getAttribute("type").trim();
        String version = element.getAttribute("version").trim();
        if (version.length() == 0) {
            version = "0.0.0";
        }
        String cardinality = element.getAttribute("cardinality").trim();
        if (cardinality.length() == 0) {
            cardinality = "0..n";
        }
        
        ExtensionPoint<?> extensionPoint = createExtensionPoint(id, type, version, cardinality, context);
        
        // Register extension point as a service
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        BeanDefinition beanDefinition;
        BeanDefinitionBuilder beanBuilder;
        
        Map<String, String> serviceProperties = new HashMap<String, String>();
        serviceProperties.put("extension-point", id);
        serviceProperties.put("type", type);
        serviceProperties.put("version", version);
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(OsgiServiceFactoryBean.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("interfaces", new Class[] {ExtensionPoint.class, PageFlowExtensionPoint.class});
        beanBuilder.addPropertyValue("serviceProperties", serviceProperties);
        beanBuilder.addPropertyValue("target", extensionPoint);
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
        
        // Make extension point available in object tree
        context.getObjectTreeElement().addObject(extensionPoint);
    }
    
    /**
     * Creates the extension point of the appropriate type.
     * 
     * @param id identifier for the extension point
     * @param type type of the extension point
     * @param version version of the extension point
     * @param cardinality cardinality for the extension point
     * @param context context the parsing handler was called with
     * @return extension point object of the appropriate type
     * @throws ParserException if extension point cannot be created
     *  (e.g. type is not supported)
     */
    protected abstract ExtensionPoint<?> createExtensionPoint(String id, String type, String version, String cardinality, HandlerContext context) throws ParserException;
}
