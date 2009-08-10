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

package org.pustefixframework.config.directoutputservice.parser.internal;

import java.util.LinkedList;
import java.util.List;

import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfig;
import org.pustefixframework.extension.DirectOutputPageRequestConfigExtension;
import org.pustefixframework.extension.DirectOutputPageRequestConfigExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

/**
 * Extension for direct output page configuration extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DirectOutputPageRequestConfigExtensionImpl extends AbstractExtension<DirectOutputPageRequestConfigExtensionPoint, DirectOutputPageRequestConfigExtensionImpl> implements DirectOutputPageRequestConfigExtension {

    private InternalDirectOutputPageRequestConfigMap pageMap = new InternalDirectOutputPageRequestConfigMap();

    public DirectOutputPageRequestConfigExtensionImpl() {
        setExtensionPointType(DirectOutputPageRequestConfigExtensionPoint.class);
    }

    public List<DirectOutputPageRequestConfig> getDirectOutputPageRequestConfigs() {
        return new LinkedList<DirectOutputPageRequestConfig>(pageMap.values());
    }

    public void setDirectOutputPageRequestConfigObjects(List<Object> directOutputPageRequestConfigObjects) {
        pageMap.setDirectOutputPageRequestConfigObjects(directOutputPageRequestConfigObjects);
    }

    private class InternalDirectOutputPageRequestConfigMap extends DirectOutputPageRequestConfigMap {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (DirectOutputPageRequestConfigExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(DirectOutputPageRequestConfigExtensionImpl.this);
                }
            }
        }

    }

}
