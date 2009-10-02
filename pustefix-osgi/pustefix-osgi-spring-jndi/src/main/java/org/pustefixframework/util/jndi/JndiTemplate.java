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

package org.pustefixframework.util.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

import org.springframework.jndi.JndiCallback;

/**
 * Special child implementation of {@link org.springframework.jndi.JndiTemplate}
 * that takes care of setting the class loader the the web application class 
 * loader, when running inside a servlet bridge OSGi environment.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class JndiTemplate extends org.springframework.jndi.JndiTemplate {

    @Override
    protected Context createInitialContext() throws NamingException {
        ClassLoader webApplicationClassLoader = getWebApplicationClassLoader();
        if (webApplicationClassLoader != null) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(webApplicationClassLoader);
            try {
                return super.createInitialContext();
            } finally {
                Thread.currentThread().setContextClassLoader(ccl);
            }
        } else {
            return super.createInitialContext();
        }
    }

    @Override
    public Object execute(JndiCallback contextCallback) throws NamingException {
        ClassLoader webApplicationClassLoader = getWebApplicationClassLoader();
        if (webApplicationClassLoader != null) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(webApplicationClassLoader);
            try {
                return super.execute(contextCallback);
            } finally {
                Thread.currentThread().setContextClassLoader(ccl);
            }
        } else {
            return super.execute(contextCallback);
        }
    }

    private ClassLoader getWebApplicationClassLoader() {
        Class<?> clazz;
        try {
            clazz = Class.forName("org.eclipse.equinox.servletbridge.BridgeServlet");
        } catch (ClassNotFoundException e) {
            return null;
        }
        return clazz.getClassLoader();
    }
}
