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

package org.pustefixframework.config.contextxmlservice;

/**
 * Holds a public JSON object and its alias name.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface JSONOutputResourceHolder {

    /**
     * Returns the alias of this JSON object.
     * The alias is used to identify the requested object in a request.
     * 
     * @return alias of the JSON object
     */
    String getName();

    /**
     * Returns the object that is sent to the browser through JSON.
     * A bean reference to the object may be returned instead of the 
     * actual object.
     * 
     * @return object that should be sent to the browser
     */
    Object getJSONOutputResource();
}
