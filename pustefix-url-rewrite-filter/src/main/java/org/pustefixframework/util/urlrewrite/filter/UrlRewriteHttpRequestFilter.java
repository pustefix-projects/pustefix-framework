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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.http.HttpRequestFilter;
import org.pustefixframework.http.HttpRequestFilterChain;
import org.pustefixframework.util.urlrewrite.filter.internal.UrlRewriteHttpServletRequest;
import org.pustefixframework.util.urlrewrite.filter.internal.UrlRewriteHttpServletResponse;
import org.pustefixframework.util.urlrewrite.io.ByteNode;
import org.pustefixframework.util.urlrewrite.io.ByteNodeUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

/**
 * HTTP request filter that rewrites URLs. 
 * The filter uses a map of paths that should be rewritten in 
 * requests and responses. This can be used to localize path names 
 * of an application without having to change the application itself.
 * The configuration file is set using the {@link #setConfigFile(Resource)} 
 * method.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UrlRewriteHttpRequestFilter implements HttpRequestFilter, InitializingBean, ServletContextAware {

    private ByteNode<byte[]> internToExtern;

    private ByteNode<byte[]> externToIntern;

    private ServletContext servletContext;

    private Resource configFile;

    public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpRequestFilterChain chain) throws IOException, ServletException {
        request = new UrlRewriteHttpServletRequest((HttpServletRequest) request, externToIntern, servletContext);
        response = new UrlRewriteHttpServletResponse((HttpServletResponse) response, internToExtern);
        chain.doFilter(request, response);
    }

    public void afterPropertiesSet() throws Exception {
        if (configFile == null) {
            throw new IllegalStateException("Config file has to be set first.");
        }

        Properties properties = new Properties();
        properties.load(configFile.getInputStream());

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

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Sets the path to the property file that contains the path names, that 
     * should be rewritten. This property files uses the original (internal) 
     * path name as the key and the rewritten (external) path name as the value 
     * of each key value pair.
     * 
     * @param resource property file
     */
    public void setConfigFile(Resource resource) {
        this.configFile = resource;
    }
}
