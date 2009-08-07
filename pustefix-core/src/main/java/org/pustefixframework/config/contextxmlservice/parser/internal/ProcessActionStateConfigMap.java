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
import org.pustefixframework.config.contextxmlservice.ProcessActionStateConfig;
import org.pustefixframework.extension.PageRequestProcessActionConfigExtension;
import org.pustefixframework.extension.PageRequestProcessActionConfigExtensionPoint;
import org.pustefixframework.extension.PageRequestProcessActionConfigExtension.ProcessActionConfig;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

/**
 * Map mapping process action names to the corresponding process action state 
 * configurations.
 * Can be initialized with a mixed list, containing process action state 
 * configuration objects as well as process action configuration extension 
 * points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProcessActionStateConfigMap extends AbstractMap<String, ProcessActionStateConfig> {

    private Map<String, ProcessActionStateConfig> cachedMap;

    private final Object updateLock = new Object();

    private List<?> processActionConfigObjects;

    private Throwable cause;

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ExtensionPointRegistrationListener<PageRequestProcessActionConfigExtensionPoint, PageRequestProcessActionConfigExtension> listener = new ExtensionPointRegistrationListener<PageRequestProcessActionConfigExtensionPoint, PageRequestProcessActionConfigExtension>() {

        @Override
        public void afterRegisterExtension(PageRequestProcessActionConfigExtensionPoint extensionPoint, PageRequestProcessActionConfigExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(PageRequestProcessActionConfigExtensionPoint extensionPoint, PageRequestProcessActionConfigExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(PageRequestProcessActionConfigExtensionPoint extensionPoint, PageRequestProcessActionConfigExtension extension) {
            updateCache();
        }
    };

    @Override
    public Set<java.util.Map.Entry<String, ProcessActionStateConfig>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("Process action config map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Process action config map has not been initialized");
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
                cachedMap = new LinkedHashMap<String, ProcessActionStateConfig>();
                for (Object processActionObject : processActionConfigObjects) {
                    if (processActionObject instanceof ProcessActionStateConfig) {
                        ProcessActionStateConfig processActionStateConfig = (ProcessActionStateConfig) processActionObject;
                        if (cachedMap.containsKey(processActionStateConfig.getName())) {
                            logger.warn("Overwriting process action with name \"" + processActionStateConfig.getName() + "\" because the name is used twice.");
                        }
                        cachedMap.put(processActionStateConfig.getName(), processActionStateConfig);
                    } else if (processActionObject instanceof PageRequestProcessActionConfigExtensionPointImpl) {
                        PageRequestProcessActionConfigExtensionPointImpl processActionConfigExtensionPoint = (PageRequestProcessActionConfigExtensionPointImpl) processActionObject;
                        for (PageRequestProcessActionConfigExtension extension : processActionConfigExtensionPoint.getExtensions()) {
                            for (ProcessActionConfig processActionConfig : extension.getProcessActionConfigs()) {
                                if (cachedMap.containsKey(processActionConfig.getName())) {
                                    logger.warn("Overwriting process action with name \"" + processActionConfig.getName() + "\" because the name is used twice.");
                                }
                                cachedMap.put(processActionConfig.getName(), processActionConfig.getProcessActionStateConfig());
                            }
                        }
                    } else {
                        throw new RuntimeException("processActionConfigObjects contains object of unsupported type " + processActionObject.getClass().getName());
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
     * Sets the list of process action configurations. This list may contain
     * {@link ProcessActionStateConfig} and 
     * {@link PageRequestProcessActionConfigExtensionPointImpl} objects. 
     * 
     * @param objects list of process action configuration objects
     */
    public void setProcessActionConfigObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.processActionConfigObjects != null) {
                for (Object o : processActionConfigObjects) {
                    if (o instanceof PageRequestProcessActionConfigExtensionPointImpl) {
                        PageRequestProcessActionConfigExtensionPointImpl processActionConfigExtensionPoint = (PageRequestProcessActionConfigExtensionPointImpl) o;
                        processActionConfigExtensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.processActionConfigObjects = objects;
            for (Object o : processActionConfigObjects) {
                if (o instanceof PageRequestProcessActionConfigExtensionPointImpl) {
                    PageRequestProcessActionConfigExtensionPointImpl processActionConfigExtensionPoint = (PageRequestProcessActionConfigExtensionPointImpl) o;
                    processActionConfigExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
