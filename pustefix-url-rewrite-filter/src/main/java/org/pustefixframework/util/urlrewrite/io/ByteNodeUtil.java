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

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.pustefixframework.util.urlrewrite.io.internal.ByteNodeImpl;

/**
 * Utility for working with {@link ByteNode} trees.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class ByteNodeUtil {

    /**
     * Creates a {@link ByteNode} tree and returns the root node for
     * a map of strings. This tree maps stores the value associated with 
     * a key as an array of bytes associated with the byte node being the 
     * leaf of the path representing the key.
     * 
     * @param map map of string keys to string values
     * @return ByteNode instance that can be used to efficiently lookup 
     *   a byte sequence that is associated with another byte sequence.
     */
    public static ByteNode<byte[]> generateByteNodeTree(Map<? extends CharSequence, ? extends CharSequence> map) {
        ByteNodeImpl<byte[]> root = new ByteNodeImpl<byte[]>();
        for (Entry<? extends CharSequence, ? extends CharSequence> entry : map.entrySet()) {
            try {
                root.addBytes(entry.getKey().toString().getBytes("UTF-8"), entry.getValue().toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Could not use charset UTF-8", e);
            }
        }
        return root;
    }
}
