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

import org.pustefixframework.extension.PageFlowStepExtension;
import org.pustefixframework.extension.PageFlowStepExtensionPoint;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;

import de.schlund.pfixcore.workflow.FlowStep;

/**
 * List implementation for page flow steps that can resolve extension points.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowStepList extends AbstractSequentialList<FlowStep> {

    private List<FlowStep> cachedList;

    private final Object updateLock = new Object();

    private List<?> flowStepObjects;

    private Throwable cause;

    private ExtensionPointRegistrationListener<PageFlowStepExtensionPoint, PageFlowStepExtension> listener = new ExtensionPointRegistrationListener<PageFlowStepExtensionPoint, PageFlowStepExtension>() {

        @Override
        public void afterRegisterExtension(PageFlowStepExtensionPoint extensionPoint, PageFlowStepExtension extension) {
            updateCache();
        }

        @Override
        public void afterUnregisterExtension(PageFlowStepExtensionPoint extensionPoint, PageFlowStepExtension extension) {
            updateCache();
        }

        @Override
        public void updateExtension(PageFlowStepExtensionPoint extensionPoint, PageFlowStepExtension extension) {
            updateCache();
        }

    };

    public void setFlowStepObjects(List<?> flowStepObjects) {
        synchronized (updateLock) {
            if (this.flowStepObjects != null) {
                for (Object o : this.flowStepObjects) {
                    if (o instanceof PageFlowStepExtensionPointImpl) {
                        PageFlowStepExtensionPointImpl extensionPoint = (PageFlowStepExtensionPointImpl) o;
                        extensionPoint.unregisterListener(listener);
                    }
                }
            }
            this.flowStepObjects = flowStepObjects;
            for (Object o : this.flowStepObjects) {
                if (o instanceof PageFlowStepExtensionPointImpl) {
                    PageFlowStepExtensionPointImpl extensionPoint = (PageFlowStepExtensionPointImpl) o;
                    extensionPoint.registerListener(listener);
                }
            }
            updateCache();
        }
    }

    @Override
    public ListIterator<FlowStep> listIterator(int index) {
        return getCachedList().listIterator(index);
    }

    @Override
    public int size() {
        return getCachedList().size();
    }

    private List<FlowStep> getCachedList() {
        synchronized (updateLock) {
            if (cachedList != null) {
                return cachedList;
            } else {
                if (cause != null) {
                    throw new IllegalStateException("Flow step list cannot be used as a problem occured during intialization", cause);
                } else {
                    throw new IllegalStateException("Flow step list has not been initialized");
                }
            }
        }
    }

    protected void updateCache() {
        synchronized (updateLock) {
            List<FlowStep> list = new LinkedList<FlowStep>();
            try {
                for (Object o : flowStepObjects) {
                    if (o instanceof FlowStep) {
                        FlowStep flowStep = (FlowStep) o;
                        list.add(flowStep);
                    } else if (o instanceof PageFlowStepExtensionPointImpl) {
                        PageFlowStepExtensionPointImpl pageFlowStepExtensionPoint = (PageFlowStepExtensionPointImpl) o;
                        for (PageFlowStepExtension extension : pageFlowStepExtensionPoint.getExtensions()) {
                            for (FlowStep flowStep : extension.getFlowSteps()) {
                                list.add(flowStep);
                            }
                        }
                    } else {
                        throw new RuntimeException("flowStepObjects contains object of unsupported type " + o.getClass().getName());
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
