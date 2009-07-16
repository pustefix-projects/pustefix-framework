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

package org.pustefixframework.http;

import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.framework.Bundle;
import org.osgi.service.http.NamespaceException;


/**
 * The HTTP Service allows other bundles in the OSGi environment to dynamically
 * register resources and servlets into the URI namespace of HTTP Service. A
 * bundle may later unregister its resources or servlets.<br>
 * In contrast to the HTTP service from the OSGi specification, this service
 * allows to use different servlet contexts for different bundles and therefore
 * to run multiple independent web applications with virtual hosts in the same
 * OSGi container.
 */
public interface ExtendedHttpService {
    /**
     * Registers a servlet into the URI namespace.
     * 
     * <p>
     * The alias is the name in the URI namespace of the HTTP Service at which
     * the registration will be mapped.
     * 
     * <p>
     * An alias must begin with slash ('/') and must not end with slash ('/'),
     * with the exception that an alias of the form &quot;/&quot; is used to
     * denote the root alias.
     * 
     * <p>
     * The HTTP Service will call the servlet's <code>init</code> method before
     * returning.
     * 
     * <pre>
     * httpService.registerServlet(&quot;/myservlet&quot;, servlet, initParams, bundle);
     * </pre>
     * 
     * <p>
     * Servlets registered with the same <code>Bundle</code> object will
     * share the same <code>ServletContext</code>.
     * 
     * @param alias name in the URI namespace at which the servlet is registered
     * @param servlet the servlet object to register
     * @param initParams initialization arguments for the servlet or
     *        <code>null</code> if there are none. This argument is used by the
     *        servlet's <code>ServletConfig</code> object.
     * @param bundle the bundle this servlet is registered for. The service
     *            implementation might decide which servlet context to use
     *            based on the bundle that is registering the servlet.
     * @throws NamespaceException if the registration fails because the alias
     *            is already in use.
     * @throws javax.servlet.ServletException if the servlet's <code>init</code>
     *            method throws an exception, or the given servlet object has
     *            already been registered at a different alias.
     * @throws java.lang.IllegalArgumentException if any of the arguments are
     *            invalid
     */
    public void registerServlet(String alias, Servlet servlet,
            Map<String, String> initParams, Bundle bundle)
            throws ServletException, NamespaceException;

    /**
     * Registers resources into the URI namespace.
     * 
     * <p>
     * The alias is the name in the URI namespace of the HTTP Service at which
     * the registration will be mapped. An alias must begin with slash ('/') and
     * must not end with slash ('/'), with the exception that an alias of the
     * form &quot;/&quot; is used to denote the root alias. The name parameter
     * must also not end with slash ('/').
     * <p>
     * For example, suppose the resource name /tmp is registered to the alias
     * /files. A request for /files/foo.txt will map to the resource name
     * /tmp/foo.txt.
     * 
     * <pre>
     * httpservice.registerResources(&quot;/files&quot;, &quot;/tmp&quot;, bundle);
     * </pre>
     * 
     * @param alias name in the URI namespace at which the resources are
     *        registered
     * @param name the base name of the resources that will be registered
     * @param bundle the bundle this servlet is registered for. The service
     *            implementation might decide which servlet context to use
     *            based on the bundle that is registering the servlet.
     * @throws NamespaceException if the registration fails because the alias
     *            is already in use.
     * @throws java.lang.IllegalArgumentException if any of the parameters
     *            are invalid
     */
    public void registerResources(String alias, String name,
            Bundle bundle) throws NamespaceException;

    /**
     * Unregisters a previous registration done by <code>registerServlet</code> or
     * <code>registerResources</code> methods.
     * 
     * <p>
     * After this call, the registered alias in the URI name-space will no
     * longer be available. If the registration was for a servlet, the HTTP
     * Service must call the <code>destroy</code> method of the servlet before
     * returning.
     * <p>
     * If the bundle which performed the registration is stopped or otherwise
     * "unget"s the HTTP Service without calling {@link #unregister}then HTTP
     * Service must automatically unregister the registration. However, if the
     * registration was for a servlet, the <code>destroy</code> method of the
     * servlet will not be called in this case since the bundle may be stopped.
     * {@link #unregister}must be explicitly called to cause the
     * <code>destroy</code> method of the servlet to be called. This can be done
     * in the <code>BundleActivator.stop</code> method of the
     * bundle registering the servlet.
     * 
     * @param alias name in the URI name-space of the registration to unregister
     * @param bundle the bundle the servlet or resource was registered for.
     *            The service implementation might decide which servlet context 
     *            to use based on the bundle that is registering the servlet.
     * @throws java.lang.IllegalArgumentException if there is no registration
     *            for the alias and bundle.
     */
    public void unregister(String alias, Bundle bundle);
}
