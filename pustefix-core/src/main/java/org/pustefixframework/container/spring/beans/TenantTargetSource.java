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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;

import org.springframework.aop.TargetSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import de.schlund.pfixxml.Tenant;


/**
 * TargetSource implementation returning different, tenant-specific target objects of a common base type 
 * or interface depending on the currently set tenant.
 * 
 * The target object beans are configured/set using a map keyed by the tenant names.
 *
 */
public class TenantTargetSource implements TargetSource {

    private Map<String, Object> targetMap;

    @Override
    public Object getTarget() throws Exception {

        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Tenant tenant = (Tenant)attributes.getAttribute("__PFX_TENANT__", RequestAttributes.SCOPE_REQUEST);
        if(tenant == null) {
            throw new Exception("Can't resolve target object because no tenant set");
        }
        Object targetObject = targetMap.get(tenant.getName());
        if(targetObject == null) {
            targetObject = targetMap.get("*");
            if(targetObject == null) {
                throw new Exception("Can't resolve target object for tenant '" + tenant.getName() +"'");
            }
        }
        return targetObject;
    }

    public Class<?> getTargetClass() {

        Set<Class<?>> clazzes = new HashSet<Class<?>>();
        for(Object obj: targetMap.values()) {
            Class<?> clazz = obj.getClass();
            if(Enhancer.isEnhanced(clazz)) {
                clazz = clazz.getSuperclass();
            }
            clazzes.add(obj.getClass());
        }
        return getBaseClass(clazzes.toArray(new Class<?>[clazzes.size()]));

    }

    public boolean isStatic() {
        return false;
    }

    public void releaseTarget(Object target) throws Exception {
        //nothing to do
    }

    public void setTargets(Map<String, Object> targetMap) {
        this.targetMap = targetMap;
    }

    static Class<?> getBaseClass(Class<?>[] classes) {

        if(classes == null || classes.length == 0) {
            return null;
        } else if(classes.length == 1) {
            return classes[0];
        } else {
            Class<?> superClass = classes[0];
            do {
                int i = 1;
                for(; i<classes.length; i++) {
                    if(!superClass.isAssignableFrom(classes[i])) {
                        break;
                    }
                }
                if(i == classes.length) {
                    return superClass;
                }
                Class<?>[] superInterfaces = superClass.getInterfaces();
                for(Class<?> superInterface: superInterfaces) {
                    if(superInterface.getMethods().length > 0) {
                        i = 1;
                        for(; i<classes.length; i++) {
                            if(!superInterface.isAssignableFrom(classes[i])) {
                                break;
                            }
                        }
                        if(i == classes.length) {
                            return superClass;
                        }
                    }
                }
                superClass = superClass.getSuperclass();
            } while(superClass != null);
            return null;
        }
    }

}
