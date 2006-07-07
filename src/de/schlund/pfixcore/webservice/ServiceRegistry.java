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

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixxml.loader.AppLoader;

/**
 * @author mleidig@schlund.de
 */
public class ServiceRegistry {
	
	enum RegistryType {APPLICATION,SESSION};
	
	ServiceRuntime runtime;
	RegistryType registryType;
	Map<String,ServiceDescriptor> serviceDescriptors;
	Map<String,Object> serviceObjects;
	
	ServiceRegistry subRegistry;
	
	public ServiceRegistry(ServiceRuntime runtime,RegistryType registryType) {
		this.runtime=runtime;
		this.registryType=registryType;
		serviceDescriptors=new HashMap<String,ServiceDescriptor>();
		serviceObjects=new HashMap<String,Object>();
	}

	protected void setSubRegistry(ServiceRegistry subRegistry) {
		this.subRegistry=subRegistry;
	}
	
	public boolean isRegistered(String serviceName) {
		GlobalServiceConfig globSrvConf=runtime.getConfiguration().getGlobalServiceConfig();
		ServiceConfig srvConf=runtime.getConfiguration().getServiceConfig(serviceName);
		if(srvConf!=null) {
			String scope=srvConf.getScopeType();
			if(scope==null) scope=globSrvConf.getScopeType();
			if(scope.equals(Constants.SERVICE_SCOPE_REQUEST)||
					(scope.equals(Constants.SERVICE_SCOPE_SESSION)&&registryType==RegistryType.SESSION)||
					(scope.equals(Constants.SERVICE_SCOPE_APPLICATION)&&registryType==RegistryType.APPLICATION))
				return true;
		}
		if(subRegistry!=null) return subRegistry.isRegistered(serviceName);
		return false;
	}
	
	public GlobalServiceConfig getGlobalServiceConfig() {
		return runtime.getConfiguration().getGlobalServiceConfig();
	}
	
	public ServiceConfig getServiceConfig(String serviceName) {
		GlobalServiceConfig globSrvConf=runtime.getConfiguration().getGlobalServiceConfig();
		ServiceConfig srvConf=runtime.getConfiguration().getServiceConfig(serviceName);
		if(srvConf!=null) {
			String scope=srvConf.getScopeType();
			if(scope==null) scope=globSrvConf.getScopeType();
			if(scope.equals(Constants.SERVICE_SCOPE_REQUEST)||
					(scope.equals(Constants.SERVICE_SCOPE_SESSION)&&registryType==RegistryType.SESSION)||
					(scope.equals(Constants.SERVICE_SCOPE_APPLICATION)&&registryType==RegistryType.APPLICATION))
				return srvConf;
		}
		if(subRegistry!=null) return subRegistry.getServiceConfig(serviceName);
		return null;
	}
	
	public ServiceDescriptor getServiceDescriptor(String serviceName) throws ServiceException {
		GlobalServiceConfig globSrvConf=runtime.getConfiguration().getGlobalServiceConfig();
		ServiceConfig srvConf=runtime.getConfiguration().getServiceConfig(serviceName);
		if(srvConf!=null) {
			String scope=srvConf.getScopeType();
			if(scope==null) scope=globSrvConf.getScopeType();
			if(scope.equals(Constants.SERVICE_SCOPE_REQUEST)||
					(scope.equals(Constants.SERVICE_SCOPE_SESSION)&&registryType==RegistryType.SESSION)||
					(scope.equals(Constants.SERVICE_SCOPE_APPLICATION)&&registryType==RegistryType.APPLICATION)) {
				ServiceDescriptor srvDesc=null;
				synchronized(serviceDescriptors) {
					srvDesc=serviceDescriptors.get(serviceName);
					if(srvDesc==null) {
						srvDesc=new ServiceDescriptor(srvConf);
						serviceDescriptors.put(serviceName,srvDesc);
					}
				}
				return srvDesc;
			}
		}
		if(subRegistry!=null) return subRegistry.getServiceDescriptor(serviceName);
		return null;
	}
	
	public Object getServiceObject(String serviceName) throws ServiceException {
		Object serviceObject=null;
		GlobalServiceConfig globSrvConf=runtime.getConfiguration().getGlobalServiceConfig();
		ServiceConfig srvConf=runtime.getConfiguration().getServiceConfig(serviceName);
		if(srvConf!=null) {
			String scope=srvConf.getScopeType();
			if(scope==null) scope=globSrvConf.getScopeType();
			if(scope.equals(Constants.SERVICE_SCOPE_REQUEST)) {
				serviceObject=createServiceObject(srvConf);
			} else if((scope.equals(Constants.SERVICE_SCOPE_SESSION)&&registryType==RegistryType.SESSION)||
					(scope.equals(Constants.SERVICE_SCOPE_APPLICATION)&&registryType==RegistryType.APPLICATION)) {
				synchronized(serviceObjects) {
					serviceObject=serviceObjects.get(serviceName);
					if(serviceObject==null) {
						serviceObject=createServiceObject(srvConf);
						serviceObjects.put(serviceName,serviceObject);
					}
				}
			}
		}
		if(serviceObject==null && subRegistry!=null) serviceObject=subRegistry.getServiceObject(serviceName);
		return serviceObject;
	}
	
	private Object createServiceObject(ServiceConfig srvConf) throws ServiceException {
        try {
            Class clazz=null;
            AppLoader loader=AppLoader.getInstance();
            if(loader.isEnabled()) {
                ClassLoader newLoader=loader.getAppClassLoader();
                clazz=newLoader.loadClass(srvConf.getImplementationName());
            } else {
                clazz=Class.forName(srvConf.getImplementationName());
            }
            Object serviceObject=clazz.newInstance();
            return serviceObject;
		} catch(Exception x) {
			throw new ServiceException("Can't create instance of service '"+srvConf.getName()+"'.",x);
		}
	}
	
}
