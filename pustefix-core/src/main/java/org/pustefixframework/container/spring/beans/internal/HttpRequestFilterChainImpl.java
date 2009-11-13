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

package org.pustefixframework.container.spring.beans.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.container.spring.http.HttpRequestFilter;
import org.pustefixframework.container.spring.http.HttpRequestFilterChain;

/**
 * Filter chain implementation. Uses filters from a set and a special, 
 * final filter to give control back to the caller. This filter will 
 * get <code>null</code> instead of a reference to the filter chain.
 * This implementation is not thread-safe. A separate instance has to be
 * created for each request. The list of filters may not be modified 
 * as long as there is a filter chain object using it.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class HttpRequestFilterChainImpl implements HttpRequestFilterChain {

    private HttpRequestFilter finalFilter;

    private Iterator<? extends HttpRequestFilter> filterIterator;

    public HttpRequestFilterChainImpl(Collection<? extends HttpRequestFilter> filters, HttpRequestFilter finalFilter) {
        this.finalFilter = finalFilter;
        this.filterIterator = filters.iterator();
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (filterIterator.hasNext()) {
            filterIterator.next().doFilter(request, response, this);
        } else {
            finalFilter.doFilter(request, response, null);
        }
    }

}
