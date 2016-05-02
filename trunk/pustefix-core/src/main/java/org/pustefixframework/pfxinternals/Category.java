package org.pustefixframework.pfxinternals;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public interface Category {
    
    public void model(Element root, HttpServletRequest request, PageContext pageContext);

}
