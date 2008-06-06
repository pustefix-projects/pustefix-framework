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

package de.schlund.pfixcore.editor2.core.spring;

/**
 * This service provides methods to store data in the context of a session. The
 * term "session" is defined by the actual implementation of this service. It
 * might e.g. use the session of a servlet when running within a servlet
 * container.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface SessionService {
    /**
     * Returns the object bound to a key
     * 
     * @param key
     *            String containing the key for the object
     * @return The object bound to the key or <code>null</code> if no object
     *         is bound to this key
     */
    Object get(String key);

    /**
     * Stores an object using a key. If there is already an object stored for
     * this key, it is replaced by value.
     * 
     * @param key
     *            The key for the object to store
     * @param value
     *            The object to store
     */
    void set(String key, Object value);

    /**
     * Removes the object stored for a key. If there is no object for this key
     * this method does nothing.
     * 
     * @param key
     *            The key to use
     */
    void unset(String key);
}
