package org.pustefixframework.pfxinternals;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {

    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException;
    
}
