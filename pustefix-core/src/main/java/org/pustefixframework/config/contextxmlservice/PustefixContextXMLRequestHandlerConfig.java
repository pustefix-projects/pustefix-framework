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

package org.pustefixframework.config.contextxmlservice;

import java.util.Map;

/**
 * Provides configuration for {@link de.schlund.pfixxml.ContextXMLServlet}.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PustefixContextXMLRequestHandlerConfig extends AbstractPustefixXMLRequestHandlerConfig {

    /**
     * Returns configuration that should be used by the context instances
     * created by the servlet.
     * 
     * @return configuration for context instances
     */
    ContextConfig getContextConfig();
    
    /**
     * Returns map of scripted flows. The name of the flow is used
     * as the key and a provider for the flow as the value of the map.
     * 
     * @return scripted flows
     */
    Map<String, ? extends ScriptedFlowProvider> getScriptedFlows();

    /**
     * Returns a map mapping string aliases, to context resource
     * instances. This map is used in order to decide which 
     * resources should be exposed through JSON using the specified
     * aliases.
     * 
     * @return map of context resources
     */
    Map<String, ?> getJSONOutputResources();
}