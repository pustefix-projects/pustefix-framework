package org.pustefixframework.security;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SecureResponseWrapper extends HttpServletResponseWrapper {

    public SecureResponseWrapper(HttpServletResponse res) {
        super(res);
    }
    
    @Override
    public void addHeader(String name, String value) {
        super.addHeader(purgeHeader(name), purgeHeader(value));
    }
    
    @Override
    public void setHeader(String name, String value) {
        super.setHeader(purgeHeader(name), purgeHeader(value));
    }
    
    @Override
    public void sendRedirect(String location) throws IOException {
    	super.sendRedirect(purgeHeader(location));
    }
    
    private static String purgeHeader(String nameOrValue) {
        //Replace linebreaks with spaces to prevent header injection and response splitting
        return Utils.removeLineBreaks(nameOrValue);
    }
    
}
