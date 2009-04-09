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

package org.pustefixframework.editor.backend.remote;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


public class HTTPAuthenticationHandlerInterceptor implements HandlerInterceptor {
    
    private String secret;
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Do nothing
    }
    
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Do nothing
    }
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof RemoteServiceExporter)) {
            return true;
        }
        try {
            if (!checkAuth(request)) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Pustefix Editor Web Service Interface\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
        return true;
    }
    
    private boolean checkAuth(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return false;
        }
        header = header.trim();
        int pos = header.indexOf(' ');
        if (pos == -1) {
            throw new IllegalArgumentException();
        }
        String scheme = header.substring(0, pos).trim();
        if (!scheme.equalsIgnoreCase("Basic")) {
            return false;
        }
        String encoded = header.substring(pos).trim();
        String decoded = new String(base64Decode(encoded));
        pos = decoded.indexOf(':');
        if (pos == -1) {
            return false;
        }
        @SuppressWarnings("unused")
        String username = decoded.substring(0, pos);
        String password = decoded.substring(pos + 1);
        if (password.equals(secret)) {
            return true;
        }
        return false;
    }
    
    public static byte[] base64Decode(String str) {
        try {
            byte[] bytes = str.getBytes("utf8");
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            InputStream is = MimeUtility.decode(bais, "Base64");
            byte[] tmp = new byte[bytes.length];
            int n = is.read(tmp);
            byte[] res = new byte[n];
            System.arraycopy(tmp, 0, res, 0, n);
            return res;
        } catch (Exception x) {
            throw new RuntimeException("Base64 decoding failed", x);
        }
    }
    
}
