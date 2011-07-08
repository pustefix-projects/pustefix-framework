package de.schlund.pfixxml;

import javax.servlet.http.HttpServletRequest;

public interface PageAliasResolver {

    public String getPageName(String pageAlias, HttpServletRequest req);
    
}
