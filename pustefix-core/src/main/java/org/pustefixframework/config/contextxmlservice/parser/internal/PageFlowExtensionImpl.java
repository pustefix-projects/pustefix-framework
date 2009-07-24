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

import org.pustefixframework.extension.PageFlowExtension;
import org.pustefixframework.extension.PageFlowExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

import de.schlund.pfixcore.workflow.context.PageFlow;


/**
 * Extension for page flow extension point.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowExtensionImpl extends AbstractExtension<PageFlowExtensionPoint, PageFlowExtensionImpl> implements PageFlowExtension {

    private InternalPageFlowMap pageFlowMap = new InternalPageFlowMap();

    public PageFlowExtensionImpl() {
        setExtensionPointType(PageFlowExtensionPoint.class);
    }
    
    public List<PageFlow> getPageFlows() {
        return new LinkedList<PageFlow>(pageFlowMap.values());
    }
    
    public void setPageFlowObjects(List<Object> pageFlowObjects) {
        pageFlowMap.setPageFlowObjects(pageFlowObjects);
    }

    private class InternalPageFlowMap extends PageFlowMap  {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (PageFlowExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(PageFlowExtensionImpl.this);
                }
            }
        }
        
    }

}
