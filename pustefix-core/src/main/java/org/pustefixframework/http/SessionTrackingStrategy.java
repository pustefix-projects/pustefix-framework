package org.pustefixframework.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SessionTrackingStrategy {

    public void handleRequestByStrategy(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;
    
}
