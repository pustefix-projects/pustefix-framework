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
 *
 */

package de.schlund.pfixcore.workflow;

import java.util.Iterator;

public interface ContextResourceManager {

    /**
     * Returns the stored object which implements the interface,
     * specified by the full qualified classname of the requested interface, or
     * null, if no object for the interface name is found. 
     *
     * @param name the classname of the requested interface
     * @return an object of a class implementing the requested interface, which
               extends <code>ContextResource</code>
     */
    public Object getResource(String name);

    /**
     * Returns the ContextResource object which implements the interface
     * passed as argument (or null if no object found).
     * 
     * @param clazz the interface class
     * @return instance of the class implementing the interface
     */
    //@SuppressWarnings("unchecked")
    public <T> T getResource(Class<T> clazz);

    /**
     * Returns an iterator for all stored objects.
     *
     * @return the <code>Iterator</code>
     */
    public Iterator<Object> getResourceIterator();

}