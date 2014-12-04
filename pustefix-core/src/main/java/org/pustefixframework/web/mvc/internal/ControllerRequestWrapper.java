package org.pustefixframework.web.mvc.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ControllerRequestWrapper extends HttpServletRequestWrapper {
    
    public ControllerRequestWrapper(HttpServletRequest request) {
        super(request);
    }

}
