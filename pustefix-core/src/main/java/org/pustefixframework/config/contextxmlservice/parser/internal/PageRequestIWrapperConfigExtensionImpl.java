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

import java.util.LinkedList;
import java.util.List;

import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.extension.PageRequestIWrapperConfigExtension;
import org.pustefixframework.extension.PageRequestIWrapperConfigExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

/**
 * Extension for IWrapper configuration extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestIWrapperConfigExtensionImpl extends AbstractExtension<PageRequestIWrapperConfigExtensionPoint, PageRequestIWrapperConfigExtensionImpl> implements PageRequestIWrapperConfigExtension {

    private InternalIWrapperConfigMap iWrapperMap = new InternalIWrapperConfigMap();

    public PageRequestIWrapperConfigExtensionImpl() {
        setExtensionPointType(PageRequestIWrapperConfigExtensionPoint.class);
    }

    public List<IWrapperConfig> getIWrapperConfigs() {
        return new LinkedList<IWrapperConfig>(iWrapperMap.values());
    }

    public void setIWrapperConfigObjects(List<Object> iWrapperConfigObjects) {
        iWrapperMap.setIWrapperConfigObjects(iWrapperConfigObjects);
    }

    private class InternalIWrapperConfigMap extends IWrapperConfigMap {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (PageRequestIWrapperConfigExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(PageRequestIWrapperConfigExtensionImpl.this);
                }
            }
        }

    }

}
