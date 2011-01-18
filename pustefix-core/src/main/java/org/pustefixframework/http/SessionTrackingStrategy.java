package org.pustefixframework.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SessionTrackingStrategy {

    public void init(SessionTrackingStrategyContext context);
    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;
    
}
