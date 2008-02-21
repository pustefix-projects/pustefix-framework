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

package de.schlund.pfixcore.webservice;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * @author mleidig@schlund.de
 */
public abstract class AbstractService {

	protected Context getContext() {
		ServiceCallContext callContext=ServiceCallContext.getCurrentContext();
		if(callContext!=null) return callContext.getContext();
		return null;
	}
	
    protected ContextResourceManager getContextResourceManager() {
    	Context context=getContext();
    	if(context!=null) return context.getContextResourceManager();
    	return null;
    }

    protected ContextResource getContextResource(Class<? extends ContextResource> clazz) {
    	if(clazz==null) throw new IllegalArgumentException("clazz="+clazz);
        return getContextResource(clazz.getName());
    }

    protected ContextResource getContextResource(String name) {
        ContextResourceManager crm=getContextResourceManager();
        if(crm!=null) return crm.getResource(name);
        return null;
    }

}


