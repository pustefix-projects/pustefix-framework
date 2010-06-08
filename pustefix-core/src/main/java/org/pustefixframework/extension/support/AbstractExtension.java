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

package org.pustefixframework.extension.support;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pustefixframework.extension.Extension;
import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.util.VersionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;


/**
 * Abstract implementation of generic extension, handling extension
 * registration and deregistration.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class AbstractExtension <T1 extends ExtensionPoint<? super T2>, T2 extends AbstractExtension<T1, T2>> implements Extension, InitializingBean, DisposableBean, BundleContextAware {

    protected String type;
    protected Collection<ExtensionTargetInfo> extensionTargetInfos;
    protected Class<? extends T1> extensionPointType;
    protected final Object registrationLock = new Object();
    protected List<T1> extensionPoints = new LinkedList<T1>();
    protected BundleContext bundleContext;
    protected ExtensionPointServiceTracker serviceTracker;

    public String getType() {
        return type;
    }

    /**
     * Sets the type of the extension. This base implementation ensures
     * that the extension is only registered with extension points of the
     * same type.
     * 
     * @param type type identifier
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the interface type of the extension point, this extension
     * registers at.
     * 
     * @return class type of the extension point for this extension
     */
    public Class<? extends ExtensionPoint<? super T2>> getExtensionPointType() {
        return extensionPointType;
    }

    /**
     * Sets the interface type of the extension point, this extension
     * registers at.
     * 
     * @param extensionPointType class type of the extension point type
     *  for this extension
     */
    public void setExtensionPointType(Class<? extends T1> extensionPointType) {
        this.extensionPointType = extensionPointType;
    }

    /**
     * Returns information about which extension points should be extended
     * by this extension.
     * 
     * @return list of target extension points
     */
    public Collection<ExtensionTargetInfo> getExtensionTargetInfos() {
        return extensionTargetInfos;
    }

    /**
     * Sets the list of extension points that should be extended by this
     * extension.
     * 
     * @param extensionTargetInfos list of target extension points
     */
    public void setExtensionTargetInfos(Collection<ExtensionTargetInfo> extensionTargetInfos) {
        this.extensionTargetInfos = extensionTargetInfos;
    }

    @SuppressWarnings("unchecked")
    private void bind(Object service) {
        if (extensionPointType.isAssignableFrom(service.getClass())) {
            T1 extensionPoint = (T1) service;
            if (!extensionPoint.getType().equals(getType())) {
                return;
            }
            for (ExtensionTargetInfo info : extensionTargetInfos) {
                String extensionPointId = info.getExtensionPoint();
                String extensionPointVersion = info.getVersion();
                if (!extensionPoint.getId().equals(extensionPointId)) {
                    continue;
                }
                if (!VersionUtils.isVersionInRange(extensionPoint.getVersion(), extensionPointVersion)) {
                    continue;
                }
                synchronized (registrationLock) {
                    extensionPoint.registerExtension((T2) this);
                    extensionPoints.add(extensionPoint);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void unbind(Object service) {
        synchronized (registrationLock) {
            if (extensionPoints.contains(service)) {
                ExtensionPoint<T2> extensionPoint = (ExtensionPoint<T2>) service;
                extensionPoints.remove(extensionPoint);
                extensionPoint.unregisterExtension((T2) this);
            }
        }
    }
    
    public void afterPropertiesSet() throws Exception {
        StringBuilder filter = new StringBuilder();
        filter.append("(&");
        filter.append("(objectClass="+extensionPointType.getName()+")");
        filter.append("(|");
        for (ExtensionTargetInfo info : extensionTargetInfos) {
            filter.append("(extension-point="+info.getExtensionPoint()+")");
        }
        filter.append(")");
        filter.append(")");
        serviceTracker = new ExtensionPointServiceTracker(bundleContext.createFilter(filter.toString()));
        serviceTracker.open();
    }

    public void destroy() throws Exception {
        serviceTracker.close();
        serviceTracker = null;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private class ExtensionPointServiceTracker extends ServiceTracker {
        public ExtensionPointServiceTracker(Class<? extends ExtensionPoint<? super T2>> extensionPointType) {
            super(bundleContext, extensionPointType.getName(), null);
        }
        
        public ExtensionPointServiceTracker(Filter filter) {
            super(bundleContext, filter, null);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            Object service = super.addingService(reference);
            bind(service);
            return service;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            unbind(service);
            super.removedService(reference, service);
        }

    }
}
