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

package org.pustefixframework.container.spring.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for all HTTP request filters.
 * Objects implementing this interface, that are placed in the 
 * {@link org.springframework.context.ApplicationContext} will
 * be detected by Pustefix and invoked on each request.
 * These objects may also implement the 
 * {@link org.springframework.core.Ordered} to provide the order 
 * in which the filters should be chained.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface HttpRequestFilter {

    /**
     * Handles a request, processing and / or passing it to the next filter
     * in the chain.
     * Implementations may replace or decorate the request and response 
     * objects with their own implementations in order to intercept 
     * actions on these objects.
     * 
     * @param request represents the client request
     * @param response represents the response send to the client
     * @param chain provides a method to pass the request to the next filter in 
     *  the chain
     * @throws IOException
     * @throws ServletException
     */
    void doFilter(HttpServletRequest request, HttpServletResponse response, HttpRequestFilterChain chain) throws IOException, ServletException;
}
