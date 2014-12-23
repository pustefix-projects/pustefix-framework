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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A HttpServletRequest wrapper used to fulfill AnnotationMethodHandlerAdapter's
 * handle method contract, e.g. maps alias paths to internal paths (to be done)
 */
public class ControllerRequestWrapper extends HttpServletRequestWrapper {
    
    public ControllerRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getPathInfo() {
        //TODO: map alias path to internal path
        return super.getPathInfo();
    }
    
}
