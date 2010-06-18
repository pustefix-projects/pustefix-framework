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

package org.pustefixframework.container.spring.extender;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.pustefixframework.container.spring.beans.PustefixOsgiApplicationContext;
import org.pustefixframework.container.spring.beans.PustefixOsgiWebApplicationContext;
import org.pustefixframework.util.PustefixBundleDetection;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.extender.OsgiApplicationContextCreator;

/**
 * Creates a Pustefix compatible Spring ApplicationContext.
 * Delegates to another implementation if bundle is not a 
 * Pustefix web application bundle.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see PustefixOsgiWebApplicationContext
 * @see PustefixOsgiApplicationContext
 */
public class PustefixApplicationContextCreator implements OsgiApplicationContextCreator {
    
    private OsgiApplicationContextCreator defaultApplicationContextCreator;
    
    /**
     * Creates an OSGi aware ApplicationContext for the specified bundle.
     * 
     * @param bundleContext context of the bundle to create an 
     *        ApplicationContext for
     */
    public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext) throws Exception {
        Bundle bundle = bundleContext.getBundle();
        
        if (PustefixBundleDetection.isPustefixApplication(bundle)) {
            String configLocation = "osgibundle:/" + PustefixBundleDetection.PUSTEFIX_CONFIG_PATH + "/" + PustefixBundleDetection.PUSTEFIX_CONFIG_FILE_APPLICATION;
            PustefixOsgiWebApplicationContext appContext = new PustefixOsgiWebApplicationContext();
            appContext.setBundleContext(bundleContext);
            // ??? appContext.setNamespace(getNamespace());
            appContext.setConfigLocation(configLocation);
            // appContext.refresh();
            return appContext;
        } else if (PustefixBundleDetection.isPustefixModule(bundle)) {
            String configLocation = "osgibundle:/" + PustefixBundleDetection.PUSTEFIX_CONFIG_PATH + "/" + PustefixBundleDetection.PUSTEFIX_CONFIG_FILE_MODULE;
            PustefixOsgiApplicationContext appContext = new PustefixOsgiApplicationContext();
            appContext.setBundleContext(bundleContext);
            // ??? appContext.setNamespace(getNamespace());
            appContext.setConfigLocations(new String[] {configLocation});
            // appContext.refresh();
            return appContext;
        } else {
            return defaultApplicationContextCreator.createApplicationContext(bundleContext);
        }
    }
    
    /**
     * Sets the default implementation of the OsgiApplicationContextCreator.
     * This implementation is used as a fallback for bundles that are not 
     * Pustefix web application bundles.
     * 
     * @param creator default implementation to use as a fallback
     */
    public void setDefaultApplicationContextCreator(OsgiApplicationContextCreator creator) {
        this.defaultApplicationContextCreator = creator;
    }
    
}
