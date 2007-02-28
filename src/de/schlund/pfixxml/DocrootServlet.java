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

package de.schlund.pfixxml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * In standalone mode this servlet serves the static files from the docroot.
 * In all modes, it serves files from the webapplication directory below
 * /xml because they would usually be masked by the files within the docroot.   
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootServlet extends HttpServlet {
    private String base;

    private String defaultpath;
    
    private List<String> passthroughPaths;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean docrootMode;

        // Get path and determine whether to deliver files from
        // webapplication or docroot directory
        String path = req.getPathInfo();

        if (path != null) {
            docrootMode = false;
        } else {
            path = req.getServletPath();
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
                if (passthroughPaths != null) {
                    for (String prefix : this.passthroughPaths) {
                        if (path.startsWith(prefix)) {
                            in = ResourceUtil.getFileResourceFromDocroot(path).getInputStream();
                        }
                    }
                }
                
                if (in == null) {
                    File file = new File(base, path);
                    in = new BufferedInputStream(new FileInputStream(file));
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

    public void init() throws ServletException {
        // In standalone mode, the context sets a parameter
        // containing the path to the docroot
        this.base = this.getServletContext().getInitParameter("staticDocBase");

        this.defaultpath = null;
        String temp = this.getInitParameter("defaultpath");
        if (temp != null && temp.length() > 0) {
            if (temp.charAt(0) != '/') {
                temp = "/" + temp;
            }
            if (temp.length() > 1) {
                this.defaultpath = temp;
            }
        }
        
        // Create a list of passthrough paths
        String passthroughParam = this.getInitParameter("passthroughPaths");
        if (passthroughParam != null && passthroughParam.length() > 0) {
            ArrayList<String> passthroughPaths = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(passthroughParam, ":");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (token.length() > 0) {
                    if (token.charAt(0) != '/') {
                        token = "/" + token;
                    }
                    if (token.charAt(token.length()-1) != '/') {
                        token = token + "/";
                    }
                    passthroughPaths.add(token);
                }
            }
            this.passthroughPaths = passthroughPaths;
        }
    }

}
