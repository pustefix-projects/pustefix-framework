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

package org.pustefixframework.container.spring.beans.support;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.context.request.RequestScope;

/**
 * Scope overriding the default request scope and making the bean names unique per bundle
 * to avoid that beans having the same name are overwritten by beans from different bundles
 * when they're stored as attribute in the current request.
 * 
 * @author mleidig@schlund.de
 *
 */
public class BundleUniqueRequestScope extends RequestScope {

    private long bundleId;
    
    public BundleUniqueRequestScope(long bundleId) {
        this.bundleId = bundleId;
    }
    
    @Override
    public Object get(String name, ObjectFactory objectFactory) {
        return super.get(getUniqueName(name), objectFactory);
    }
    
    @Override
    public Object remove(String name) {
        return super.remove(getUniqueName(name));
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        super.registerDestructionCallback(getUniqueName(name), callback);
    }
    
    private String getUniqueName(String name) {
        return name + "@bundle:" + bundleId;
    }
    
}
