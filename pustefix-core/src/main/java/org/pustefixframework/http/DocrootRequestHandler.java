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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.I18NResourceUtil;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.MD5Utils;

/**
 * This servlet delivers static files from the webapp or embedded modules.   
 * 
 */
public class DocrootRequestHandler implements UriProvidingHttpRequestHandler, ServletContextAware, InitializingBean {
    
    private Logger LOG = Logger.getLogger(DocrootRequestHandler.class);
    
    private String base;
    private boolean i18nBase;

    private String defaultpath = "/";
    
    private List<String> passthroughPaths;
    private Set<String> i18nPaths;
    
    private ServletContext servletContext;

    private String mode;
    
    private Set<String> extractedPaths = new HashSet<String>();
    
    private TenantInfo tenantInfo;
    private LanguageInfo languageInfo;
    
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
    
    public void setI18NPaths(Set<String> i18nPaths) {
        this.i18nPaths = i18nPaths;
    }
    
    public void setBase(String path) {
        this.base = path;
    }
    
    public void setI18NBase(boolean i18nBase) {
        this.i18nBase = i18nBase;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        AbstractPustefixRequestHandler.initializeRequest(req, tenantInfo, languageInfo);
        
        String path = req.getPathInfo();
        
        // Handle default (root) request
        if(path == null || path.length() == 0 || (path.equals("/") && !defaultpath.equals("/"))) {
            StringBuilder sb = new StringBuilder();
            sb.append(req.getScheme()).append("://").append(getServerName(req));
            if(!(req.getServerPort() == 80 || req.getServerPort() == 443)) sb.append(":" + req.getServerPort());
            sb.append(req.getContextPath()).append(defaultpath);
            if(req.getQueryString() != null && !req.getQueryString().equals("")) sb.append("?" + req.getQueryString());
            res.sendRedirect(sb.toString());
            return;
        }

        // Avoid path traversal and access to config or source files
        if (path.contains("..") || path.startsWith("/WEB-INF")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Directory listing is not allowed
        if (path.endsWith("/")) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Resource inputResource = null;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Tenant tenant = (Tenant)req.getAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT);
        String tenantName = null;
        if(tenant != null) {
            tenantName = tenant.getName();
        }
        String language = (String)req.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE);
        
        if (passthroughPaths != null) {
            
            for (String prefix : this.passthroughPaths) {
                boolean i18n = false;
                if(i18nPaths != null && i18nPaths.contains(prefix)) {
                    i18n = true;
                }
                if (path.startsWith(prefix)) {
                    Resource resource = null;
                    if(path.startsWith("modules/") && !extractedPaths.contains(prefix)) {
                        String moduleUri = "module://" + path.substring(8);
                        if(i18n) {
                            resource = I18NResourceUtil.getResource(moduleUri, tenantName, language);
                        } else {
                            resource = ResourceUtil.getResource(moduleUri);
                        }
                    } else {
                        if(i18n) {
                            resource = I18NResourceUtil.getFileResourceFromDocroot(path, tenantName, language);
                        } else {
                            resource = ResourceUtil.getFileResourceFromDocroot(path);
                        }
                    }
                    if(resource.exists()) {
                        inputResource = resource;
                        break;
                    }
                }
            }
            if (inputResource == null) {
                FileResource baseResource = ResourceUtil.getFileResource(base);
                FileResource resource;
                if(i18nBase) {
                    resource = I18NResourceUtil.getFileResource(baseResource, path, tenantName, language);
                } else {
                    resource = ResourceUtil.getFileResource(baseResource, path);
                }
                if(resource.exists()) {
                    inputResource = resource;
                }
            }
        }
        
        if(inputResource == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            if(LOG.isDebugEnabled()) {
                LOG.debug("Resource doesn't exist -> send 'not found': " + path);
            }
            return;
        }
        
        if(!inputResource.isFile()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            if(LOG.isDebugEnabled()) {
                LOG.debug("Resource isn't a normal file -> send 'forbidden': " + path);
            }
            return;
        }
        
        long contentLength = inputResource.length();
        long lastModified = inputResource.lastModified();
            
        String reqETag = req.getHeader("If-None-Match");
        if(reqETag != null) {
            String etag = createETag(path, contentLength, lastModified);
            if(etag.equals(reqETag)) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                res.flushBuffer();
                if(LOG.isDebugEnabled()) {
                    LOG.debug("ETag didn't change -> send 'not modified' for resource: " + path);
                }
                return;
            }
        }
     
        long reqMod = req.getDateHeader("If-Modified-Since");
        if(reqMod != -1) {
            if(lastModified < reqMod + 1000) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                res.flushBuffer();
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Modification time didn't change -> send 'not modified' for resource: " + path);
                }
                return;
            }
        }

        String type = getServletContext().getMimeType(path);
        if (type == null) {
            type = "application/octet-stream";
        }
        res.setContentType(type);
        if(contentLength > -1 && contentLength < Integer.MAX_VALUE) {
            res.setContentLength((int)contentLength);
        }
        if(lastModified > -1) {
            res.setDateHeader("Last-Modified", lastModified);
        }
                
        String etag = MD5Utils.hex_md5(path+contentLength+lastModified);
        res.setHeader("ETag", etag);
        
        Integer errorStatus = (Integer)req.getAttribute("javax.servlet.error.status_code");
        if(errorStatus != null) {
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Pragma", "no-cache");
        } else if(mode==null || mode.equals("") || mode.equals("prod")) {
            res.setHeader("Cache-Control", "max-age=3600");
        } else {
            res.setHeader("Cache-Control", "max-age=3, must-revalidate");
        }
         
        if(req.getMethod().equals("HEAD")) {
            return;
        }
        
        OutputStream out = new BufferedOutputStream(res.getOutputStream());
        InputStream in = inputResource.getInputStream();
        int bytes_read;
        byte[] buffer = new byte[8];
        while ((bytes_read = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes_read);
        }
        out.flush();
        in.close();
        out.close();

    }

    
    public String[] getRegisteredURIs() {
        String[] uris;
        if(defaultpath.equals("/")) uris = new String[] {"/?*", "/?*/**"};
        else uris = new String[] {"/**", "/xml/**"};
        return uris;
    }
    
    
    private String createETag(String path, long length, long modtime) {
        return MD5Utils.hex_md5(path + length + modtime);
    }

    
    public void afterPropertiesSet() throws Exception {
        for(String path: passthroughPaths) {
            if(path.startsWith("modules/") || path.equals("modules")) {
                String dirPath = path;
                if(!dirPath.endsWith("/")) dirPath = dirPath + "/";
                Resource resource = ResourceUtil.getFileResourceFromDocroot(dirPath);
                if(resource.exists()) extractedPaths.add(path);
            }
        }
    }
    
    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }
    
    public void setLanguageInfo(LanguageInfo languageInfo) {
        this.languageInfo = languageInfo;
    }
    
    public static String getServerName(HttpServletRequest req) {
        String forward = req.getHeader("X-Forwarded-Server");
        if (forward != null && !forward.equals("")) {
            return forward;
        } else {
            return req.getServerName();
        }
    }

}
