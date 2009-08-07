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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pustefixframework.config.contextxmlservice.ProcessActionPageRequestConfig;
import org.pustefixframework.config.contextxmlservice.ProcessActionStateConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestProcessActionConfigExtensionImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestProcessActionConfigExtensionPointImpl;
import org.pustefixframework.config.generic.AbstractExtensionParsingHandler;
import org.pustefixframework.extension.PageRequestProcessActionConfigExtension.ProcessActionConfig;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Creates an extension for a process action configuration extension point.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestProcessActionConfigExtensionParsingHandler extends AbstractExtensionParsingHandler {

    @Override
    protected BeanDefinition createExtension(String type, Collection<ExtensionTargetInfo> extensionTargetInfos, HandlerContext context) throws ParserException {
        Map<String, ProcessActionStateConfig> processActionStateConfigs = new HashMap<String, ProcessActionStateConfig>();
        List<ProcessActionConfigImpl> processActionConfigs = new LinkedList<ProcessActionConfigImpl>();
        List<Object> processActionConfigObjects = new LinkedList<Object>();

        // Find all valid objects and create a list containing
        // extension point and compound objects.
        for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
            if (o instanceof ProcessActionStateConfig) {
                ProcessActionStateConfig processActionStateConfig = (ProcessActionStateConfig) o;
                processActionStateConfigs.put(processActionStateConfig.getName(), processActionStateConfig);
            } else if (o instanceof ProcessActionPageRequestConfig) {
                ProcessActionConfigImpl processActionConfig = new ProcessActionConfigImpl();
                processActionConfig.processActionPageRequestConfig = (ProcessActionPageRequestConfig) o;
                processActionConfigObjects.add(processActionConfig);
                processActionConfigs.add(processActionConfig);
            } else if (o instanceof PageRequestProcessActionConfigExtensionPointImpl) {
                processActionConfigObjects.add(o);
            }
        }

        // Find state configuration for all page request configurations
        // (if available) and add them to the compound configuration.
        // As the compound configurations stored in the 
        // processActionConfigObjects list are the same objects, the change
        // will also be performed there.
        for (ProcessActionConfigImpl processActionConfig : processActionConfigs) {
            processActionConfig.processActionStateConfig = processActionStateConfigs.get(processActionConfig.getName());
        }

        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageRequestProcessActionConfigExtensionImpl.class);
        beanBuilder.addPropertyValue("processActionConfigObjects", processActionConfigObjects);
        return beanBuilder.getBeanDefinition();
    }

    private class ProcessActionConfigImpl implements ProcessActionConfig {

        private ProcessActionPageRequestConfig processActionPageRequestConfig;

        private ProcessActionStateConfig processActionStateConfig;

        public String getName() {
            return processActionPageRequestConfig.getName();
        }

        public ProcessActionPageRequestConfig getProcessActionPageRequestConfig() {
            return processActionPageRequestConfig;
        }

        public ProcessActionStateConfig getProcessActionStateConfig() {
            return processActionStateConfig;
        }

    }
}
