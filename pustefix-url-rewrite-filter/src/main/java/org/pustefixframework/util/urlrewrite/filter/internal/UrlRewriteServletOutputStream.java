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

package org.pustefixframework.util.urlrewrite.filter.internal;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.pustefixframework.util.urlrewrite.io.ByteNode;
import org.pustefixframework.util.urlrewrite.io.HtmlLinkReplacingOutputStream;

/**
 * Wrapper around {@link HtmlLinkReplacingOutputStream} that implements 
 * {@link ServletOutputStream}.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UrlRewriteServletOutputStream extends ServletOutputStream {

    byte[] buffer;

    int bufferSize;

    int bytesWritten = 0;

    private String mimeType;

    private ServletOutputStream originalStream;

    private OutputStream targetStream;

    private ByteNode<byte[]> replacementTree;

    public UrlRewriteServletOutputStream(ServletOutputStream targetStream, ByteNode<byte[]> replacementTree, int bufferSize, String mimeType) {
        mimeType = extractMimeType(mimeType);
        this.mimeType = mimeType;
        this.originalStream = targetStream;
        if (mimeType != null && mimeType.equals("text/html")) {
            this.targetStream = new HtmlLinkReplacingOutputStream(this.originalStream, replacementTree);
        } else {
            this.targetStream = this.originalStream;
        }
        this.replacementTree = replacementTree;
        this.buffer = new byte[bufferSize];
        this.bufferSize = bufferSize;
    }

    @Override
    public void write(int b) throws IOException {
        if (buffer != null) {
            if (bytesWritten < buffer.length) {
                buffer[bytesWritten] = (byte) b;
                bytesWritten++;
            } else {
                targetStream.write(buffer, 0, bytesWritten);
                buffer = null;
                targetStream.write(b);
            }
        } else {
            targetStream.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        targetStream.close();
    }

    @Override
    public void flush() throws IOException {
        if (buffer != null) {
            targetStream.write(buffer, 0, bytesWritten);
            buffer = null;
        }
        targetStream.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (buffer != null) {
            if (bytesWritten + len <= buffer.length) {
                System.arraycopy(b, off, buffer, bytesWritten, len);
                bytesWritten += len;
            } else {
                targetStream.write(buffer, 0, bytesWritten);
                buffer = null;
                targetStream.write(b, off, len);
            }
        } else {
            targetStream.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void resetBuffer() {
        if (buffer != null) {
            bytesWritten = 0;
        } else {
            throw new IllegalStateException("Cannot reset buffer because response has already been committed.");
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (buffer == null || bytesWritten != 0) {
            throw new IllegalStateException("Cannot change buffer size after bytes have been written to the stream");
        } else {
            this.buffer = new byte[bufferSize];
            this.bufferSize = bufferSize;
        }
    }

    public boolean isCommitted() {
        if (buffer == null) {
            return true;
        } else {
            return false;
        }
    }

    public void setContentType(String mimeType) {
        if (isCommitted()) {
            return;
        }
        mimeType = extractMimeType(mimeType);
        if (this.mimeType != null && this.mimeType.equals("text/html") && !this.mimeType.equals(mimeType)) {
            this.targetStream = this.originalStream;
        } else if (mimeType != null && mimeType.equals("text/html") && !mimeType.equals(this.mimeType)) {
            this.targetStream = new HtmlLinkReplacingOutputStream(originalStream, replacementTree);
        }
    }

    private String extractMimeType(String mimeType) {
        if (mimeType.contains(";")) {
            return mimeType.substring(0, mimeType.indexOf(';'));
        } else {
            return mimeType;
        }
    }
}
