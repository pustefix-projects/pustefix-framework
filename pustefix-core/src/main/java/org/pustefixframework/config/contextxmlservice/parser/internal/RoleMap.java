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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pustefixframework.extension.RoleExtension;
import org.pustefixframework.extension.RoleExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.auth.RoleNotFoundException;
import de.schlund.pfixcore.auth.RoleProvider;

/**
 * Map mapping role names to role instances.
 * Can be initialized with a mixed list, containing role 
 * objects as well as role extension points.  
 * 
 * @author mleidig@schlund.de
 */
public class RoleMap extends AbstractMap<String, Role> implements RoleProvider {

    private Map<String, Role> cachedMap;
    private List<Role> cachedList;
    
    private final Object updateLock = new Object();

    private List<?> roleObjects;

    private Throwable cause;

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ExtensionPointRegistrationListener<RoleExtensionPoint, RoleExtension> listener = new ExtensionPointRegistrationListener<RoleExtensionPoint, RoleExtension>() {

        @Override
        public void afterRegisterExtension(RoleExtensionPoint extensionPoint, RoleExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(RoleExtensionPoint extensionPoint, RoleExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(RoleExtensionPoint extensionPoint, RoleExtension extension) {
            updateCache();
        }
    };

    public Role getRole(String roleName) throws RoleNotFoundException {
    	if(cachedMap == null) return null;
    	return cachedMap.get(roleName);
    }
    
    public void addRole(Role role) {
    	throw new RuntimeException("Method not supported");
    }
    
    public List<Role> getRoles() {
    	if(cachedList == null) return new ArrayList<Role>(0);
    	return cachedList;
    }
    
    @Override
    public Set<java.util.Map.Entry<String, Role>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("Role map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Role map has not been initialized");
                }
            }
            // cachedMap is unmodifiable, therefore iterators
            // on this map are save.
            return cachedMap.entrySet();
        }
    }

    protected void updateCache() {
        synchronized (updateLock) {
            try {
                cachedMap = new LinkedHashMap<String, Role>();
                cachedList = new ArrayList<Role>();
                for (Object roleObject : roleObjects) {
                    if (roleObject instanceof Role) {
                        Role role = (Role) roleObject;
                        if (cachedMap.containsKey(role.getName())) {
                            logger.warn("Overwriting role \"" + role.getName() + "\" because it is declared twice.");
                        }
                        cachedMap.put(role.getName(), role);
                        cachedList.add(role);
                    } else if (roleObject instanceof RoleExtensionPointImpl) {
                        RoleExtensionPointImpl roleExtensionPoint = (RoleExtensionPointImpl) roleObject;
                        for (RoleExtension extension : roleExtensionPoint.getExtensions()) {
                            for (Role role : extension.getRoles()) {
                                if (cachedMap.containsKey(role.getName())) {
                                    logger.warn("Overwriting role \"" + role.getName() + "\" because it is declared twice.");
                                }
                                cachedMap.put(role.getName(), role);
                                cachedList.add(role);
                            }
                        }
                    } else {
                        throw new RuntimeException("roleObjects contains object of unsupported type " + roleObject.getClass().getName());
                    }
                }
            } catch (Throwable e) {
                // Store exception for later use
                cachedMap = null;
                cachedList = null;
                cause = e;
                return;
            }
            // Make sure map is not modified
            cachedMap = Collections.unmodifiableMap(cachedMap);
            cachedList = Collections.unmodifiableList(cachedList);
            // If update was successful, delete cause
            cause = null;
        }
    }

    /**
     * Sets the list of roles. This list may contain
     * {@link Role} and {@link RoleExtensionPointImpl} 
     * objects.
     * 
     * @param objects list of role objects
     */
    public void setRoleObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.roleObjects != null) {
                for (Object o : roleObjects) {
                    if (o instanceof RoleExtensionPointImpl) {
                        RoleExtensionPointImpl roleExtensionPoint = (RoleExtensionPointImpl) o;
                        roleExtensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.roleObjects = objects;
            for (Object o : roleObjects) {
                if (o instanceof RoleExtensionPointImpl) {
                    RoleExtensionPointImpl roleExtensionPoint = (RoleExtensionPointImpl) o;
                    roleExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
