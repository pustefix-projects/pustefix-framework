package org.pustefixframework.pfxinternals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class DownloadAction implements Action {

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
        
        String resourceParam = req.getParameter("resource");
        if(resourceParam != null) {
            if(resourceParam.startsWith("/")) {
                resourceParam = "file:" + resourceParam;
            }
            Resource resource = ResourceUtil.getResource(resourceParam);
            deliver(resource, res, pageContext.getServletContext());
            return;
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Missing resource parameter");
        }
    }
    
    private void deliver(Resource res, HttpServletResponse response, ServletContext ctx) throws IOException {
        
        if(!"prod".equals(EnvironmentProperties.getProperties().get("mode"))) {
            if(res.exists()) {
                OutputStream out = response.getOutputStream();
        
                String type = ctx.getMimeType(res.getFilename());
                if (type == null) {
                    type = "application/octet-stream";
                }
                response.setContentType(type);
                response.setHeader("Content-Disposition", "inline;filename="+res.getFilename());
                
                long contentLength = res.length();
                if(contentLength > -1 && contentLength < Integer.MAX_VALUE) {
                    response.setContentLength((int)contentLength);
                }
                
                long lastModified = res.lastModified();
                if(lastModified > -1) {
                    response.setDateHeader("Last-Modified", lastModified);
                }
                
                InputStream in = res.getInputStream();
                byte[] buffer = new byte[4096];
                int no = 0;
                try {
                    while ((no = in.read(buffer)) != -1)
                        out.write(buffer, 0, no);
                } finally {
                    in.close();
                    out.close();
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, res.toString());
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, res.toString());
        }
        
    }
    
}
