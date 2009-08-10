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

package org.pustefixframework.config.directoutputservice;

import org.springframework.beans.factory.config.BeanReference;

/**
 * Holds a direct output request handler config object.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface DirectOutputRequestHandlerConfigHolder {

    /**
     * Returns the direct output config object. The returned object must either 
     * implement the {@link DirectOutputRequestHandlerConfig} interface or be a 
     * {@link BeanReference} to a bean that implements this interface.
     * 
     * @return object containing direct output configuration
     */
    Object getDirectOutputServiceConfigObject();
}
