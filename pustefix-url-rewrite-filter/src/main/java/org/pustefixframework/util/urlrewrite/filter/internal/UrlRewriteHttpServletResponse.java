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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.pustefixframework.util.urlrewrite.io.ByteNode;

/**
 * Servlet response wrapper, rewriting URLs in HTML output and location header.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UrlRewriteHttpServletResponse extends HttpServletResponseWrapper {

    ByteNode<byte[]> replacementTree;

    UrlRewriteServletOutputStream outputStream;

    ServletOutputStream originalOutputStream;

    PrintWriter printWriter;

    StringRewriteUtil stringRewriteUtil;

    public UrlRewriteHttpServletResponse(HttpServletResponse response, ByteNode<byte[]> replacementTree) {
        super(response);
        this.replacementTree = replacementTree;
        this.stringRewriteUtil = new StringRewriteUtil(replacementTree);
    }

    @Override
    public void addCookie(Cookie cookie) {
        String path = cookie.getPath();
        if (path != null) {
            path = stringRewriteUtil.rewriteString(path);
            cookie = (Cookie) cookie.clone();
            cookie.setPath(path);
        }
        super.addCookie(cookie);
    }

    @Override
    public void addHeader(String name, String value) {
        if (name.equals("Location")) {
            value = rewriteURLString(value);
        }
        super.addHeader(name, value);
    }

    @Override
    public void flushBuffer() throws IOException {
        if (this.outputStream != null) {
            this.outputStream.flush();
        }
        super.flushBuffer();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException("getWriter() has been called earlier");
        }
        if (this.outputStream == null) {
            this.outputStream = new UrlRewriteServletOutputStream(super.getOutputStream(), replacementTree, getBufferSize(), getContentType());
        }
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.printWriter == null) {
            if (this.outputStream != null) {
                throw new IllegalStateException("getOutputStream() has been called earlier");
            }
            this.printWriter = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
        }
        return this.printWriter;
    }

    @Override
    public void reset() {
        if (outputStream instanceof UrlRewriteServletOutputStream) {
            UrlRewriteServletOutputStream rewriteOutputStream = (UrlRewriteServletOutputStream) outputStream;
            if (!rewriteOutputStream.isCommitted()) {
                rewriteOutputStream.resetBuffer();
            } else {
                throw new IllegalStateException("Not allowed for committed response");
            }
        }
        super.reset();
    }

    @Override
    public void resetBuffer() {
        if (outputStream instanceof UrlRewriteServletOutputStream) {
            UrlRewriteServletOutputStream rewriteOutputStream = (UrlRewriteServletOutputStream) outputStream;
            if (!rewriteOutputStream.isCommitted()) {
                rewriteOutputStream.resetBuffer();
            } else {
                throw new IllegalStateException("Not allowed for committed response");
            }
        }
        super.resetBuffer();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        super.sendRedirect(rewriteURLString(location));
    }

    @Override
    public void setHeader(String name, String value) {
        if (name.equals("Location")) {
            value = rewriteURLString(value);
        }
        super.setHeader(name, value);
    }

    @Override
    public int getBufferSize() {
        if (this.outputStream != null && this.outputStream instanceof UrlRewriteServletOutputStream) {
            UrlRewriteServletOutputStream rewriteOutputStream = (UrlRewriteServletOutputStream) outputStream;
            return rewriteOutputStream.getBufferSize();
        } else {
            return super.getBufferSize();
        }
    }

    @Override
    public boolean isCommitted() {
        boolean committed = false;
        if (this.outputStream != null && this.outputStream instanceof UrlRewriteServletOutputStream) {
            UrlRewriteServletOutputStream rewriteOutputStream = (UrlRewriteServletOutputStream) outputStream;
            committed = rewriteOutputStream.isCommitted();
        }
        return committed || super.isCommitted();
    }

    @Override
    public void setBufferSize(int size) {
        if (this.outputStream != null && this.outputStream instanceof UrlRewriteServletOutputStream) {
            UrlRewriteServletOutputStream rewriteOutputStream = (UrlRewriteServletOutputStream) outputStream;
            if (!rewriteOutputStream.isCommitted()) {
                rewriteOutputStream.setBufferSize(size);
            } else {
                throw new IllegalStateException("Not allowed for committed response");
            }
        }
        super.setBufferSize(size);
    }

    @Override
    public void setContentType(String type) {
        if (this.outputStream != null && this.outputStream instanceof UrlRewriteServletOutputStream) {
            UrlRewriteServletOutputStream rewriteOutputStream = (UrlRewriteServletOutputStream) outputStream;
            rewriteOutputStream.setContentType(type);
        }
        super.setContentType(type);
    }

    private String rewriteURLString(String value) {
        value = stringRewriteUtil.rewriteString(value);
        byte[] bb = value.getBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bb.length; i++) {
            byte b = bb[i];
            if (b >= 0) {
                sb.append((char) b);
            } else {
                int bi = b + 256;
                String hs = Integer.toHexString(bi);
                sb.append('%');
                if (hs.length() == 1) {
                    sb.append('0');
                }
                sb.append(hs);
            }
        }
        return sb.toString();
    }
}
