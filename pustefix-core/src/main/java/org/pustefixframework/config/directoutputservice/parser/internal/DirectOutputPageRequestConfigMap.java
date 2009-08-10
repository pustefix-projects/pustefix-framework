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

package org.pustefixframework.config.directoutputservice.parser.internal;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigVariantExtensionPointImpl;
import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfig;
import org.pustefixframework.extension.DirectOutputPageRequestConfigExtension;
import org.pustefixframework.extension.DirectOutputPageRequestConfigExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

/**
 * Map mapping page names to the corresponding direct output page 
 * configurations.
 * Can be initialized with a mixed list, containing page configuration objects
 * as well as page configuration extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DirectOutputPageRequestConfigMap extends AbstractMap<String, DirectOutputPageRequestConfig> {

    private Map<String, DirectOutputPageRequestConfig> cachedMap;

    private final Object updateLock = new Object();

    private List<?> directOutputPageRequestConfigObjects;

    private Throwable cause;

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ExtensionPointRegistrationListener<DirectOutputPageRequestConfigExtensionPoint, DirectOutputPageRequestConfigExtension> listener = new ExtensionPointRegistrationListener<DirectOutputPageRequestConfigExtensionPoint, DirectOutputPageRequestConfigExtension>() {

        @Override
        public void afterRegisterExtension(DirectOutputPageRequestConfigExtensionPoint extensionPoint, DirectOutputPageRequestConfigExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(DirectOutputPageRequestConfigExtensionPoint extensionPoint, DirectOutputPageRequestConfigExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(DirectOutputPageRequestConfigExtensionPoint extensionPoint, DirectOutputPageRequestConfigExtension extension) {
            updateCache();
        }
    };

    @Override
    public Set<java.util.Map.Entry<String, DirectOutputPageRequestConfig>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("Page flow map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Page flow map has not been initialized");
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
                cachedMap = new LinkedHashMap<String, DirectOutputPageRequestConfig>();
                for (Object directOutputPageRequestObject : directOutputPageRequestConfigObjects) {
                    if (directOutputPageRequestObject instanceof DirectOutputPageRequestConfig) {
                        DirectOutputPageRequestConfig pageConfig = (DirectOutputPageRequestConfig) directOutputPageRequestObject;
                        if (cachedMap.containsKey(pageConfig.getPageName())) {
                            logger.warn("Overwriting page \"" + pageConfig.getPageName() + "\" because it is declared twice.");
                        }
                        cachedMap.put(pageConfig.getPageName(), pageConfig);
                    } else if (directOutputPageRequestObject instanceof DirectOutputPageRequestConfigExtensionPointImpl) {
                        DirectOutputPageRequestConfigExtensionPointImpl directOutputPageConfigExtensionPoint = (DirectOutputPageRequestConfigExtensionPointImpl) directOutputPageRequestObject;
                        for (DirectOutputPageRequestConfigExtension extension : directOutputPageConfigExtensionPoint.getExtensions()) {
                            for (DirectOutputPageRequestConfig pageConfig : extension.getDirectOutputPageRequestConfigs()) {
                                if (cachedMap.containsKey(pageConfig.getPageName())) {
                                    logger.warn("Overwriting page \"" + pageConfig.getPageName() + "\" because it is declared twice.");
                                }
                                cachedMap.put(pageConfig.getPageName(), pageConfig);
                            }
                        }
                    } else {
                        throw new RuntimeException("directOutputPageRequestConfigObjects contains object of unsupported type " + directOutputPageRequestObject.getClass().getName());
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
     * Sets the list of page configurations. This list may contain
     * {@link PageRequestConfig}, {@link PageRequestConfigExtensionPointImpl} 
     * and {@link PageRequestConfigVariantExtensionPointImpl} objects.
     * 
     * @param objects list of page configuration objects
     */
    public void setDirectOutputPageRequestConfigObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.directOutputPageRequestConfigObjects != null) {
                for (Object o : directOutputPageRequestConfigObjects) {
                    if (o instanceof DirectOutputPageRequestConfigExtensionPointImpl) {
                        DirectOutputPageRequestConfigExtensionPointImpl directOutputPageConfigExtensionPoint = (DirectOutputPageRequestConfigExtensionPointImpl) o;
                        directOutputPageConfigExtensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.directOutputPageRequestConfigObjects = objects;
            for (Object o : directOutputPageRequestConfigObjects) {
                if (o instanceof DirectOutputPageRequestConfigExtensionPointImpl) {
                    DirectOutputPageRequestConfigExtensionPointImpl directOutputPageConfigExtensionPoint = (DirectOutputPageRequestConfigExtensionPointImpl) o;
                    directOutputPageConfigExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
