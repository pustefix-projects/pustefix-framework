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
import java.util.List;

import org.pustefixframework.config.contextxmlservice.PageRequestConfigHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigExtensionImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigVariantExtensionPointImpl;
import org.pustefixframework.config.generic.AbstractExtensionParsingHandler;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Creates an extension for a page configuration extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestConfigExtensionParsingHandler extends AbstractExtensionParsingHandler {

    @Override
    protected BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException {
        @SuppressWarnings("unchecked")
        List<Object> pageConfigObjects = new ManagedList();
        for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
            if (o instanceof PageRequestConfigHolder) {
                PageRequestConfigHolder pageConfigHolder = (PageRequestConfigHolder) o;
                pageConfigObjects.add(pageConfigHolder.getPageRequestConfigObject());
            } else if (o instanceof PageRequestConfigExtensionPointImpl) {
                pageConfigObjects.add(o);
            } else if (o instanceof PageRequestConfigVariantExtensionPointImpl) {
                pageConfigObjects.add(o);
            }
        }

        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageRequestConfigExtensionImpl.class);
        beanBuilder.addPropertyValue("pageRequestConfigObjects", pageConfigObjects);
        return beanBuilder.getBeanDefinition();
    }

}
