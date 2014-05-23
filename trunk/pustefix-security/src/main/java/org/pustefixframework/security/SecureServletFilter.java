package org.pustefixframework.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecureServletFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {   
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        
        if(req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
        
            req = new SecureRequestWrapper((HttpServletRequest)req);
            res = new SecureResponseWrapper((HttpServletResponse)res);

        }
        
        chain.doFilter(req, res);

    }

    public void destroy() {
    }

}
