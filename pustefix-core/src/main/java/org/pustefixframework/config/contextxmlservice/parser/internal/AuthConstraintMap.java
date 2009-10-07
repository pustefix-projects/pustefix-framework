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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pustefixframework.extension.AuthConstraintExtension;
import org.pustefixframework.extension.AuthConstraintExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

import de.schlund.pfixcore.auth.AuthConstraint;

/**
 * Map mapping authconstraint names to authconstraint instances.
 * Can be initialized with a mixed list, containing authconstraint 
 * objects as well as authconstraint extension points.  
 * 
 * @author mleidig@schlund.de
 */
public class AuthConstraintMap extends AbstractMap<String, AuthConstraint> {

    private Map<String, AuthConstraint> cachedMap;
    private AuthConstraint defaultAuthConstraint;

    private final Object updateLock = new Object();

    private List<?> authConstraintObjects;

    private Throwable cause;

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ExtensionPointRegistrationListener<AuthConstraintExtensionPoint, AuthConstraintExtension> listener = new ExtensionPointRegistrationListener<AuthConstraintExtensionPoint, AuthConstraintExtension>() {

        @Override
        public void afterRegisterExtension(AuthConstraintExtensionPoint extensionPoint, AuthConstraintExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(AuthConstraintExtensionPoint extensionPoint, AuthConstraintExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(AuthConstraintExtensionPoint extensionPoint, AuthConstraintExtension extension) {
            updateCache();
        }
    };
    
    public AuthConstraint getDefaultAuthConstraint() {
    	return defaultAuthConstraint;
    }

    @Override
    public Set<java.util.Map.Entry<String, AuthConstraint>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("Authconstraint map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Authconstraint map has not been initialized");
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
                cachedMap = new LinkedHashMap<String, AuthConstraint>();
                defaultAuthConstraint = null;
                for (Object authConstraintObject : authConstraintObjects) {
                    if (authConstraintObject instanceof AuthConstraint) {
                        AuthConstraint authConstraint = (AuthConstraint) authConstraintObject;
                        if (cachedMap.containsKey(authConstraint.getId())) {
                            logger.warn("Overwriting authconstraint \"" + authConstraint.getId() + "\" because it is declared twice.");
                        }
                        cachedMap.put(authConstraint.getId(), authConstraint);
                        if(authConstraint.isDefault()) {
                        	if(defaultAuthConstraint != null) {
                        		logger.warn("Overwriting default authconstraint '" + defaultAuthConstraint.getId() +"' with '" + authConstraint.getId() +"'");
                        	}
                        	defaultAuthConstraint = authConstraint;
                        }
                    } else if (authConstraintObject instanceof AuthConstraintExtensionPointImpl) {
                        AuthConstraintExtensionPointImpl authConstraintExtensionPoint = (AuthConstraintExtensionPointImpl) authConstraintObject;
                        for (AuthConstraintExtension extension : authConstraintExtensionPoint.getExtensions()) {
                            for (AuthConstraint authConstraint : extension.getAuthConstraints()) {
                                if (cachedMap.containsKey(authConstraint.getId())) {
                                    logger.warn("Overwriting authconstraint \"" + authConstraint.getId() + "\" because it is declared twice.");
                                }
                                cachedMap.put(authConstraint.getId(), authConstraint);
                                if(authConstraint.isDefault()) {
                                	if(defaultAuthConstraint != null) {
                                		logger.warn("Overwriting default authconstraint '" + defaultAuthConstraint.getId() +"' with '" + authConstraint.getId() +"'");
                                	}
                                	defaultAuthConstraint = authConstraint;
                                }
                            }
                        }
                    } else {
                        throw new RuntimeException("authConstraintObjects contains object of unsupported type " + authConstraintObject.getClass().getName());
                    }
                }
            } catch (Throwable e) {
                // Store exception for later use
                cachedMap = null;
                cause = e;
                return;
            }
            // Make sure map is not modified
            cachedMap = Collections.unmodifiableMap(cachedMap);
            // If update was successful, delete cause
            cause = null;
        }
    }

    /**
     * Sets the list of authconstraints. This list may contain
     * {@link AuthConstraint} and {@link AuthConstraintExtensionPointImpl} 
     * objects.
     * 
     * @param objects list of authconstraint objects
     */
    public void setAuthConstraintObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.authConstraintObjects != null) {
                for (Object o : authConstraintObjects) {
                    if (o instanceof AuthConstraintExtensionPointImpl) {
                        AuthConstraintExtensionPointImpl authConstraintExtensionPoint = (AuthConstraintExtensionPointImpl) o;
                        authConstraintExtensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.authConstraintObjects = objects;
            for (Object o : authConstraintObjects) {
                if (o instanceof AuthConstraintExtensionPointImpl) {
                    AuthConstraintExtensionPointImpl authConstraintExtensionPoint = (AuthConstraintExtensionPointImpl) o;
                    authConstraintExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
