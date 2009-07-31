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

import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.extension.PageRequestConfigExtension;
import org.pustefixframework.extension.PageRequestConfigExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

/**
 * Extension for page configuration extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestConfigExtensionImpl extends AbstractExtension<PageRequestConfigExtensionPoint, PageRequestConfigExtensionImpl> implements PageRequestConfigExtension {

    private InternalPageRequestConfigMap pageMap = new InternalPageRequestConfigMap();

    public PageRequestConfigExtensionImpl() {
        setExtensionPointType(PageRequestConfigExtensionPoint.class);
    }

    public List<PageRequestConfig> getPageRequestConfigs() {
        return new LinkedList<PageRequestConfig>(pageMap.values());
    }

    public void setPageRequestConfigObjects(List<Object> pageRequestConfigObjects) {
        pageMap.setPageRequestConfigObjects(pageRequestConfigObjects);
    }

    private class InternalPageRequestConfigMap extends PageRequestConfigMap {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (PageRequestConfigExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(PageRequestConfigExtensionImpl.this);
                }
            }
        }

    }

}
