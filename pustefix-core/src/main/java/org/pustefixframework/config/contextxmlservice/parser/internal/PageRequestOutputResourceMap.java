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
import org.pustefixframework.extension.PageRequestOutputResourceExtension;
import org.pustefixframework.extension.PageRequestOutputResourceExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

/**
 * Map mapping output resource node names to the corresponding objects. 
 * Can be initialized with a map containing node name to output resource
 * mappings and a list of extension points.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestOutputResourceMap extends AbstractMap<String, Object> {

    private Map<String, Object> cachedMap;

    private final Object updateLock = new Object();

    private Map<String, ?> outputResources;

    private List<PageRequestOutputResourceExtensionPointImpl> outputResourceExtensionPoints;

    private Throwable cause;

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ExtensionPointRegistrationListener<PageRequestOutputResourceExtensionPoint, PageRequestOutputResourceExtension> listener = new ExtensionPointRegistrationListener<PageRequestOutputResourceExtensionPoint, PageRequestOutputResourceExtension>() {

        @Override
        public void afterRegisterExtension(PageRequestOutputResourceExtensionPoint extensionPoint, PageRequestOutputResourceExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(PageRequestOutputResourceExtensionPoint extensionPoint, PageRequestOutputResourceExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(PageRequestOutputResourceExtensionPoint extensionPoint, PageRequestOutputResourceExtension extension) {
            updateCache();
        }
    };

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("Output resource map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Output resource map has not been initialized");
                }
            }
            // cachedMap is unmodifiable, therefore iterators
            // on this map are thread-safe.
            return cachedMap.entrySet();
        }
    }

    protected void updateCache() {
        synchronized (updateLock) {
            try {
                cachedMap = new LinkedHashMap<String, Object>(outputResources);
                for (PageRequestOutputResourceExtensionPointImpl outputResourceExtensionPoint : outputResourceExtensionPoints) {
                    for (PageRequestOutputResourceExtension extension : outputResourceExtensionPoint.getExtensions()) {
                        for (Entry<String, ?> outputResourceEntry : extension.getOutputResources().entrySet()) {
                            if (cachedMap.containsKey(outputResourceEntry.getKey())) {
                                logger.warn("Overwriting output resource with node name \"" + outputResourceEntry.getKey() + "\" because the node name is used twice.");
                            }
                            cachedMap.put(outputResourceEntry.getKey(), outputResourceEntry.getValue());
                        }
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
     * Sets the map of output resources.
     * 
     * @param map mapping node names to output resources
     */
    public void setOutputResources(Map<String, ?> outputResources) {
        synchronized (updateLock) {
            this.outputResources = outputResources;
            updateCache();
        }
    }

    public void setOutputResourceExtensionPoints(List<PageRequestOutputResourceExtensionPointImpl> extensionPoints) {
        synchronized (updateLock) {
            if (this.outputResourceExtensionPoints != null) {
                for (PageRequestOutputResourceExtensionPointImpl outputResourceExtensionPoint : outputResourceExtensionPoints) {
                    outputResourceExtensionPoint.unregisterListener(listener);
                }
            }
            this.outputResourceExtensionPoints = extensionPoints;
            if (this.outputResourceExtensionPoints != null) {
                for (PageRequestOutputResourceExtensionPointImpl outputResourceExtensionPoint : outputResourceExtensionPoints) {
                    outputResourceExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
