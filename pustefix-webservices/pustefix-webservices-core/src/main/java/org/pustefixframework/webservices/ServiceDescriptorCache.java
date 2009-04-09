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
 *
 */

package org.pustefixframework.webservices;

import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.webservices.config.ServiceConfig;


/**
 * @author mleidig@schlund.de
 */
public class ServiceDescriptorCache {

    Map<Class<?>,ServiceDescriptor> srvDescs;
    
    public ServiceDescriptorCache() {
        srvDescs=new HashMap<Class<?>,ServiceDescriptor>();
    }
    
    public void clear() {
        srvDescs.clear();
    }
    
    public ServiceDescriptor getServiceDescriptor(Class<?> clazz) throws ServiceException {
        ServiceDescriptor srvDesc=new ServiceDescriptor(clazz);
        synchronized(srvDescs) {
            srvDescs.put(clazz,srvDesc);
        }
        return srvDesc;
    }
    
    public ServiceDescriptor getServiceDescriptor(ServiceConfig config) throws ServiceException {
        ServiceDescriptor srvDesc=new ServiceDescriptor(config);
        synchronized(srvDescs) {
            srvDescs.put(srvDesc.getServiceClass(),srvDesc);
        }
        return srvDesc;
    }
    
}
