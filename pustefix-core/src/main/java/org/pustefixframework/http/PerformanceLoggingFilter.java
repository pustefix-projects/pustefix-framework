package org.pustefixframework.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceLoggingFilter implements Filter {
    
    Logger LOG = LoggerFactory.getLogger(PerformanceLoggingFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            chain.doFilter(request, response);
            success = true;
            
        } finally {
           
            if(request instanceof HttpServletRequest && response instanceof HttpServletResponse
                    && request.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_CLIENT_ABORTED) == null
                    && request.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_REQUEST_TYPE) != null) { 
                
                HttpServletRequest req = (HttpServletRequest)request;
                HttpServletResponse res = (HttpServletResponse)response;
                
                long endTime = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append(LogUtils.makeLogSafe(req.getServerName())).append("|");
                sb.append(success ? res.getStatus() : "500").append("|");
                sb.append(req.getMethod()).append("|");
                String path = req.getContextPath() + req.getServletPath();
                if(req.getPathInfo() != null) {
                    path += req.getPathInfo();
                }
                sb.append(LogUtils.makeLogSafe(path)).append("|");
                sb.append(request.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_REQUEST_TYPE)).append("|");
                String requestId = (String)req.getAttribute("requestId");
                if(requestId != null) {
                    sb.append(requestId);
                }
                sb.append("|");
                sb.append(endTime - startTime).append("|");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.GETDOMTIME)).append("|");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.TRAFOTIME)).append("|");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.RENDEREXTTIME)).append("|");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.PREPROCTIME)).append("|");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.EXTFUNCTIME)).append("|");
                LOG.info(sb.toString());
            }
        }
    }
    
    private String getAttribute(HttpServletRequest req, String name) {
        Object value = req.getAttribute(name);
        if(value == null) {
            return ""; 
        } else {
            return value.toString();
        }
    }

    @Override
    public void destroy() {}

}
