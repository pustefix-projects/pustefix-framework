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

import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.extension.support.AbstractExtension;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.pustefixframework.util.VersionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * Abstract base class for parsing handlers that handle extension point
 * declarations.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractExtensionParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"type"}, null);
        
        String type = element.getAttribute("type").trim();
        
        NodeList nl = element.getElementsByTagNameNS(element.getNamespaceURI(), "extend");
        if(nl.getLength() == 0) nl = element.getElementsByTagNameNS(element.getNamespaceURI(), "extends");
        LinkedList<ExtensionTargetInfo> infos = new LinkedList<ExtensionTargetInfo>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element extendElement = (Element) nl.item(i);
            ParsingUtils.checkAttributes(extendElement, new String[] {"extension-point"}, new String[] {"version"});
            String extensionPoint = extendElement.getAttribute("extension-point").trim();
            String version = extendElement.getAttribute("version").trim();
            if (version.length() == 0) {
                version = "*";
            }
            if (!VersionUtils.isVersionOrRange(version)) {
                throw new ParserException("Not a valid version or version range: " + version);
            }
            ExtensionTargetInfo info = new ExtensionTargetInfo();
            info.setExtensionPoint(extensionPoint);
            info.setVersion(version);
            infos.add(info);
        }
        
        // Create the extension object (performed in child implementation)
        BeanDefinition beanDefinition = createExtension(type, infos, context);
        
        // Utilities for creating beans
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        
        // Add properties and register bean definition
        beanDefinition.getPropertyValues().addPropertyValue("type", type);
        beanDefinition.getPropertyValues().addPropertyValue("extensionTargetInfos", infos);
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
    }
    
    /**
     * Creates the extension of the appropriate type.
     * 
     * @param type type of the extension
     * @param extensionTargetInfos list of all extension points
     *  that should be extended
     * @param context context the parsing handler was called with
     * @return bean definition for a subclass of {@link AbstractExtension}.
     *  The fields extensionTargetInfos and type are set by the base class.
     *  The field extensionPointType has to be set by the child implementation.
     * @throws ParserException if extension cannot be created
     *  (e.g. type is not supported)
     */
    protected abstract BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException;
}
