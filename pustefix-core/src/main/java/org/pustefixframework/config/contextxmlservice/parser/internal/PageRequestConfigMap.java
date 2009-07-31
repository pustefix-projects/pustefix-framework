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
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.extension.PageRequestConfigExtension;
import org.pustefixframework.extension.PageRequestConfigExtensionPoint;
import org.pustefixframework.extension.PageRequestConfigVariantExtension;
import org.pustefixframework.extension.PageRequestConfigVariantExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;


/**
 * Map mapping page names to the corresponding page configurations.
 * Can be initialized with a mixed list, containing page configuration objects
 * as well as page configuration extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestConfigMap extends AbstractMap<String, PageRequestConfig> {
    
    private Map<String, PageRequestConfig> cachedMap;
    private final Object updateLock = new Object();
    private List<?> pageRequestConfigObjects;
    private Throwable cause;
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ExtensionPointRegistrationListener<PageRequestConfigExtensionPoint, PageRequestConfigExtension> listener =
        new ExtensionPointRegistrationListener<PageRequestConfigExtensionPoint, PageRequestConfigExtension>() {

            @Override
            public void afterRegisterExtension(PageRequestConfigExtensionPoint extensionPoint, PageRequestConfigExtension extension) {
                updateCache();
            }

            @Override
            public void afterUnregisterExtension(PageRequestConfigExtensionPoint extensionPoint, PageRequestConfigExtension extension) {
                updateCache();
            }

            @Override
            public void updateExtension(PageRequestConfigExtensionPoint extensionPoint, PageRequestConfigExtension extension) {
                updateCache();
            }
    };

    private ExtensionPointRegistrationListener<PageRequestConfigVariantExtensionPoint, PageRequestConfigVariantExtension> variantListener =
        new ExtensionPointRegistrationListener<PageRequestConfigVariantExtensionPoint, PageRequestConfigVariantExtension>() {

            @Override
            public void afterRegisterExtension(PageRequestConfigVariantExtensionPoint extensionPoint, PageRequestConfigVariantExtension extension) {
                updateCache();
            }

            @Override
            public void afterUnregisterExtension(PageRequestConfigVariantExtensionPoint extensionPoint, PageRequestConfigVariantExtension extension) {
                updateCache();
            }

            @Override
            public void updateExtension(PageRequestConfigVariantExtensionPoint extensionPoint, PageRequestConfigVariantExtension extension) {
                updateCache();
            }
    };

    @Override
    public Set<java.util.Map.Entry<String, PageRequestConfig>> entrySet() {
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
                cachedMap = new LinkedHashMap<String, PageRequestConfig>();
                for (Object pageRequestObject : pageRequestConfigObjects) {
                    if (pageRequestObject instanceof PageRequestConfig) {
                        PageRequestConfig pageConfig = (PageRequestConfig) pageRequestObject;
                        if (cachedMap.containsKey(pageConfig.getPageName())) {
                            logger.warn("Overwriting page \"" + pageConfig.getPageName() + "\" because it is declared twice.");
                        }
                        cachedMap.put(pageConfig.getPageName(), pageConfig);
                    } else if (pageRequestObject instanceof PageRequestConfigExtensionPointImpl) {
                        PageRequestConfigExtensionPointImpl pageConfigExtensionPoint = (PageRequestConfigExtensionPointImpl) pageRequestObject;
                        for (PageRequestConfigExtension extension : pageConfigExtensionPoint.getExtensions()) {
                            for (PageRequestConfig pageConfig : extension.getPageRequestConfigs()) {
                                if (cachedMap.containsKey(pageConfig.getPageName())) {
                                    logger.warn("Overwriting page \"" + pageConfig.getPageName() + "\" because it is declared twice.");
                                }
                                cachedMap.put(pageConfig.getPageName(), pageConfig);
                            }
                        }
                    } else if (pageRequestObject instanceof PageRequestConfigVariantExtensionPointImpl) {
                        PageRequestConfigVariantExtensionPointImpl pageConfigVariantExtensionPoint = (PageRequestConfigVariantExtensionPointImpl) pageRequestObject;
                        String pageName = pageConfigVariantExtensionPoint.getPageName();
                        for (PageRequestConfigVariantExtension extension : pageConfigVariantExtensionPoint.getExtensions()) {
                            for (PageRequestConfig pageConfig : extension.getPageRequestConfigs()) {
                                if (!pageNameToRootName(pageConfig.getPageName()).equals(pageName) || pageConfig.getPageName().equals(pageName)) {
                                    logger.warn("Ignoring page flow \"" + pageConfig.getPageName() + " as it is not a variant of page flow \"" + pageName + "\".");
                                    continue;
                                }
                                if (cachedMap.containsKey(pageConfig.getPageName())) {
                                    logger.warn("Overwriting page flow \"" + pageConfig.getPageName() + "\" because it is declared twice.");
                                }
                                cachedMap.put(pageConfig.getPageName(), pageConfig);
                            }
                        }
                    } else {
                        throw new RuntimeException("pageRequestConfigObjects contains object of unsupported type " + pageRequestObject.getClass().getName());
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
    public void setPageRequestConfigObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.pageRequestConfigObjects != null) {
                for (Object o : pageRequestConfigObjects) {
                    if (o instanceof PageRequestConfigExtensionPointImpl) {
                        PageRequestConfigExtensionPointImpl pageConfigExtensionPoint = (PageRequestConfigExtensionPointImpl) o;
                        pageConfigExtensionPoint.unregisterListener(listener);
                    } else if (o instanceof PageRequestConfigVariantExtensionPointImpl) {
                        PageRequestConfigVariantExtensionPointImpl pageConfigVariantExtensionPoint = (PageRequestConfigVariantExtensionPointImpl) o;
                        pageConfigVariantExtensionPoint.unregisterListener(variantListener);
                    }
                }
            }
            this.pageRequestConfigObjects = objects;
            for (Object o : pageRequestConfigObjects) {
                if (o instanceof PageRequestConfigExtensionPointImpl) {
                    PageRequestConfigExtensionPointImpl pageConfigExtensionPoint = (PageRequestConfigExtensionPointImpl) o;
                    pageConfigExtensionPoint.registerListener(listener);
                } else if (o instanceof PageRequestConfigVariantExtensionPointImpl) {
                    PageRequestConfigVariantExtensionPointImpl pageConfigVariantExtensionPoint = (PageRequestConfigVariantExtensionPointImpl) o;
                    pageConfigVariantExtensionPoint.registerListener(variantListener);
                }
            }
            updateCache();
        }
    }
    
    private String pageNameToRootName(String pageName) {
        int index = pageName.indexOf("::");
        if (index < 0) {
            return pageName;
        }
        return pageName.substring(index + 2);
    }

}
