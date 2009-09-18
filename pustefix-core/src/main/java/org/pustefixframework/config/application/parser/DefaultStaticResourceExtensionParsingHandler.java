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

package org.pustefixframework.config.application.parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionImpl;
import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionPointImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.extension.support.AbstractExtension;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.pustefixframework.resource.ResourceLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Special extension that attaches to default static resource extension point(s)
 * and provides static resources from a module.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DefaultStaticResourceExtensionParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        String type = "application.static";
        String version = "0.0.0";
        String extensionPoint = Constants.EXTENSION_POINT_DEFAULT_STATIC_RESOURCES;

        ExtensionTargetInfo info = new ExtensionTargetInfo();
        info.setExtensionPoint(extensionPoint);
        info.setVersion(version);
        LinkedList<ExtensionTargetInfo> infos = new LinkedList<ExtensionTargetInfo>();
        infos.add(info);

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
    private BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException {
        Element extensionElement = (Element) context.getNode();

        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
        ConfigurableOsgiBundleApplicationContext applicationContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);

        String requestPathPrefix = "bundle/" + applicationContext.getBundle().getSymbolicName() + "/";

        List<String> paths = new LinkedList<String>();

        NodeList pathList = extensionElement.getElementsByTagNameNS(extensionElement.getNamespaceURI(), "path");
        for (int j = 0; j < pathList.getLength(); j++) {
            Element pathElement = (Element) pathList.item(j);
            String path = pathElement.getTextContent();
            if (!paths.contains(path)) {
                paths.add(path);
            }
        }

        LinkedList<StaticResourceExtensionPointImpl> extensionPoints = new LinkedList<StaticResourceExtensionPointImpl>();
        extensionPoints.addAll(context.getObjectTreeElement().getObjectsOfTypeFromSubTree(StaticResourceExtensionPointImpl.class));

        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(StaticResourceExtensionImpl.class);
        beanBuilder.addPropertyValue("staticResourceExtensionPoints", extensionPoints);
        beanBuilder.addPropertyValue("requestPathPrefix", requestPathPrefix);
        beanBuilder.addPropertyValue("modulePathPrefixes", paths);
        beanBuilder.addPropertyValue("resourceLoader", resourceLoader);
        return beanBuilder.getBeanDefinition();
    }
}
