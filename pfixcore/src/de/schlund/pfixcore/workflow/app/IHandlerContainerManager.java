/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.workflow.app;
import java.util.HashMap;
import java.util.Properties;

import org.pustefixframework.config.contextxml.StateConfig;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.ConfigurableObject;
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
        synchronized (known) {
            PageRequest       page   = context.getCurrentPageRequest();
            IHandlerContainer retval = known.get(page); 
            if (retval == null) {
                retval = new IHandlerContainerImpl();
                retval.initIHandlers(stateConfig);
                known.put(page, retval);
            }
            return retval;
        }
    }

}// IHandlerContainerFactory
