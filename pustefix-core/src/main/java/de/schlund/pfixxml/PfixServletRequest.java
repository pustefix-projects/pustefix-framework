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
 *
 */
package de.schlund.pfixxml;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface PfixServletRequest {

    public static final String PAGEPARAM = "__page";

    public long getCreationTimeStamp();

    /**
     * Returns the value of the request-attribute that is stored under the key
     * {@link #ATTR_LASTEXCEPTION ATTR_LASTEXCEPTION}
     */
    public Throwable getLastException();

    /**
     * Stores the given <code>exception</code>-object as an attribute in the request,
     * under the key of {@link #ATTR_LASTEXCEPTION ATTR_LASTEXCEPTION}.
     * @param lastException The value to assign lastException.
     */
    public void setLastException(Throwable lastException);

    /**
     * Retrieve the server name form the orginal request
     * @return the name
     */
    public String getOriginalServerName();

    /**
     * Retrieve the query string from the orginal request
     * @return the query string
     */
    public String getOriginalQueryString();

    /**
     * Retrieve the scheme from the orginal request
     * @return the scheme
     */
    public String getOriginalScheme();

    /**
     * Retrieve the request uri from the orginal request
     * @return the uri
     */
    public String getOriginalRequestURI();

    /**
     * Retrieve the port number from the orginal request
     * @return the port
     */
    public int getOriginalServerPort();

    /**
     * Update the servlet request. After calling this method
     * the request data used by constructor are accesible
     * only by the 'getOriginal' methods.
     * @param req the new request
     */
    public void updateRequest(HttpServletRequest req);

    /**
     * Retrieve the current request.
     * @return the current request.
     */
    public HttpServletRequest getRequest();

    /**
     * Determine if any error happened.
     * @return true if error happened, else false
     */
    public boolean errorHappened();

    /**
     * Retrieve all exceptions that happened during multipart-handling
     * @return a list containing all exceptions
     */
    public List<Exception> getAllExceptions();

    /**
     * Retrieve all cookies from the current request
     * @return an array containing all cookies
     */
    public Cookie[] getCookies();

    /**
     * Retrieve the path information from the current request
     * @return the path info
     */
    public String getPathInfo();

    /**
     * Retrieve the translated path from the current request
     * @return the translated path
     */
    public String getPathTranslated();

    /**
     * Retrieve the query string from the current request
     * @return the query string
     */
    public String getQueryString();

    /**
     * Retrieve the session id belonging to the current request
     * @return the session id
     */
    public String getRequestedSessionId();

    /**
     * Retrieve the request uri from the current request
     * @return the request uri
     */
    public String getRequestURI();

    /**
     * Retrieve the context path from the current request
     * @return the context path
     */
    public String getContextPath();

    /**
     * Retrieve the servlet path from the current request
     * @return the servlet path
     */
    public String getServletPath();

    /**
     * Retrieve the session belonging to the current request
     * @param create if true a new session will be created if not exists
     * in the current request, if false the orginal session will be returned
     * @return the http session
     */
    public HttpSession getSession(boolean create);

    /**
     * Retrieve whether the requested session id is valid
     * @return true if valid, else false
     */
    public boolean isRequestedSessionIdValid();

    /**
     * Retrieve the remote ip-address from the current request
     * @return the remote address
     */
    public String getRemoteAddr();

    /**
     * Retrieve the remote host from the current request
     * @return the remote host
     */
    public String getRemoteHost();

    /**
     * Retrieve the scheme from the current request
     * @return the scheme
     */
    public String getScheme();

    /**
     * Retrieve the port number from the current request
     * @return the port number
     */
    public int getServerPort();

    /**
     * Retrieve the server name from the current request
     * @return the name
     */
    public String getServerName();

    /**
     *  Gets the part of this request's URI that refers to the servlet being invoked.
     * @ the servlet being invoked, as contained in this request's URI
     */
    public String getServletName();

    /**
     * Retrieve a {@link RequestParam} according to the name parameter
     * @param the name used as a key
     * @return the request param or null if not exists
     */
    public RequestParam getRequestParam(String name);

    /**
     * Retrieve all request params according to the name parameter
     * @param the name used as a key
     * @return an array containing all request params or null if not exists
     */
    public RequestParam[] getAllRequestParams(String name);

    /**
     * Retrieve all names of all request params
     * @return an array containing all names for all request params.
     */
    public String[] getRequestParamNames();

    /**
     * Extracts page name from pathinfo of this request.
     * 
     * @return page name for the given request or <code>null</code>
     *         if no page name has been specified or specified page name
     *         has invalid scheme
     */
    public String getPageName();

}