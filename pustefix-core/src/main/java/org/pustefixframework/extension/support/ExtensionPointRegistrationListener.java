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

import org.pustefixframework.extension.Extension;
import org.pustefixframework.extension.ExtensionPoint;


/**
 * Listener that can be registered at an instance of 
 * {@link AbstractExtensionPoint} in order to be notified about
 * registration and deregistration of extensions.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ExtensionPointRegistrationListener <T1 extends ExtensionPoint<? extends T2>, T2 extends Extension> {
    
    /**
     * This method is called before an extension is registered.
     * 
     * @param extensionPoint extension point the extension is going to be
     *  registered at
     * @param extension extension that is going to be registered
     */
    public void beforeRegisterExtension(T1 extensionPoint, T2 extension) {
        
    }

    /**
     * This method is called after an extension has been registered.
     * 
     * @param extensionPoint extension point the extension has been
     *  registered at
     * @param extension extension that has been registered
     */
    public void afterRegisterExtension(T1 extensionPoint, T2 extension) {
        
    }

    /**
     * This method is called before an extension is unregistered.
     * 
     * @param extensionPoint extension point the extension is going to be
     *  unregistered from
     * @param extension extension that is going to be unregistered
     */
    public void beforeUnregisterExtension(T1 extensionPoint, T2 extension) {
        
    }

    /**
     * This method is called after an extension has been unregistered.
     * 
     * @param extensionPoint extension point the extension has been
     *  unregistered from
     * @param extension extension that has been unregistered
     */
    public void afterUnregisterExtension(T1 extensionPoint, T2 extension) {
        
    }
    
    /**
     * This method is called when an extension has notified the
     * extension point about an internal change.
     * 
     * @param extensionPoint extension point that is sending this event
     * @param extension extension that has caused this event to be sent
     */
    public void updateExtension(T1 extensionPoint, T2 extension) {
        
    }
}
