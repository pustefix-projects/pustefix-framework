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

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.pustefixframework.util.urlrewrite.io.ByteNode;

/**
 * Servlet request wrapper, rewriting request path.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UrlRewriteHttpServletRequest extends HttpServletRequestWrapper {

    private ServletContext servletContext;

    private String pathInfo;

    private Cookie[] cookies;

    public UrlRewriteHttpServletRequest(HttpServletRequest request, ByteNode<byte[]> replacementTree, ServletContext servletContext) {
        super(request);
        this.servletContext = servletContext;
        StringRewriteUtil stringRewriteUtil = new StringRewriteUtil(replacementTree);
        String pathInfo = super.getPathInfo();
        if (pathInfo != null) {
            this.pathInfo = stringRewriteUtil.rewriteString(pathInfo);
        }
        Cookie[] cookies = super.getCookies();
        if (cookies != null) {
            this.cookies = new Cookie[cookies.length];
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = (Cookie) cookies[i].clone();
                String path = cookie.getPath();
                if (path != null) {
                    path = stringRewriteUtil.rewriteString(path);
                    cookie.setPath(path);
                }
                this.cookies[i] = cookie;
            }
        }
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    public String getPathTranslated() {
        String pathInfo = getPathInfo();
        if (pathInfo == null) {
            return null;
        } else {
            return servletContext.getRealPath(pathInfo);
        }
    }

    @Override
    public String getRequestURI() {
        String pathInfo = getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        return getContextPath() + getServletPath() + pathInfo;
    }

    @Override
    public StringBuffer getRequestURL() {
        String scheme = getScheme();
        int port = getServerPort();
        StringBuffer sb = new StringBuffer();
        sb.append(scheme);
        sb.append("://");
        sb.append(getServerName());
        if (!(scheme.equals("http") && port == 80) && !(scheme.equals("https") && port == 443)) {
            sb.append(':');
            sb.append(port);
        }
        sb.append(getRequestURI());
        return sb;
    }

}
