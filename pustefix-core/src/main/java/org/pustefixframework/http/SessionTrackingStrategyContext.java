package org.pustefixframework.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.serverutil.SessionAdmin;

public interface SessionTrackingStrategyContext {

    public boolean wantsCheckSessionIdValid();
    public boolean needsSession();
    public boolean allowSessionCreate();
    public boolean needsSSL(PfixServletRequest preq) throws ServletException;
    public ServletManagerConfig getServletManagerConfig();
    public void callProcess(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;
    public SessionAdmin getSessionAdmin();
    
}
