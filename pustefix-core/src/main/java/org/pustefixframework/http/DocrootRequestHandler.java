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

package org.pustefixframework.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionPointImpl;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.extension.StaticResourceExtension;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.LastModifiedInfoResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.URLResource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.mvc.LastModified;

import de.schlund.pfixxml.util.MD5Utils;

/**
 * Request handler serving static files.   
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootRequestHandler implements UriProvidingHttpRequestHandler, LastModified, ServletContextAware {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final static URI APPLICATION_URI_PREFIX = URI.create("bundle:///PUSTEFIX-INF/");

    private final static URI CORE_URI_PREFIX = URI.create("pustefixcore:///");

    private ResourceLoader resourceLoader;

    private List<String> applicationPathPrefixes;

    private List<StaticResourceExtensionPointImpl> extensionPoints;

    private String defaultPath;

    private URI baseURI;

    private ServletContext servletContext;

    public String[] getRegisteredURIs() {
        return new String[] { "/**" };
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Handle redirect from root
        if (this.defaultPath != null && (req.getPathInfo() == null || req.getPathInfo().length() == 0 || req.getPathInfo().equals("/"))) {
            res.sendRedirect(req.getContextPath() + this.defaultPath);
            return;
        }

        InputStreamResource resource = findResource(req, res);
        if (resource == null) {
            return;
        }
        sendResource(resource, req, res);
    }

    private InputStreamResource findResource(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getPathInfo().trim();

        if (path == null || path.endsWith("/")) {
            if (res != null) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Directory listing not allowed.");
            }
            return null;
        }

        if (path.contains("../")) {
            if (res != null) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Detected possible directory traversal.");
            }
            return null;
        }

        // Make sure path does not start with a slash
        while (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }

        path = path.trim();
        if (path.length() == 0) {
            if (res != null) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Directory listing not allowed.");
            }
            return null;
        }

        // Create relative URI
        URI pathURI;
        try {
            // The URI constructors assemble a string that is then parsed.
            // Therefore a scheme in the path part will be interpreted as a 
            // scheme if no explicit scheme is given.
            // To avoid the construction of an absolute URI, we use the single 
            // argument constructor here, but encode the string.
            pathURI = new URI(URLEncoder.encode(path, "UTF-8"));
        } catch (URISyntaxException e) {
            if (res != null) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path.");
            }
            return null;
        }

        // Found resource (initially null)
        InputStreamResource resource = null;

        // Search application's configured static paths for resource
        if (applicationPathPrefixes != null) {
            if (isMatchingPrefix(path, applicationPathPrefixes)) {
                // Path is starting with an allowed prefix, so try to find resource
                // in application bundle.
                URI resourceURI = APPLICATION_URI_PREFIX.resolve(pathURI);
                resource = loadInputStreamResource(resourceURI);
            }
        }

        if (resource != null) {
            return resource;
        }

        // Search special paths from Pustefix Core
        if (path.startsWith("core/img/") || path.startsWith("core/script/")) {
            URI relativeURI;
            try {
                relativeURI = new URI(null, null, path.substring(5), null);
            } catch (URISyntaxException e) {
                if (res != null) {
                    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path.");
                }
                return null;
            }
            URI resourceURI = CORE_URI_PREFIX.resolve(relativeURI);
            resource = loadInputStreamResource(resourceURI);
        }

        if (resource != null) {
            return resource;
        }

        for (StaticResourceExtensionPointImpl extensionPoint : extensionPoints) {
            for (StaticResourceExtension extension : extensionPoint.getExtensions()) {
                resource = extension.getResource(pathURI);
                if (resource != null) {
                    return resource;
                }
            }
        }

        // Search base path
        if (baseURI != null) {
            resource = loadInputStreamResource(baseURI.resolve(pathURI));
        }

        if (resource == null && res != null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource " + pathURI.toASCIIString() + " not found.");
        }

        return resource;
    }

    private void sendResource(InputStreamResource resource, HttpServletRequest req, HttpServletResponse res) throws IOException {
        URLResource urlResource = null;
        URLConnection urlConnection = null;
        if (resource instanceof URLResource) {
            urlResource = (URLResource) resource;
            urlConnection = urlResource.getURL().openConnection();
        }

        String resourceETag;
        if (urlResource != null) {
            // If we have an URL resource, we use a simplified
            // method for calculating the ETag, as this method
            // does not require reading the whole file.
            StringBuilder sb = new StringBuilder();
            sb.append(urlResource.getURL().toExternalForm());
            sb.append(";");
            sb.append(urlConnection.getContentLength());
            sb.append(";");
            urlConnection.getContentType();
            sb.append(";");
            sb.append(urlConnection.getLastModified());
            resourceETag = MD5Utils.hex_md5(sb.toString());
        } else {
            InputStream is = resource.getInputStream();
            resourceETag = MD5Utils.hex_md5(is);
            is.close();
        }

        String reqETag = req.getHeader("If-None-Match");
        if (reqETag != null) {
            if (resourceETag.equals(reqETag)) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                res.flushBuffer();
                logger.debug("ETag didn't change -> send 'not modified' for resource: " + resource);
                return;
            }
        }

        String mimeType = null;

        if (urlResource != null) {
            mimeType = urlResource.getURL().openConnection().getContentType();
        }

        if (mimeType == null) {
            mimeType = servletContext.getMimeType(req.getPathInfo());
        }

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        res.setContentType(mimeType);

        if (urlResource != null) {
            int contentLength = urlConnection.getContentLength();
            if (contentLength > -1) {
                res.setContentLength(contentLength);
            }
        }

        res.setHeader("ETag", resourceETag);

        res.setHeader("Cache-Control", "max-age=3600");

        OutputStream out = new BufferedOutputStream(res.getOutputStream());

        InputStream is = resource.getInputStream();
        int bytes_read;
        byte[] buffer = new byte[1024];
        while ((bytes_read = is.read(buffer)) != -1) {
            out.write(buffer, 0, bytes_read);
        }
        out.flush();
        is.close();
        out.close();
    }

    private InputStreamResource loadInputStreamResource(URI resourceURI) {
        Resource resource = resourceLoader.getResource(resourceURI);
        if (resource instanceof InputStreamResource) {
            return (InputStreamResource) resource;
        } else {
            return null;
        }
    }

    private boolean isMatchingPrefix(String str, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }
            if (!prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
            if (str.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public long getLastModified(HttpServletRequest request) {
        InputStreamResource resource;
        try {
            resource = findResource(request, null);
        } catch (IOException e) {
            return -1;
        }
        if (resource == null) {
            return -1;
        }
        if (resource instanceof LastModifiedInfoResource) {
            LastModifiedInfoResource lastModifiedInfoResource = (LastModifiedInfoResource) resource;
            return lastModifiedInfoResource.lastModified();
        } else {
            return -1;
        }
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public void setApplicationPathPrefixes(List<String> applicationPathPrefixes) {
        this.applicationPathPrefixes = applicationPathPrefixes;
    }

    public void setBase(String path) {
        this.baseURI = URI.create(path);
    }

    public void setStaticResourceExtensionPoints(List<StaticResourceExtensionPointImpl> extensionPoints) {
        this.extensionPoints = extensionPoints;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}