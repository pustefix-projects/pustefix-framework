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

import org.pustefixframework.extension.RoleExtension;
import org.pustefixframework.extension.RoleExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

import de.schlund.pfixcore.auth.Role;

/**
 * Extension for role extension point.  
 * 
 * @author mleidig@schlund.de
 * 
 */
public class RoleExtensionImpl extends AbstractExtension<RoleExtensionPoint, RoleExtensionImpl> implements RoleExtension {

    private InternalRoleMap roleMap = new InternalRoleMap();

    public RoleExtensionImpl() {
        setExtensionPointType(RoleExtensionPoint.class);
    }

    public List<Role> getRoles() {
        return new LinkedList<Role>(roleMap.values());
    }

    public void setRoleObjects(List<Object> roleObjects) {
        roleMap.setRoleObjects(roleObjects);
    }

    private class InternalRoleMap extends RoleMap {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (RoleExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(RoleExtensionImpl.this);
                }
            }
        }

    }

}
