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

import org.apache.log4j.Logger;
import org.pustefixframework.util.LogUtils;

public class PerformanceLoggingFilter implements Filter {
    
    Logger LOG = Logger.getLogger(PerformanceLoggingFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        long t1 = System.currentTimeMillis();
        
        chain.doFilter(request, response);
        
        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) { 
        
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse res = (HttpServletResponse)response;
            
            Long domtime = (Long)req.getAttribute(AbstractPustefixXMLRequestHandler.GETDOMTIME);
            if(domtime != null) {
                long t2 = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append(LogUtils.makeLogSafe(AbstractPustefixRequestHandler.getServerName(req))).append("|");
                sb.append(req.getMethod()).append("|");
                sb.append(req.isSecure() ? "https" : "http").append("|");
                sb.append(res.getStatus()).append("|");
                sb.append(LogUtils.makeLogSafe(req.getRequestURI())).append("|");
                sb.append(LogUtils.makeLogSafe(req.getQueryString())).append("|");
                sb.append(LogUtils.makeLogSafe(req.getHeader("Referer"))).append("|");
                sb.append(LogUtils.makeLogSafe(req.getHeader("User-Agent"))).append("|");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.GETDOMTIME)).append(",");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.TRAFOTIME)).append(",");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.RENDEREXTTIME)).append(",");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.PREPROCTIME)).append(",");
                sb.append(getAttribute(req, AbstractPustefixXMLRequestHandler.EXTFUNCTIME)).append("|");
                sb.append(t2 - t1).append("|");
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
