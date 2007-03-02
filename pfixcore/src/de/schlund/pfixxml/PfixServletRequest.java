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
 *
 */
package de.schlund.pfixxml;




import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.multipart.MultipartHandler;
import de.schlund.pfixxml.multipart.PartData;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.pfixxml.serverutil.SessionHelper;

/**
 * <p>This class is an abstraction of a servlet request. Is provides wrapper functions
 * on a {@link javax.servlet.http.HttpServletRequest} but has also the possibility
 * to update the request and to retrieve both the current and orginal data.
 * Classes in the Pustefix system should use this class and not a
 * <code>HttpServletRequest</code>.</p>
 * <p>Note: All method with a 'getOrginal' prefix work on the orginal request,
 * all others methods work on the currently set request, which may
 * be the original request used when the instance was created, or
 * any other request that has been set via <code>updateRequest()</code>.</p>
 *
 *
 * Created: Tue May  7 23:55:50 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */
public class PfixServletRequest {

    //~ Instance/static variables ..................................................................

    private static final String   SBMT_PREFIX         = "__SBMT:";
    private static final String   SYNT_PREFIX         = "__SYNT:";
    private static final String   PROP_TMPDIR         = "pfixservletrequest.tmpdir";
    private static final String   PROP_MAXPARTSIZE    = "pfixservletrequest.maxpartsize";
    private static final String   ATTR_LASTEXCEPTION  = "REQ_LASTEXCEPTION";
    public  static final String   PAGEPARAM           = "__page";
    private static String         DEF_MAXPARTSIZE     = "" + (10 * 1024 * 1024); // 10 MB
    private HashMap               parameters          = new HashMap();
    private Logger                LOG                 = Logger.getLogger(this.getClass());
    private ArrayList             multiPartExceptions = new ArrayList();
    private String                servername;
    private String                querystring;
    private String                scheme;
    private String                uri;
    private HttpSession           session          = null;
    private int                   serverport;
    private HttpServletRequest    request;
    private long                  starttime        = 0;


    public long getCreationTimeStamp() {
        return starttime;
    }
    
   
    //~ Constructors ...............................................................................

    /**
     * Constructor for creating a PfixServletRequest
     * @param req the orginal servlet request
     * @param properties
     * @param cUtil
     */
    public PfixServletRequest(HttpServletRequest req, Properties properties) {
        PerfEvent pe = new PerfEvent(PerfEventType.PFIXSERVLETREQUEST_INIT);
        pe.start();
        
        starttime   = System.currentTimeMillis();
        getRequestParams(req, properties);
        servername  = req.getServerName();
        querystring = req.getQueryString();
        scheme      = req.getScheme();
        uri         = req.getRequestURI();
        serverport  = req.getServerPort();
        request     = req;
        session     = req.getSession(false);

        pe.setIdentfier(uri);
        pe.save();
    }

    //~ Methods ....................................................................................

    /**
     * Returns the value of the request-attribute that is stored under the key
     * {@link #ATTR_LASTEXCEPTION ATTR_LASTEXCEPTION}
     */
    public Throwable getLastException() {
        return (Throwable) request.getAttribute(ATTR_LASTEXCEPTION);
    }

    /**
     * Stores the given <code>exception</code>-object as an attribute in the request,
       * under the key of {@link #ATTR_LASTEXCEPTION ATTR_LASTEXCEPTION}.
     * @param lastException The value to assign lastException.
     */
    public void setLastException(Throwable lastException) {
        request.setAttribute(ATTR_LASTEXCEPTION, lastException);
    }

    /**
     * Retrieve the server name form the orginal request
     * @return the name
     */
    public String getOriginalServerName() {
        return servername;
    }

    /**
     * Retrieve the query string from the orginal request
     * @return the query string
     */
    public String getOriginalQueryString() {
        return querystring;
    }

    /**
     * Retrieve the scheme from the orginal request
     * @return the scheme
     */
    public String getOriginalScheme() {
        return scheme;
    }

    /**
     * Retrieve the request uri from the orginal request
     * @return the uri
     */
    public String getOriginalRequestURI() {
        return uri;
    }

    /**
     * Retrieve the port number from the orginal request
     * @return the port
     */
    public int getOriginalServerPort() {
        return serverport;
    }

    /**
     * Update the servlet request. After calling this method
     * the request data used by constructor are accesible
     * only by the 'getOriginal' methods.
     * @param req the new request
     */
    public void updateRequest(HttpServletRequest req) {
        this.request = req;
        this.session = req.getSession(false);
    }

    /**
     * Retrieve the current request.
     * @return the current request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Determine if any error happened.
     * @return true if error happened, else false
     */
    public boolean errorHappened() {
        return ! multiPartExceptions.isEmpty();
    }

    /**
     * Retrieve all exceptions that happened during multipart-handling
     * @return a list containing all exceptions
     */
    public List getAllExceptions() {
        return multiPartExceptions;
    }

    // All these methods work on the currently set request, which may
    // be the original request used when the instance was created, or
    // any other request that has been set via updateRequest().
    // Most are just called as the corresponding methods in HttpServletRequest.

    /**
     * Retrieve all cookies from the current request
     * @return an array containing all cookies
     */
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    /**
     * Retrieve the path information from the current request
     * @return the path info
     */
    public String getPathInfo() {
        return request.getPathInfo();
    }

    /**
     * Retrieve the translated path from the current request
     * @return the translated path
     */
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    /**
     * Retrieve the query string from the current request
     * @return the query string
     */
    public String getQueryString() {
        return request.getQueryString();
    }

    /**
     * Retrieve the session id belonging to the current request
     * @return the session id
     */
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    /**
     * Retrieve the request uri from the current request
     * @return the request uri
     */
    public String getRequestURI() {
        return SessionHelper.encodeURI(request);
    }

    /**
     * Retrieve the context path from the current request
     * @return the context path
     */
    public String getContextPath() {
        return request.getContextPath();
    }

    /**
     * Retrieve the servlet path from the current request
     * @return the servlet path
     */
    public String getServletPath() {
        return request.getServletPath();
    }

    /**
     * Retrieve the session belonging to the current request
     * @param create if true a new session will be created if not exists
     * in the current request, if false the orginal session will be returned
     * @return the http session
     */
    public HttpSession getSession(boolean create) {
        if (! create) {
            return session;
        } else {
            return request.getSession(true);
        }
    }

    /**
     * Retrieve whether the requested session id is valid
     * @return true if valid, else false
     */
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    /**
     * Retrieve the remote ip-address from the current request
     * @return the remote address
     */
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    /**
     * Retrieve the remote host from the current request
     * @return the remote host
     */
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    /**
     * Retrieve the scheme from the current request
     * @return the scheme
     */
    public String getScheme() {
        return request.getScheme();
    }

    /**
     * Retrieve the port number from the current request
     * @return the port number
     */
    public int getServerPort() {
        return request.getServerPort();
    }

    /**
     * Retrieve the server name from the current request
     * @return the name
     */
    public String getServerName() {
        return request.getServerName();
    }


    /**
     *  Gets the part of this request's URI that refers to the servlet being invoked.
     * @ the servlet being invoked, as contained in this request's URI
     */
    public String getServletName() {
        return request.getServletPath();
    }

    /**
     * Retrieve a {@link RequestParam} according to the name parameter
     * @param the name used as a key
     * @return the request param or null if not exists
     */
    public RequestParam getRequestParam(String name) {
        RequestParam[] params = (RequestParam[]) parameters.get(name);
        if (params != null && params.length > 0) {
            return params[0];
        } else {
            return null;
        }
    }

    /**
     * Retrieve all request params according to the name parameter
     * @param the name used as a key
     * @return an array containing all request params or null if not exists
     */
    public RequestParam[] getAllRequestParams(String name) {
        RequestParam[] params = (RequestParam[]) parameters.get(name);
        if (params != null) {
            return params;
        } else {
            return null;
        }
    }

    // ---------------------------------- //

    /**
     * Retrieve all names of all request params
     * @return an array containing all names for all request params.
     */
    public String[] getRequestParamNames() {
        return (String[]) parameters.keySet().toArray(new String[]{});
    }


    private void getRequestParams(HttpServletRequest req, Properties properties) {
        String  type     = req.getContentType();
        HashSet allnames = new HashSet();
        if (type != null && type.toLowerCase().startsWith(MultipartHandler.MULTI_FORM_DATA)) {
            allnames.addAll(handleMulti(req, properties));
        }
        for (Enumeration enm = req.getParameterNames(); enm.hasMoreElements();) {
            String key = (String) enm.nextElement();
            allnames.add(key);
            String[] data = req.getParameterValues(key);
            LOG.debug("* [NORMAL] Found parameters for key '" + key + "' count=" + data.length);
            RequestParam[] multiparams = (RequestParam[]) parameters.get(key);
            RequestParam[] params;
            if (multiparams == null) {
                params = new RequestParam[data.length];
            } else {
                params = new RequestParam[data.length + multiparams.length];
            }
            for (int i = 0; i < data.length; i++) {
                RequestParam param = new SimpleRequestParam(data[i]);
                LOG.debug("          " + i + ") Type: NORMAL Value: " + param.getValue());
                params[i] = param;
            }
            if (multiparams != null) {
                LOG.debug("          **** MULTI is not null...");
                for (int i = data.length; i < (data.length + multiparams.length); i++) {
                    PartData pdat = (PartData) multiparams[i - data.length];
                    LOG.debug("          " + i + ") Type: " + pdat.getType() + " Value: "
                              + pdat.getValue());
                    params[i] = multiparams[i - data.length];
                }
            }
            parameters.put(key, params);
        }
        generateSynthetics(req,allnames);
    }

    private HashSet handleMulti(HttpServletRequest req, Properties properties) {
        String  tmpdir = properties.getProperty(PROP_TMPDIR);
        HashSet names = new HashSet();
        if (tmpdir == null || tmpdir.equals("")) {
            tmpdir = System.getProperty(AbstractXMLServlet.DEF_PROP_TMPDIR);
        }
        
        String maxsize = properties.getProperty(PROP_MAXPARTSIZE);
        if (maxsize == null || maxsize.equals("")) {
            maxsize = DEF_MAXPARTSIZE;
        }
        MultipartHandler multi = new MultipartHandler(req, tmpdir);
        multi.setMaxPartSize((new Long(maxsize)).longValue());
        try {
            multi.parseRequest();
            multiPartExceptions.addAll(multi.getExceptionList());
        } catch (Exception e) {
            multiPartExceptions.add(e);
        }
        for (Enumeration enm = multi.getParameterNames(); enm.hasMoreElements();) {
            String key = (String) enm.nextElement();
            names.add(key);
            List       values = multi.getAllParameter(key);
            PartData[] data = (PartData[]) values.toArray(new PartData[]{});
            LOG.debug("* [MULTI] Found parameters for key '" + key + "' count=" + data.length);
            for (int i = 0; i < data.length; i++) {
                PartData tmp = data[i];
                LOG.debug("          " + i + ") Type: " + tmp.getType() + " Value: "
                          + tmp.getValue());
            }
            parameters.put(key, data);
        }
        return names;
    }

    private void generateSynthetics(HttpServletRequest req, HashSet allnames) {
        HashSet prefixes = new HashSet();
        for (Iterator i = allnames.iterator(); i.hasNext();) {
            String name = (String) i.next();
            if (name.startsWith(SBMT_PREFIX)) {
                int start = SBMT_PREFIX.length();
                int end   = name.lastIndexOf(":");
                if (end > start) {
                    String prefix = name.substring(start, end);
                    if (prefix != null && !prefix.equals("")) {
                        prefixes.add(prefix);
                    }
                }
            }
        }
        for (Iterator i = prefixes.iterator(); i.hasNext();) {
            String prefix = SYNT_PREFIX + (String) i.next() + ":";
            for (Iterator j = allnames.iterator(); j.hasNext();) {
                String name = (String) j.next();
                if (name.startsWith(prefix) && (name.length() > prefix.length())) {
                    String         key    = name.substring(prefix.length());
                    RequestParam[] values = (RequestParam[]) parameters.get(name);
                    LOG.debug("    * [EMB/" + name + "]  >> Key is " + key);
                    if (values != null && values.length > 0) {
                        RequestParam[] newvals = new RequestParam[values.length];
                        for (int k = 0; k < values.length ; k++) {
                            LOG.debug("         Adding value: " + values[k].getValue());
                            newvals[k] = new SimpleRequestParam(values[k].getValue());
                            newvals[k].setSynthetic(true);
                        }
                        // Eeeek, how to concatenate arrays in an elegant way?
                        RequestParam[] oldvals = (RequestParam[]) parameters.get(key);
                        RequestParam[] finvals = newvals;
                        if (oldvals != null && oldvals.length > 0) {
                            finvals = new RequestParam[oldvals.length + newvals.length];
                            for (int k = 0; k < newvals.length; k++) {
                                finvals[k] = newvals[k];
                            }
                            for (int k = 0; k < oldvals.length; k++) {
                                finvals[k + newvals.length] = oldvals[k];
                            }
                        }
                        parameters.put(key, finvals);
                    }
                }
            }
        }
    }
    
    /**
     * Extracts page name from pathinfo of this request.
     * 
     * @return page name for the given request or <code>null</code>
     *         if no page name has been specified or specified page name
     *         has invalid scheme
     */
    public String getPageName() {
        String       pagename = "";
        String       pathinfo = getPathInfo();
        RequestParam name     = getRequestParam(PAGEPARAM);
        if (name != null && !name.getValue().equals("")) {
            pagename = name.getValue();
        } else if (pathinfo != null && !pathinfo.equals("") && 
                   pathinfo.startsWith("/") && pathinfo.length() > 1) {
            pagename = pathinfo.substring(1);
        } else {
            return null;
        }
        // We must remove any '::' that may have slipped in through the request
        if (pagename.indexOf("::") > 0) {
            pagename = pagename.substring(0, pagename.indexOf("::"));
        }
        if (pagename.length() > 0) {
            return pagename;
        } else {
            return null;
        }
    }
    
} // PfixServletRequest
