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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author mleidig@schlund.de
 */
public class ServiceRequestWrapper implements ServiceRequest {

	ServiceRequest request;
	
	public ServiceRequestWrapper(ServiceRequest request) {
		this.request=request;
	}

	public String getMessage() throws IOException {
		return request.getMessage();
	}

	public Reader getMessageReader() throws IOException {
		return request.getMessageReader();
	}

	public InputStream getMessageStream() throws IOException {
		return request.getMessageStream();
	}

	public String getParameter(String name) {
		return request.getParameter(name);
	}

	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}
		
	public String getServiceName() {
		return request.getServiceName();
	}
	
	public Object getUnderlyingRequest() {
		return request.getUnderlyingRequest();
	}
	
    public String dump() {
        return request.dump();
    }
    
}
