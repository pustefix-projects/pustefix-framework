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

package org.pustefixframework.config.directoutputservice.parser;

import java.util.Collection;
import java.util.Properties;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfig;
import org.pustefixframework.config.directoutputservice.parser.internal.DirectOutputServiceConfigImpl;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class DirectOutputServiceConfigParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        if (context.getRunOrder() == RunOrder.START) {
            DirectOutputServiceConfigImpl serviceConfig = new DirectOutputServiceConfigImpl();
            Properties properties = new Properties(System.getProperties());
            serviceConfig.setProperties(properties);
            context.getObjectTreeElement().addObject(serviceConfig);
        } else if (context.getRunOrder() == RunOrder.END) {
            DirectOutputServiceConfigImpl serviceConfig = context.getObjectTreeElement().getObjectsOfType(DirectOutputServiceConfigImpl.class).iterator().next();
            Collection<DirectOutputPageRequestConfig> requestsCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(DirectOutputPageRequestConfig.class);
            for (DirectOutputPageRequestConfig config : requestsCollection) {
                serviceConfig.addPageRequest(config);
            }
        }
    }

}
