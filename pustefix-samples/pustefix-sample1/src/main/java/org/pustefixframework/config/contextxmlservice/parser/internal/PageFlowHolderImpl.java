/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxmlservice.parser.internal;

import org.pustefixframework.config.contextxmlservice.PageFlowHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;

public class PageFlowHolderImpl implements PageFlowHolder {
    
    private String name;
    private RuntimeBeanReference reference;
    
    public PageFlowHolderImpl(String name, String beanName) {
        this.name = name;
        this.reference = new RuntimeBeanReference(beanName);
    }
    
    public String getName() {
        return name;
    }

    public Object getPageFlowObject() {
        return reference;
    }
    
}