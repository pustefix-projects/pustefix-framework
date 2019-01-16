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
package org.pustefixframework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.web.ServletUtils;

/**
 * Servlet filter which enforces the usage of HTTPS by redirecting all insecure
 * requests to HTTPS. The filter also supports HSTS by adding an according
 * Strict-Transport-Security header to the HTTPS responses.
 */
public class EnforceHTTPSFilter implements Filter {

    private boolean enforce = true;
    private boolean strict = true;
    private long maxAge = 31536000;
    private boolean includeSubDomains = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String value = filterConfig.getInitParameter("enforce");
        if(value != null) {
            enforce = Boolean.parseBoolean(value);
        }
        value = filterConfig.getInitParameter("strict");
        if(value != null) {
            strict = Boolean.parseBoolean(value);
        }
        value = filterConfig.getInitParameter("maxAge");
        if(value != null) {
            maxAge = Long.parseLong(value);
        }
        value = filterConfig.getInitParameter("includeSubDomains");
        if(value != null) {
            includeSubDomains = Boolean.parseBoolean(value);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(enforce) {
            if(request instanceof HttpServletRequest &&  response instanceof HttpServletResponse) {
                HttpServletRequest req = (HttpServletRequest)request;
                HttpServletResponse res = (HttpServletResponse)response;
                if(!req.isSecure()) {
                    res.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                    res.setHeader("Cache-Control", "private");
                    res.setHeader("Location", ServletUtils.getRedirectURL(req, "https"));
                    return;
                } else if(strict && req.getServerPort() == 443) {
                    String value = "max-age=" + maxAge;
                    if(includeSubDomains) {
                        value += "; includeSubDomains";
                    }
                    res.setHeader("Strict-Transport-Security", value);
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Set if HTTPS should be enforced.
     * @param enforce - HTTPS is enforced
     */
    public void setEnforce(boolean enforce) {
        this.enforce = enforce;
    }

    /**
     * Set if HSTS should be enabled, i.e. Strict-Transport-Security header should be set.
     * @param strict - HSTS is enabled
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * Set max-age directive of Strict-Transport-Security header.
     * @param maxAge - max-age value in seconds
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Set includeSubDomains directive of Strict-Transport-Security header.
     * @param includeSubDomains - whether includeSubDomains should be set
     */
    public void setIncludeSubDomains(boolean includeSubDomains) {
        this.includeSubDomains = includeSubDomains;
    }

}
