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

package org.pustefixframework.config.contextxmlservice.parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pustefixframework.config.contextxmlservice.PageRequestOutputResourceHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestOutputResourceExtensionImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestOutputResourceExtensionPointImpl;
import org.pustefixframework.config.generic.AbstractExtensionParsingHandler;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Creates an extension for a page output resource extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestOutputResourceExtensionParsingHandler extends AbstractExtensionParsingHandler {

    @Override
    protected BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException {
        List<PageRequestOutputResourceExtensionPointImpl> extensionPoints = new LinkedList<PageRequestOutputResourceExtensionPointImpl>();
        extensionPoints.addAll(context.getObjectTreeElement().getObjectsOfTypeFromSubTree(PageRequestOutputResourceExtensionPointImpl.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> resourceMap = new ManagedMap();
        for (PageRequestOutputResourceHolder holder : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(PageRequestOutputResourceHolder.class)) {
            resourceMap.put(holder.getName(), holder.getOutputResource());
        }

        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageRequestOutputResourceExtensionImpl.class);
        beanBuilder.addPropertyValue("outputResourceObjects", resourceMap);
        beanBuilder.addPropertyValue("outputResourceExtensionPoints", extensionPoints);
        return beanBuilder.getBeanDefinition();
    }

}
