package org.pustefixframework.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class SecureRequestWrapper extends HttpServletRequestWrapper {

    public SecureRequestWrapper(HttpServletRequest req) {
        super(req);
    }
    
}
