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
import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.extension.PageRequestIWrapperConfigExtension;
import org.pustefixframework.extension.PageRequestIWrapperConfigExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;


/**
 * Map mapping page names to the corresponding page configurations.
 * Can be initialized with a mixed list, containing page configuration objects
 * as well as page configuration extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IWrapperConfigMap extends AbstractMap<String, IWrapperConfig> {
    
    private Map<String, IWrapperConfig> cachedMap;
    private final Object updateLock = new Object();
    private List<?> iWrapperConfigObjects;
    private Throwable cause;
    private List<IWrapperConfigMapChangeListener> changeListeners = new ArrayList<IWrapperConfigMapChangeListener>();
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ExtensionPointRegistrationListener<PageRequestIWrapperConfigExtensionPoint, PageRequestIWrapperConfigExtension> listener =
        new ExtensionPointRegistrationListener<PageRequestIWrapperConfigExtensionPoint, PageRequestIWrapperConfigExtension>() {

            @Override
            public void afterRegisterExtension(PageRequestIWrapperConfigExtensionPoint extensionPoint, PageRequestIWrapperConfigExtension extension) {
                updateCache();
            }

            @Override
            public void afterUnregisterExtension(PageRequestIWrapperConfigExtensionPoint extensionPoint, PageRequestIWrapperConfigExtension extension) {
                updateCache();
            }

            @Override
            public void updateExtension(PageRequestIWrapperConfigExtensionPoint extensionPoint, PageRequestIWrapperConfigExtension extension) {
                updateCache();
            }
    };

    public void addChangeListener(IWrapperConfigMapChangeListener listener) {
        changeListeners.add(listener);
    }
    
    public boolean removeChangeListener(IWrapperConfigMapChangeListener listener) {
        return changeListeners.remove(listener);
    }
    
    @Override
    public Set<java.util.Map.Entry<String, IWrapperConfig>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("IWrapper config map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("IWrapper config map has not been initialized");
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
                cachedMap = new LinkedHashMap<String, IWrapperConfig>();
                for (Object iWrapperObject : iWrapperConfigObjects) {
                    if (iWrapperObject instanceof IWrapperConfig) {
                        IWrapperConfig iWrapperConfig = (IWrapperConfig) iWrapperObject;
                        if (cachedMap.containsKey(iWrapperConfig.getPrefix())) {
                            logger.warn("Overwriting IWrapper with prefix \"" + iWrapperConfig.getPrefix() + "\" because the prefix is used twice.");
                        }
                        cachedMap.put(iWrapperConfig.getPrefix(), iWrapperConfig);
                    } else if (iWrapperObject instanceof PageRequestIWrapperConfigExtensionPointImpl) {
                        PageRequestIWrapperConfigExtensionPointImpl iWrapperConfigExtensionPoint = (PageRequestIWrapperConfigExtensionPointImpl) iWrapperObject;
                        for (PageRequestIWrapperConfigExtension extension : iWrapperConfigExtensionPoint.getExtensions()) {
                            for (IWrapperConfig iWrapperConfig : extension.getIWrapperConfigs()) {
                                if (cachedMap.containsKey(iWrapperConfig.getPrefix())) {
                                    logger.warn("Overwriting IWrapper with prefix \"" + iWrapperConfig.getPrefix() + "\" because the prefix is used twice.");
                                }
                                cachedMap.put(iWrapperConfig.getPrefix(), iWrapperConfig);
                            }
                        }
                    } else {
                        throw new RuntimeException("iWrapperConfigObjects contains object of unsupported type " + iWrapperObject.getClass().getName());
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
            
            for(IWrapperConfigMapChangeListener listener: changeListeners) {
                listener.iwrapperConfigMapChanged();
            }
        }
    }

    /**
     * Sets the list of IWrapper configurations. This list may contain
     * {@link IWrapperConfig} and 
     * {@link PageRequestIWrapperConfigExtensionPointImpl} objects. 
     * 
     * @param objects list of IWrapper configuration objects
     */
    public void setIWrapperConfigObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.iWrapperConfigObjects != null) {
                for (Object o : iWrapperConfigObjects) {
                    if (o instanceof PageRequestIWrapperConfigExtensionPointImpl) {
                        PageRequestIWrapperConfigExtensionPointImpl iWrapperConfigExtensionPoint = (PageRequestIWrapperConfigExtensionPointImpl) o;
                        iWrapperConfigExtensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.iWrapperConfigObjects = objects;
            for (Object o : iWrapperConfigObjects) {
                if (o instanceof PageRequestIWrapperConfigExtensionPointImpl) {
                    PageRequestIWrapperConfigExtensionPointImpl iWrapperConfigExtensionPoint = (PageRequestIWrapperConfigExtensionPointImpl) o;
                    iWrapperConfigExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
