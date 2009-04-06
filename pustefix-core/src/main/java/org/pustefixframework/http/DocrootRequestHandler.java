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

package org.pustefixframework.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * This servlet serves the static files from the docroot.   
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootRequestHandler implements UriProvidingHttpRequestHandler, ServletContextAware {
    private String base;

    private String defaultpath;
    
    private List<String> passthroughPaths;
    
    private ServletContext servletContext;

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setDefaultPath(String defaultpath) {
        this.defaultpath = defaultpath;
    }

    public void setPassthroughPaths(List<String> passthroughPaths) {
        this.passthroughPaths = passthroughPaths;
    }
    
    public void setBase(String path) {
        this.base = path;
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean docrootMode;

        // Get path and determine whether to deliver files from
        // webapplication or docroot directory
        String reqPath = req.getPathInfo();
        String path = reqPath;
        String servletPath = req.getServletPath();
        

        if (path != null && (servletPath != null && servletPath.length() > 0)) {
            docrootMode = false;
        } else {
            path = (servletPath != null) ? servletPath : "";
            path += (reqPath != null) ? reqPath : "";
            docrootMode = true;
        }

        // Handle default (root) request
        if (docrootMode && this.defaultpath != null
                && (path == null || path.length() == 0 || path.equals("/"))) {
            res.sendRedirect(req.getContextPath() + this.defaultpath);
            return;
        }

        // Avoid path traversal and access to config or source files
        if (path.contains("..") || path.startsWith("/WEB-INF")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }

        // Directory listing is not allowed
        if (path.endsWith("/")) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, path);
            return;
        }

        try {
            // Docroot is set by context, so if we are in 
            // WAR mode, the docroot will not be available
            // and we simply skip to the webapp directory
            if (docrootMode && (base == null || base.length() == 0)) {
                docrootMode = false;
            }

            InputStream in = null;

            if (docrootMode) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                
                if (passthroughPaths != null) {
                    for (String prefix : this.passthroughPaths) {
                        if (path.startsWith(prefix)) {
                            in = ResourceUtil.getFileResourceFromDocroot(path).getInputStream();
                        }
                    }
                }
                
                if (in == null) {
                    FileResource baseResource = ResourceUtil.getFileResource(base);
                    FileResource resource = ResourceUtil.getFileResource(baseResource, path);
                    in = new BufferedInputStream(resource.getInputStream());
                }
            } else {
                // Use getResourceAsStream() to make sure we can
                // access the file even in packed WAR mode
                in = getServletContext().getResourceAsStream(path);
                if (in == null) {
                    throw new FileNotFoundException();
                }
            }

            String type = getServletContext().getMimeType(path);
            if (type == null) {
                type = "application/octet-stream";
            }
            res.setContentType(type);

            OutputStream out = new BufferedOutputStream(res.getOutputStream());

            int bytes_read;
            byte[] buffer = new byte[8];
            while ((bytes_read = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytes_read);
            }
            out.flush();
            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, path);
        }
    }

    public String[] getRegisteredURIs() {
        return new String[] {"/**", "/xml/**"};
    }
}
