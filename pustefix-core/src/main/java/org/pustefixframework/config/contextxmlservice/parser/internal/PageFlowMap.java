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

import org.pustefixframework.extension.PageFlowExtension;
import org.pustefixframework.extension.PageFlowExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

import de.schlund.pfixcore.workflow.context.PageFlow;


/**
 * Map mapping page flow names to the corresponding page flow instances.
 * Can be initialized with a mixed list, containing page flow objects
 * as well as page flow extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowMap extends AbstractMap<String, PageFlow> {
    
    private Map<String, PageFlow> cachedMap;
    private final Object updateLock = new Object();
    private List<?> pageFlowObjects;
    private Throwable cause;
    
    private ExtensionPointRegistrationListener<PageFlowExtensionPoint, PageFlowExtension> listener =
        new ExtensionPointRegistrationListener<PageFlowExtensionPoint, PageFlowExtension>() {

            @Override
            public void afterRegisterExtension(PageFlowExtensionPoint extensionPoint, PageFlowExtension extension) {
                updateCache();
            }

            @Override
            public void afterUnregisterExtension(PageFlowExtensionPoint extensionPoint, PageFlowExtension extension) {
                updateCache();
            }

            @Override
            public void updateExtension(PageFlowExtensionPoint extensionPoint, PageFlowExtension extension) {
                updateCache();
            }
    };
    
    @Override
    public Set<java.util.Map.Entry<String, PageFlow>> entrySet() {
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
                cachedMap = new LinkedHashMap<String, PageFlow>();
                for (Object pageFlowObject : pageFlowObjects) {
                    if (pageFlowObject instanceof PageFlow) {
                        PageFlow pageFlow = (PageFlow) pageFlowObject;
                        cachedMap.put(pageFlow.getName(), pageFlow);
                    } else if (pageFlowObject instanceof PageFlowExtensionPointImpl) {
                        PageFlowExtensionPointImpl pageFlowExtensionPoint = (PageFlowExtensionPointImpl) pageFlowObject;
                        for (PageFlowExtension extension : pageFlowExtensionPoint.getExtensions()) {
                            for (PageFlow pageFlow : extension.getPageFlows()) {
                                cachedMap.put(pageFlow.getName(), pageFlow);
                            }
                        }
                    } else {
                        throw new RuntimeException("pageFlowObjects contains object of unsupported type " + pageFlowObject.getClass().getName());
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
     * Sets the list of pageflows. This list may contain
     * {@link PageFlow} and {@link PageFlowExtensionPointImpl} objects.
     * 
     * @param objects list of pageflow objects
     */
    public void setPageFlowObjects(List<?> objects) {
        synchronized (updateLock) {
        if (this.pageFlowObjects != null) {
            for (Object o : pageFlowObjects) {
                if (o instanceof PageFlowExtensionPointImpl) {
                    PageFlowExtensionPointImpl pageFlowExtensionPoint = (PageFlowExtensionPointImpl) o;
                    pageFlowExtensionPoint.unregisterListener(listener);
                }
            }
        }
        this.pageFlowObjects = objects;
        for (Object o : pageFlowObjects) {
            if (o instanceof PageFlowExtensionPointImpl) {
                PageFlowExtensionPointImpl pageFlowExtensionPoint = (PageFlowExtensionPointImpl) o;
                pageFlowExtensionPoint.registerListener(listener);
            }
        }
        updateCache();
        }
    }

}
