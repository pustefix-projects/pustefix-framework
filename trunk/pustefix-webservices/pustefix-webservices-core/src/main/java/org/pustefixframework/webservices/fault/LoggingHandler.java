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
package org.pustefixframework.webservices.fault;

import org.apache.log4j.Logger;

public class LoggingHandler extends FaultHandler {

    private static final long serialVersionUID = -5523320091746362278L;
    
    private Logger LOG=Logger.getLogger(getClass().getName());
	
    @Override
    public void init() {
        
    }
    
	@Override
	public void handleFault(Fault fault) {
        LOG.error("Service name: "+fault.getServiceName());
        LOG.error("Exception name: "+fault.getName());
        LOG.error("Exception message: "+fault.getMessage());
        LOG.error("Request message: "+fault.getRequestMessage());
        LOG.error("Context: "+(fault.getContext()==null?"-":fault.getContext()));
        LOG.error("Stacktrace: "+fault.getStackTrace());
	}
	
}
