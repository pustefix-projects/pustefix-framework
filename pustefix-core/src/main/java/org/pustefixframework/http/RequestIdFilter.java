package org.pustefixframework.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;

/**
 * Servlet filter which generates a request ID for uniquely identifying requests.
 * 
 * By default the ID is set as Log4J MDC entry, request attribute and response header.
 * The ID name can be configured using the according filter init parameters "mdcKey",
 * "attributeName" and "headerName". Specifying an empty name will disable it.
 */
public class RequestIdFilter implements Filter {
	
    final static String DEFAULT_MDC_KEY = "requestId";
    final static String DEFAULT_ATTRIBUTE_NAME = "requestId";
    final static String DEFAULT_HEADER_NAME = "X-Request-Id";
    
    private String mdcKey = DEFAULT_MDC_KEY;
    private String attributeName = DEFAULT_ATTRIBUTE_NAME;
    private String headerName = DEFAULT_HEADER_NAME;
    
    private Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
        String val = filterConfig.getInitParameter("mdcKey");
        if(val != null) {
            if(val.isEmpty()) {
                mdcKey = null;
            } else {
                mdcKey = val;
            }
        }
        val = filterConfig.getInitParameter("attributeName");
        if(val != null) {
            if(val.isEmpty()) {
                attributeName = null;
            } else {
                attributeName = val;
            }
        }
        val = filterConfig.getInitParameter("headerName");
        if(val != null) {
            if(val.isEmpty()) {
                headerName = null;
            } else {
                headerName = val;
            }
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) { 
            String requestId = generateID();
            if(mdcKey != null) {
                MDC.put(mdcKey, requestId);
            }
            if(attributeName != null) {
                request.setAttribute(attributeName, requestId);
            }
            if(headerName != null) {
                ((HttpServletResponse)response).addHeader(headerName, requestId);
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if(mdcKey != null) {
                MDC.remove(mdcKey);
            }
            if(attributeName != null) {
                request.removeAttribute(attributeName);
            }
        }
    }
    
    @Override
    public void destroy() {}
    
    String generateID() {
        UUID id = UUID.randomUUID();
        byte[] bytes = ByteBuffer.allocate(16).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).array();
        return encoder.encodeToString(bytes);
    }

}
