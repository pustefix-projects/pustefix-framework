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

package org.pustefixframework.util.urlrewrite.filter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.util.urlrewrite.filter.internal.UrlRewriteHttpServletRequest;
import org.pustefixframework.util.urlrewrite.filter.internal.UrlRewriteHttpServletResponse;
import org.pustefixframework.util.urlrewrite.io.ByteNode;
import org.pustefixframework.util.urlrewrite.io.ByteNodeUtil;

/**
 * Servlet filter that rewrites URLs. 
 * The filter uses a map of paths that should be rewritten in 
 * requests and responses. This can be used to localize path names 
 * of an application without having to change the application itself.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UrlRewriteFilter implements Filter {

    private ByteNode<byte[]> internToExtern;

    private ByteNode<byte[]> externToIntern;

    private ServletContext servletContext;

    public void destroy() {
        // No clean-up needed
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            request = new UrlRewriteHttpServletRequest((HttpServletRequest) request, externToIntern, servletContext);
            response = new UrlRewriteHttpServletResponse((HttpServletResponse) response, internToExtern);
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();

        String configFile = filterConfig.getInitParameter("configFile");
        if (configFile == null) {
            throw new ServletException("Mandatory init parameter \"configFile\" has not been specified.");
        }
        URL configResource;
        try {
            configResource = servletContext.getResource(configFile);
        } catch (MalformedURLException e) {
            throw new ServletException("Invalid value \"" + configFile + "\" for init parameter \"configFile\".", e);
        }
        if (configResource == null) {
            throw new ServletException("Resource \"" + configFile + "\" could not be found in servlet context.");
        }
        Properties properties = new Properties();
        try {
            properties.load(configResource.openStream());
        } catch (IOException e) {
            throw new ServletException("Error while reading configuration file \"" + configFile + "\".", e);
        }

        HashMap<String, String> internToExtern = new HashMap<String, String>();
        HashMap<String, String> externToIntern = new HashMap<String, String>();

        Enumeration<?> en = properties.propertyNames();
        while (en.hasMoreElements()) {
            String propertyName = (String) en.nextElement();
            String propertyValue = properties.getProperty(propertyName);
            propertyName = propertyName.trim();
            propertyValue = propertyValue.trim();
            internToExtern.put(propertyName, propertyValue);
            externToIntern.put(propertyValue, propertyName);
        }

        this.internToExtern = ByteNodeUtil.generateByteNodeTree(internToExtern);
        this.externToIntern = ByteNodeUtil.generateByteNodeTree(externToIntern);
    }

}
