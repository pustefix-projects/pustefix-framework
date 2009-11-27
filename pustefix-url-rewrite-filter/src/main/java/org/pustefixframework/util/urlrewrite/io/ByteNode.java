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

package org.pustefixframework.util.urlrewrite.io;

/**
 * Represents a node in a tree of bytes.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ByteNode<T> {

    /**
     * Returns the child node associated with the given byte
     * or <code>null</code> if no such node exists.
     * 
     * @param b byte too look for in the table of child nodes.
     * @return child node or <code>null</code> if no matching child node exists
     */
    ByteNode<T> findChildForByte(byte b);

    /**
     * Returns the information object associated with this node. This object 
     * may be used to store arbitrary information, that is used by the 
     * application.
     * 
     * @return information associated with this node or <code>null</code> if 
     * no information has been stored for this node.
     */
    T getInfo();
}
