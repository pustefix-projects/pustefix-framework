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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Output stream decorator, that replaces certain byte sequences with other 
 * byte sequences. This can be used to implement a search and replace 
 * method without having to store the whole text in memory. 
 * This class is not thread-safe.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ByteSequenceReplacingOutputStream extends OutputStream {

    private OutputStream out;

    private ByteNode<byte[]> root = null;

    private ByteNode<byte[]> activeNode = null;

    private boolean replacing = true;

    private byte[] buffer = new byte[1024];

    private int bufDataLen = 0;

    /**
     * Create a wrapper around an {@link OutputStream} instance.
     * 
     * @param out the output stream this stream writes to
     * @param replacementTree tree containing the byte sequences to search for 
     *  and their replacements. This tree can be created using 
     *  {@link ByteNodeUtil}.
     */
    public ByteSequenceReplacingOutputStream(OutputStream out, ByteNode<byte[]> replacementTree) {
        this.root = replacementTree;
        this.out = out;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (!replacing) {
            doFlush();
            out.write(b, off, len);
            return;
        }

        int ioff = off;
        int ilen = len;
        int copyLen = 0;
        int copyOffset = ioff;
        while (ilen > 0) {
            ByteNode<byte[]> child = null;
            if (activeNode != null) {
                child = activeNode.findChildForByte(b[ioff]);
                if (child == null) {
                    byte[] replacement = activeNode.getInfo();
                    if (replacement != null) {
                        out.write(replacement);
                    } else {
                        if (bufDataLen != 0) {
                            out.write(buffer, 0, bufDataLen);
                        }
                        if (copyLen != 0) {
                            out.write(b, copyOffset, copyLen);
                        }
                    }
                    bufDataLen = 0;
                    copyOffset = ioff;
                    copyLen = 0;
                    activeNode = null;
                }
            }
            if (child == null) {
                child = root.findChildForByte(b[ioff]);
            }
            if (child != null) {
                if (activeNode == null && copyLen != 0) {
                    out.write(b, copyOffset, copyLen);
                    copyLen = 0;
                    copyOffset = ioff;
                }
                activeNode = child;
            }
            copyLen++;
            ioff++;
            ilen--;
        }
        if (copyLen > 0) {
            if (activeNode == null) {
                out.write(b, copyOffset, copyLen);
            } else {
                while (copyLen + bufDataLen > buffer.length) {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }
                System.arraycopy(b, copyOffset, buffer, bufDataLen, copyLen);
                bufDataLen += copyLen;
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        if (!replacing) {
            doFlush();
            out.write(b);
            return;
        }

        byte bb = (byte) b;
        ByteNode<byte[]> child = null;
        if (activeNode != null) {
            child = activeNode.findChildForByte(bb);
            if (child == null) {
                byte[] replacement = activeNode.getInfo();
                if (replacement != null) {
                    out.write(replacement);
                } else {
                    out.write(buffer, 0, bufDataLen);
                }
                bufDataLen = 0;
                activeNode = null;
            }
        }
        if (child == null) {
            child = root.findChildForByte(bb);
        }
        if (child == null) {
            out.write(b);
        } else {
            activeNode = child;
            if (bufDataLen == buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }
            buffer[bufDataLen] = bb;
            bufDataLen++;
        }
    }

    @Override
    public void flush() throws IOException {
        doFlush();
        out.flush();
    }

    private void doFlush() throws IOException {
        if (bufDataLen > 0) {
            write(buffer, 0, bufDataLen);
            bufDataLen = 0;
        }
        activeNode = null;
    }

    /**
     * Checks whether this stream is configured to replace 
     * byte sequences.
     * 
     * @return flag indicating replacement option
     */
    public boolean isReplacing() {
        return replacing;
    }

    /**
     * Sets the repaclement option. If it is set to <code>true</code>
     * (default), this stream will use the replacement map to replace 
     * certain byte sequences with other byte sequences. If the option 
     * is set to <code>false</code>, data written to this stream will 
     * just be passed through.
     * @param replacing
     * @throws IOException 
     */
    public void setReplacing(boolean replacing) {
        this.replacing = replacing;
    }

}
