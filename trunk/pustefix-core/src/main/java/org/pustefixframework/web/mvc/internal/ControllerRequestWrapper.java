/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.web.mvc.internal;

import javax.servlet.http.HttpServletRequestWrapper;

import org.pustefixframework.http.AbstractPustefixRequestHandler;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * A HttpServletRequest wrapper used to fulfill AnnotationMethodHandlerAdapter's
 * handle method contract, e.g. maps alias paths to internal paths (to be done)
 */
public class ControllerRequestWrapper extends HttpServletRequestWrapper {
    
    private PfixServletRequest pfixRequest;
    private String pageName;
    
    public ControllerRequestWrapper(PfixServletRequest pfixRequest, String pageName) {
        super(pfixRequest.getRequest());
        this.pfixRequest = pfixRequest;
        this.pageName = pageName;
    }

    @Override
    public String getRequestURI() {
        String uri = "/" + pageName;
        String additionalPath = (String)pfixRequest.getRequest().getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_PAGE_ADDITIONAL_PATH);
        if(additionalPath != null && additionalPath.length() > 0) {
            uri += additionalPath;
        }
        return uri;
    }

}
