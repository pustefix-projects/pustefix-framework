/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.http.SessionContextStore;

import de.schlund.pfixcore.workflow.Context;

/**
 * Abstract base class that can be used to derive a servlet filter that is aware
 * of Pustefix's {@link Context} object. Implementations have to override the 
 * {@link #doFilter(HttpServletRequest, HttpServletResponse, FilterChain, Context)}
 * method to implement the actual filter code.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractContextServletFilter implements Filter {
    
    private String contextIdentifier;
    
    public void init(FilterConfig config) throws ServletException {
        contextIdentifier = config.getInitParameter("contextRef");
        if (contextIdentifier == null) {
            throw new ServletException("Init parameter contextRef is not set for serlvet filter " + config.getFilterName());
        }
    }
    
    public void destroy() {
        // Nothing to do
    }

    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {
        if (sreq instanceof HttpServletRequest && sres instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) sreq;
            HttpServletResponse res = (HttpServletResponse) sres;
            
            Context context = null;
            HttpSession session = req.getSession(false);
            if (session != null) {
                context = SessionContextStore.getInstance(session).getContext(this.contextIdentifier);
            }
            
            this.doFilter(req, res, chain, context);            
        } else {
            chain.doFilter(sreq, sres);
        }
    }

    /**
     * Overwrite this method to implement filter. The method code has either to
     * handle the request and return a response or to call the 
     * <code>doFilter()</code> method on the supplied <code>FilterChain</code>
     * object. The method will only be called, if the request is a HTTP request.
     * 
     * @param req request being filtered. May be wrapped by the filter to change
     *  values / bahavior of the object.
     * @param res response supplied by the container. May be wrapped by the filter 
     *  to add new functionality (e.g. filtering of the output).
     * @param chain object representing the filter chain. Provides methods to
     *  forward a request to the next filter in the chain. 
     * @param context Pustefix context object for the current session. May be
     *  <code>null</code> if no valid session is available for the current request.
     * @throws IOException
     * @throws ServletException
     */
    abstract protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Context context) throws IOException, ServletException;

}
