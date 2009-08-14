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

import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionImpl;
import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionPointImpl;
import org.pustefixframework.config.generic.AbstractExtensionParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.pustefixframework.resource.ResourceLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Creates an extension for a static resource extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class StaticResourceExtensionParsingHandler extends AbstractExtensionParsingHandler {

    @Override
    protected BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException {
        Element extensionElement = (Element) context.getNode();

        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);

        NodeList requestPathPrefixList = extensionElement.getElementsByTagNameNS(extensionElement.getNamespaceURI(), "request-path-prefix");
        if (requestPathPrefixList.getLength() > 1) {
            throw new ParserException("Found " + requestPathPrefixList.getLength() + " <request-path-prefix> elements but expected one.");
        }
        Element requestPathPrefixElement = requestPathPrefixList.getLength() > 0 ? (Element) requestPathPrefixList.item(0) : null;
        String requestPathPrefix = requestPathPrefixElement != null ? requestPathPrefixElement.getTextContent() : null;

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
