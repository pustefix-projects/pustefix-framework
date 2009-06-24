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

package org.pustefixframework.osgi.support.internal;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


/**
 * Proxy class for servlet config. This implementation delegates all method
 * calls to another instance of {@link ServletConfig} that can be changed
 * at runtime.    
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ServletConfigProxy implements ServletConfig {
    
    private ServletConfig servletConfig;
    
    /**
     * Changes the object method calls are delegated to.
     * 
     * @param servletContext target object for method calls
     */
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }
    
    private void checkState() throws IllegalStateException {
        if (this.servletConfig == null) {
            throw new IllegalStateException("Method called on " + ServletConfigProxy.class.getName() + " but target servlet config is not set.");
        }
    }

    public String getInitParameter(String name) {
        checkState();
        return servletConfig.getInitParameter(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getInitParameterNames() {
        checkState();
        return servletConfig.getInitParameterNames();
    }

    public ServletContext getServletContext() {
        checkState();
        return servletConfig.getServletContext();
    }

    public String getServletName() {
        checkState();
        return servletConfig.getServletName();
    }

}
