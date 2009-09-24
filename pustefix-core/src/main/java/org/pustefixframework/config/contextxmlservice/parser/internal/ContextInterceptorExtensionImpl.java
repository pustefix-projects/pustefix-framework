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

import org.pustefixframework.extension.ContextInterceptorExtension;
import org.pustefixframework.extension.ContextInterceptorExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

import de.schlund.pfixcore.workflow.ContextInterceptor;

/**
 * Extension for context interceptor extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextInterceptorExtensionImpl extends AbstractExtension<ContextInterceptorExtensionPoint, ContextInterceptorExtensionImpl> implements ContextInterceptorExtension {

    private InternalContextInterceptorList contextInterceptorList = new InternalContextInterceptorList();

    public ContextInterceptorExtensionImpl() {
        setExtensionPointType(ContextInterceptorExtensionPoint.class);
    }

    public List<ContextInterceptor> getContextInterceptors() {
        return new LinkedList<ContextInterceptor>(contextInterceptorList);
    }

    public void setContextInterceptorObjects(List<?> contextInterceptorObjects) {
        contextInterceptorList.setContextInterceptorObjects(contextInterceptorObjects);
    }

    private class InternalContextInterceptorList extends ContextInterceptorList {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (ContextInterceptorExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(ContextInterceptorExtensionImpl.this);
                }
            }
        }

    };
}
