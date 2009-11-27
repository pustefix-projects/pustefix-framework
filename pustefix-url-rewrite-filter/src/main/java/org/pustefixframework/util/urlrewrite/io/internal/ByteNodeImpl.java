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

package org.pustefixframework.util.urlrewrite.io.internal;

import java.util.Arrays;

import org.pustefixframework.util.urlrewrite.io.ByteNode;

/**
 * Simple implementation of {@link ByteNode}.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ByteNodeImpl<T> implements ByteNode<T> {

    private byte b;

    @SuppressWarnings("unchecked")
    private ByteNodeImpl<T>[] children = (ByteNodeImpl<T>[]) new ByteNodeImpl<?>[0];

    private T info;

    protected void addBytes(byte[] bytes, int offset, T info) {
        if (bytes.length - offset == 0) {
            this.info = info;
            return;
        }
        ByteNodeImpl<T> child = findChildForByte(bytes[offset]);
        if (child == null) {
            child = new ByteNodeImpl<T>();
            child.b = bytes[offset];
            children = Arrays.copyOf(children, children.length + 1);
            children[children.length - 1] = child;
        }
        child.addBytes(bytes, offset + 1, info);
    }

    public void addBytes(byte[] bytes, T info) {
        addBytes(bytes, 0, info);
    }

    public ByteNodeImpl<T> findChildForByte(byte b) {
        for (int i = 0; i < children.length; i++) {
            if (children[i].b == b) {
                return children[i];
            }
        }
        return null;
    }

    public T getInfo() {
        return info;
    }
}
