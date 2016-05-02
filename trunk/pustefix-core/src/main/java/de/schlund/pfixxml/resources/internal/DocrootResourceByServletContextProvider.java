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

package de.schlund.pfixxml.resources.internal;

import java.net.URI;

import javax.servlet.ServletContext;

import de.schlund.pfixxml.resources.DocrootResourceProvider;
import de.schlund.pfixxml.resources.Resource;

/**
 * Provider using the resources provided by an instance of {@link ServletContext}
 * to resolve docroot resources.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootResourceByServletContextProvider extends DocrootResourceProvider {
    
    private ServletContext context;
    
    public DocrootResourceByServletContextProvider(javax.servlet.ServletContext servletContext) {
        this.context = servletContext;
    }
    
    public Resource getResource(URI uri) {
        return new DocrootResourceByServletContextImpl(uri, context);
    }

}
