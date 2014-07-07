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

package org.pustefixframework.container.spring.beans;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import de.schlund.pfixxml.Tenant;

/**
 * Tenant-aware Spring scope implementation. 
 * 
 * Giving a Spring bean tenant scope, will mean that an own instance of this bean will be created 
 * and managed for each tenant. Injecting the bean as dependency will work similar to session-scoped 
 * beans: Spring will inject a proxy bean, which will delegate method calls to the appropriate instance 
 * associated with the current tenant.
 *
 */
public class TenantScope implements Scope, DisposableBean {

    public static final String REQUEST_ATTRIBUTE_TENANT = "__PFX_TENANT__";
    
    private final ConcurrentHashMap<String, Object> scopedObjects = new ConcurrentHashMap<String, Object>();
    private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<String, Runnable>();

    public Object get(String name, ObjectFactory<?> objectFactory) {            

        String key = getTenantKey(name);
        Object scopedObject = scopedObjects.get(key);
        if(scopedObject == null) {
            synchronized(scopedObjects) {
                Object tmp = scopedObjects.get(key);
                if(tmp == null) {
                    tmp = objectFactory.getObject();
                    scopedObjects.put(key, tmp);
                }
                scopedObject = tmp;
            }
        }
        return scopedObject;

    }

    public String getConversationId() {

        return null;
    }

    public void registerDestructionCallback(String name, Runnable callback) {

        synchronized(destructionCallbacks) {
            destructionCallbacks.put(name, callback);
        }
    }

    public Object remove(String name) {

        String key = getTenantKey(name);
        return scopedObjects.remove(key);
    }

    public Object resolveContextualObject(String key) {

        return null;
    }

    public void destroy() throws Exception {

        synchronized(destructionCallbacks) {
            for(Runnable runnable: destructionCallbacks.values()) {
                runnable.run();
            }
            this.destructionCallbacks.clear();
        }
    }

    private String getTenantKey(String name) {

        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Tenant tenant = (Tenant)attributes.getAttribute(REQUEST_ATTRIBUTE_TENANT, RequestAttributes.SCOPE_REQUEST);
        if(tenant == null) {
            return name;
        } else {
            return tenant.getName() + ":" + name;
        }
    }

}
