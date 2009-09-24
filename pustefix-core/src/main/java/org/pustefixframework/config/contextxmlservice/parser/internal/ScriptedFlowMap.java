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
import org.pustefixframework.config.contextxmlservice.ScriptedFlowProvider;
import org.pustefixframework.extension.ScriptedFlowExtension;
import org.pustefixframework.extension.ScriptedFlowExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

/**
 * Map mapping scripted flow names to the corresponding script provider.
 * Can be initialized with a mixed list, containing scripted flow provider 
 * objects as well as scripted flow extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ScriptedFlowMap extends AbstractMap<String, ScriptedFlowProvider> {

    private Map<String, ScriptedFlowProvider> cachedMap;

    private final Object updateLock = new Object();

    private List<?> scriptedFlowObjects;

    private Throwable cause;

    protected final Log logger = LogFactory.getLog(this.getClass());

    private ExtensionPointRegistrationListener<ScriptedFlowExtensionPoint, ScriptedFlowExtension> listener = new ExtensionPointRegistrationListener<ScriptedFlowExtensionPoint, ScriptedFlowExtension>() {

        @Override
        public void afterRegisterExtension(ScriptedFlowExtensionPoint extensionPoint, ScriptedFlowExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(ScriptedFlowExtensionPoint extensionPoint, ScriptedFlowExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(ScriptedFlowExtensionPoint extensionPoint, ScriptedFlowExtension extension) {
            updateCache();
        }
    };

    @Override
    public Set<java.util.Map.Entry<String, ScriptedFlowProvider>> entrySet() {
        synchronized (updateLock) {
            if (cachedMap == null) {
                if (cause != null) {
                    throw new IllegalStateException("Scripted flow map cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Scripted flow map has not been initialized");
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
                cachedMap = new LinkedHashMap<String, ScriptedFlowProvider>();
                for (Object scriptedFlowObject : scriptedFlowObjects) {
                    if (scriptedFlowObject instanceof ScriptedFlowProvider) {
                        ScriptedFlowProvider scriptedFlowProvider = (ScriptedFlowProvider) scriptedFlowObject;
                        if (cachedMap.containsKey(scriptedFlowProvider.getName())) {
                            logger.warn("Overwriting scripted flow \"" + scriptedFlowProvider.getName() + "\" because it is declared twice.");
                        }
                        cachedMap.put(scriptedFlowProvider.getName(), scriptedFlowProvider);
                    } else if (scriptedFlowObject instanceof ScriptedFlowExtensionPointImpl) {
                        ScriptedFlowExtensionPointImpl scriptedFlowExtensionPoint = (ScriptedFlowExtensionPointImpl) scriptedFlowObject;
                        for (ScriptedFlowExtension extension : scriptedFlowExtensionPoint.getExtensions()) {
                            for (ScriptedFlowProvider scriptedFlowProvider : extension.getScriptedFlows()) {
                                if (cachedMap.containsKey(scriptedFlowProvider.getName())) {
                                    logger.warn("Overwriting scripted flow \"" + scriptedFlowProvider.getName() + "\" because it is declared twice.");
                                }
                                cachedMap.put(scriptedFlowProvider.getName(), scriptedFlowProvider);
                            }
                        }
                    } else {
                        throw new RuntimeException("scriptedFlowObjects contains object of unsupported type " + scriptedFlowObject.getClass().getName());
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
     * Sets the list of scripted flow providers. This list may contain
     * {@link ScriptedFlowProvider} and {@link ScriptedFlowExtensionPointImpl} 
     * objects.
     * 
     * @param objects list of scripted flow objects
     */
    public void setScriptedFlowObjects(List<?> objects) {
        synchronized (updateLock) {
            if (this.scriptedFlowObjects != null) {
                for (Object o : scriptedFlowObjects) {
                    if (o instanceof ScriptedFlowExtensionPointImpl) {
                        ScriptedFlowExtensionPointImpl scriptedFlowExtensionPoint = (ScriptedFlowExtensionPointImpl) o;
                        scriptedFlowExtensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.scriptedFlowObjects = objects;
            for (Object o : scriptedFlowObjects) {
                if (o instanceof ScriptedFlowExtensionPointImpl) {
                    ScriptedFlowExtensionPointImpl scriptedFlowExtensionPoint = (ScriptedFlowExtensionPointImpl) o;
                    scriptedFlowExtensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

}
