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

package org.pustefixframework.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pustefixframework.webservices.spring.WebserviceRegistration;


/**
 * @author mleidig@schlund.de
 */
public class ServiceRegistry {
	
	List<WebserviceRegistration> registrations;
	Map<String,WebserviceRegistration> nameToRegistration;
	Map<String,ServiceDescriptor> nameToDescriptor;

	public ServiceRegistry() {
		setWebserviceRegistrations(new ArrayList<WebserviceRegistration>());
	}
	
	public synchronized void setWebserviceRegistrations(List<WebserviceRegistration> registrations) {
		this.registrations = registrations;
		nameToRegistration = buildRegistrationMap(registrations);
		nameToDescriptor = buildServiceDescriptors(registrations);
	}
	
	public synchronized WebserviceRegistration getWebserviceRegistration(String serviceName) {
		return nameToRegistration.get(serviceName);
	}
	
	public synchronized List<WebserviceRegistration> getWebserviceRegistrations() {
		return registrations;
	}
  
	public synchronized ServiceDescriptor getServiceDescriptor(String serviceName) {
		return nameToDescriptor.get(serviceName);
	}
	
	private Map<String,WebserviceRegistration> buildRegistrationMap(List<WebserviceRegistration> registrations) {
		Map<String,WebserviceRegistration> map = new HashMap<String,WebserviceRegistration>();
		for(WebserviceRegistration registration: registrations) {
			map.put(registration.getServiceName(), registration);
		}
		return map;
	}
	
	private Map<String,ServiceDescriptor> buildServiceDescriptors(List<WebserviceRegistration> registrations) {
		Map<String, ServiceDescriptor> descriptors = new HashMap<String, ServiceDescriptor>();
		for(WebserviceRegistration registration: registrations) {
			try {
				ServiceDescriptor descriptor = new ServiceDescriptor(registration);
				nameToDescriptor.put(registration.getServiceName(), descriptor);
			} catch(ServiceException x) {
				throw new RuntimeException("Can't create ServiceDescriptor for service: " + registration.getServiceName());
			}
		}
		return descriptors;
	}
	
}
