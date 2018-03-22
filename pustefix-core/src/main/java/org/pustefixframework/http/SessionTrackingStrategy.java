package org.pustefixframework.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SessionTrackingStrategy {

    public boolean handleRequest(HttpServletRequest req, HttpServletResponse res,
            SessionTrackingStrategyContext context) throws ServletException, IOException;
    
}
