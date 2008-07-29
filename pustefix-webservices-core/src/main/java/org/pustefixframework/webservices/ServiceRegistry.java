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

package org.pustefixframework.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ServiceConfig;


/**
 * @author mleidig@schlund.de
 */
public class ServiceRegistry {
	
	public enum RegistryType {APPLICATION,SESSION};
	
	Configuration configuration;
    
	RegistryType registryType;
	Map<String,ServiceDescriptor> serviceDescriptors;
	Map<String,Object> serviceObjects;
    
	public ServiceRegistry(Configuration configuration,RegistryType registryType) {
		this.configuration=configuration;
		this.registryType=registryType;
        serviceDescriptors=new HashMap<String,ServiceDescriptor>();
        serviceObjects=new HashMap<String,Object>();
	}

    public void deregisterService(String serviceName) {
        //remove serviceconfig, servicedescriptor and serviceobject
        throw new RuntimeException("Not yet implemented!");
    }
    
	public boolean isRegistered(String serviceName) {
	    return getService(serviceName)!=null;
	}

	public ServiceConfig getService(String serviceName) {
		ServiceConfig srvConf=configuration.getServiceConfig(serviceName);
		if(srvConf!=null) {
			String scope=srvConf.getScopeType();
			if((scope.equals(Constants.SERVICE_SCOPE_APPLICATION)&&registryType==RegistryType.APPLICATION)||
                    (scope.equals(Constants.SERVICE_SCOPE_SESSION)&&registryType==RegistryType.SESSION)||
                    (scope.equals(Constants.SERVICE_SCOPE_REQUEST)))
				return srvConf;
        }
		return null;
	}
    
    public List<ServiceConfig> getServices() {
        List<ServiceConfig> list=new ArrayList<ServiceConfig>();
        for(ServiceConfig srvConf:configuration.getServiceConfig()) {
            String scope=srvConf.getScopeType();
            if((scope.equals(Constants.SERVICE_SCOPE_APPLICATION)&&registryType==RegistryType.APPLICATION)||
                    (scope.equals(Constants.SERVICE_SCOPE_SESSION)&&registryType==RegistryType.SESSION))
                list.add(srvConf);
        }
        return list;
    }
	
	public ServiceDescriptor getServiceDescriptor(String serviceName) throws ServiceException {
        ServiceDescriptor srvDesc=null;
        synchronized(serviceDescriptors) {
            srvDesc=serviceDescriptors.get(serviceName);
        }
        if(srvDesc==null) {
            ServiceConfig srvConf=getService(serviceName);
            if(srvConf!=null) {
                synchronized(serviceDescriptors) {
                    srvDesc=new ServiceDescriptor(srvConf);
                    serviceDescriptors.put(serviceName,srvDesc);
                }
            }
        }
        return srvDesc;
	}
	
	public Object getServiceObject(String serviceName) throws ServiceException {
		Object serviceObject=null;
        synchronized(serviceObjects) {
            serviceObject=serviceObjects.get(serviceName);
        }
        if(serviceObject==null) {
            ServiceConfig srvConf=getService(serviceName);
            if(srvConf!=null) {
                String scope=srvConf.getScopeType();
                if(scope.equals(Constants.SERVICE_SCOPE_REQUEST)) return createServiceObject(srvConf);
				synchronized(serviceObjects) {
				    serviceObject=createServiceObject(srvConf);
				    serviceObjects.put(serviceName,serviceObject);
				}
			}
		}
		return serviceObject;
	}
	
	private Object createServiceObject(ServiceConfig srvConf) throws ServiceException {
        try {
            Class<?> clazz=Class.forName(srvConf.getImplementationName());
            Object serviceObject=clazz.newInstance();
            return serviceObject;
		} catch(Exception x) {
			throw new ServiceException("Can't create instance of service '"+srvConf.getName()+"'.",x);
		}
	}
	
	public void register(String serviceName, Object serviceObject) {
	    synchronized(serviceObjects) {
	        serviceObjects.put(serviceName, serviceObject);
	    }
	}
    
}
