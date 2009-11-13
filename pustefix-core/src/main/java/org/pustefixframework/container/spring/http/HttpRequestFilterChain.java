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
 * Provides access to the next filter in the filter chain.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface HttpRequestFilterChain {

    /**
     * Calls the <code>doFilter()</code> method of the next filter 
     * in the filter chain or passes the request to the actual request 
     * handler if the calling filter is the last filter in the 
     * filter chain.
     * 
     * @param request represents the client request
     * @param response represents the response send to the client
     * @throws IOException
     * @throws ServletException
     */
    void doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}
