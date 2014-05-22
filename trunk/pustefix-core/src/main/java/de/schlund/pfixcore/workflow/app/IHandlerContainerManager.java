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

package de.schlund.pfixcore.workflow.app;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.ConfigurableObject;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.XMLException;

/**
 * This class is responsible for managing all {@link IHandlerContainer}.
 * For every servlet in the Pustefix system one IHandlerContainerManager exists.
 * <br/>
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class IHandlerContainerManager implements ConfigurableObject {
    /** Store the already created IHandlerContainer here, use the page as key*/
    private HashMap<PageRequest, IHandlerContainer> known = new HashMap<PageRequest, IHandlerContainer>();
    private Map<String, Map<PageRequest, IHandlerContainer>> tenantToKnown = new HashMap<String, Map<PageRequest, IHandlerContainer>>();
    
    /**
     * @see de.schlund.pfixxml.PropertyObject#init(Properties)
     */
    public void init(Object dummy) {
    	// Takes a dummy context config object, which is only used to ensure
    	// there is one instance of IHandlerContainerManager per context
        // Intentionally do nothing here :-)
    }
    
    /**
     * Get the IHandler according to the passed context. If the IHandler is already
     * known it will be returned, else it will be created.
     * @param context the context containing the {@link PageRequest} where
     * the desired IHandlerContainer is responsible for.
     * @return the desired IHandlerContainer
     * @throws XMLException on errors when creating the IHandlerContainer.
     */
    public IHandlerContainer getIHandlerContainer(Context context, StateConfig stateConfig) throws XMLException {
        PageRequest page = context.getCurrentPageRequest();
        IHandlerContainer retval = null;
        Tenant tenant = context.getTenant();
        if(tenant == null) {
            synchronized(known) {
                retval = known.get(page);
            }
        } else {
            synchronized(tenantToKnown) {
                Map<PageRequest, IHandlerContainer> cont = tenantToKnown.get(tenant.getName());
                if(cont != null) {
                    retval = cont.get(page);
                }
            }
        }
        if (retval == null) {
            retval = new IHandlerContainerImpl();
            retval.initIHandlers(stateConfig, context.getTenant());
            if(tenant == null) {
                synchronized(known) {
                    known.put(page, retval);
                }
            } else {
                synchronized(tenantToKnown) {
                    Map<PageRequest, IHandlerContainer> cont = tenantToKnown.get(tenant.getName());
                    if(cont == null) {
                        cont = new HashMap<PageRequest, IHandlerContainer>();
                        tenantToKnown.put(tenant.getName(), cont);
                    }
                    cont.put(page, retval);
                }
            }
        }
        return retval;
    }

}// IHandlerContainerFactory
