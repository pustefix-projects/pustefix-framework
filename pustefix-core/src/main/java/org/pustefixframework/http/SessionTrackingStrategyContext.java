package org.pustefixframework.http;

import javax.servlet.ServletException;

import de.schlund.pfixxml.PageAliasResolver;
import de.schlund.pfixxml.PfixServletRequest;

public interface SessionTrackingStrategyContext extends PageAliasResolver {

    public boolean wantsCheckSessionIdValid();
    public boolean needsSession();
    public boolean allowSessionCreate();
    public boolean needsSSL(PfixServletRequest preq) throws ServletException;

}
