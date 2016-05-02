package org.pustefixframework.pfxinternals;

import javax.servlet.http.HttpServletRequest;

import org.pustefixframework.util.FrameworkInfo;
import org.w3c.dom.Element;

public class FrameworkCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {
        
        Element root = parent.getOwnerDocument().createElement("framework");
        parent.appendChild(root);
        root.setAttribute("version", FrameworkInfo.getVersion());
        root.setAttribute("scmurl", FrameworkInfo.getSCMUrl());
    }
    
}
