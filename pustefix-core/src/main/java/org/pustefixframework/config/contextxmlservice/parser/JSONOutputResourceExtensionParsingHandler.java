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

import org.pustefixframework.config.contextxmlservice.JSONOutputResourceHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.JSONOutputResourceExtensionImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.JSONOutputResourceExtensionPointImpl;
import org.pustefixframework.config.generic.AbstractExtensionParsingHandler;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Creates an extension for a public JSON object extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class JSONOutputResourceExtensionParsingHandler extends AbstractExtensionParsingHandler {

    @Override
    protected BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException {
        List<JSONOutputResourceExtensionPointImpl> extensionPoints = new LinkedList<JSONOutputResourceExtensionPointImpl>();
        extensionPoints.addAll(context.getObjectTreeElement().getObjectsOfTypeFromSubTree(JSONOutputResourceExtensionPointImpl.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> jsonOutputResourceMap = new ManagedMap();
        for (JSONOutputResourceHolder holder : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(JSONOutputResourceHolder.class)) {
            jsonOutputResourceMap.put(holder.getName(), holder.getJSONOutputResource());
        }

        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(JSONOutputResourceExtensionImpl.class);
        beanBuilder.addPropertyValue("JSONOutputResources", jsonOutputResourceMap);
        beanBuilder.addPropertyValue("JSONOutputResourceExtensionPoints", extensionPoints);
        return beanBuilder.getBeanDefinition();
    }

}
