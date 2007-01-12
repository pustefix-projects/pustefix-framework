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

/**
 * @author mleidig@schlund.de
 */
public class ServiceCallContext {

	ServiceRuntime runtime;
	Context context;
    ServiceRequest request;
    ServiceResponse response;
	
	public ServiceCallContext(ServiceRuntime runtime) {
		this.runtime=runtime;
	}
	
	ServiceRuntime getServiceRuntime() {
		return runtime;
	}
	
	public static ServiceCallContext getCurrentContext() {
		return ServiceRuntime.getCurrentContext();
	}
	
	protected void setContext(Context context) {
		this.context=context;
	}
	
	public Context getContext() {
		return context;
	}
    
    protected void setServiceRequest(ServiceRequest request) {
        this.request=request;
    }
    
    public ServiceRequest getServiceRequest() {
        return request;
    }
    
    protected void setServiceResponse(ServiceResponse response) {
        this.response=response;
    }
    
    public ServiceResponse getServiceResponse() {
        return response;
    }
    
    void setInvocationTime(long time) {
        
    }
	
}
