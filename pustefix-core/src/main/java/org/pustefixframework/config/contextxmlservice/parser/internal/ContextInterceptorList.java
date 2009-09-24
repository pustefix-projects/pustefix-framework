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

import java.util.AbstractSequentialList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.pustefixframework.extension.ContextInterceptorExtension;
import org.pustefixframework.extension.ContextInterceptorExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

import de.schlund.pfixcore.workflow.ContextInterceptor;

/**
 * List implementation for context interceptors that can resolve extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextInterceptorList extends AbstractSequentialList<ContextInterceptor> {

    private List<ContextInterceptor> cachedList;

    private final Object updateLock = new Object();

    private List<?> contextInterceptorObjects;

    private Throwable cause;

    private ExtensionPointRegistrationListener<ContextInterceptorExtensionPoint, ContextInterceptorExtension> listener = new ExtensionPointRegistrationListener<ContextInterceptorExtensionPoint, ContextInterceptorExtension>() {

        @Override
        public void afterRegisterExtension(ContextInterceptorExtensionPoint extensionPoint, ContextInterceptorExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(ContextInterceptorExtensionPoint extensionPoint, ContextInterceptorExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(ContextInterceptorExtensionPoint extensionPoint, ContextInterceptorExtension extension) {
            updateCache();
        }

    };

    public void setContextInterceptorObjects(List<?> contextInterceptorObjects) {
        synchronized (updateLock) {
            if (this.contextInterceptorObjects != null) {
                for (Object o : this.contextInterceptorObjects) {
                    if (o instanceof ContextInterceptorExtensionPointImpl) {
                        ContextInterceptorExtensionPointImpl extensionPoint = (ContextInterceptorExtensionPointImpl) o;
                        extensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.contextInterceptorObjects = contextInterceptorObjects;
            for (Object o : this.contextInterceptorObjects) {
                if (o instanceof ContextInterceptorExtensionPointImpl) {
                    ContextInterceptorExtensionPointImpl extensionPoint = (ContextInterceptorExtensionPointImpl) o;
                    extensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

    @Override
    public ListIterator<ContextInterceptor> listIterator(int index) {
        return getCachedList().listIterator(index);
    }

    @Override
    public int size() {
        return getCachedList().size();
    }

    private List<ContextInterceptor> getCachedList() {
        synchronized (updateLock) {
            if (cachedList != null) {
                return cachedList;
            } else {
                if (cause != null) {
                    throw new IllegalStateException("Context interceptor list cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Context interceptor list has not been initialized");
                }
            }
        }
    }

    protected void updateCache() {
        synchronized (updateLock) {
            List<ContextInterceptor> list = new LinkedList<ContextInterceptor>();
            try {
                for (Object o : contextInterceptorObjects) {
                    if (o instanceof ContextInterceptor) {
                        ContextInterceptor contextInterceptor = (ContextInterceptor) o;
                        list.add(contextInterceptor);
                    } else if (o instanceof ContextInterceptorExtensionPointImpl) {
                        ContextInterceptorExtensionPointImpl contextInterceptorExtensionPoint = (ContextInterceptorExtensionPointImpl) o;
                        for (ContextInterceptorExtension extension : contextInterceptorExtensionPoint.getExtensions()) {
                            for (ContextInterceptor contextInterceptor : extension.getContextInterceptors()) {
                                list.add(contextInterceptor);
                            }
                        }
                    } else {
                        throw new RuntimeException("contextInterceptorObjects contains object of unsupported type " + o.getClass().getName());
                    }
                }
            } catch (Throwable e) {
                // Save exception for later
                cause = e;
                cachedList = null;
                return;
            }
            // Make sure list is not modified
            cachedList = Collections.unmodifiableList(list);
            // Clean cause
            cause = null;
        }
    }
}
