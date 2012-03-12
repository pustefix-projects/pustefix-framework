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
import org.pustefixframework.http.AbstractPustefixXMLRequestHandler;

import de.schlund.pfixxml.multipart.MultipartHandler;
import de.schlund.pfixxml.multipart.PartData;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.util.CookieUtils;

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
public class PfixServletRequestImpl implements PfixServletRequest {

    //~ Instance/static variables ..................................................................

    private static final String   SBMT_PREFIX         = "__SBMT:";
    private static final String   SYNT_PREFIX         = "__SYNT:";
    private static final String   PROP_TMPDIR         = "pfixservletrequest.tmpdir";
    private static final String   PROP_MAXPARTSIZE    = "pfixservletrequest.maxpartsize";
    private static final String   ATTR_LASTEXCEPTION  = "REQ_LASTEXCEPTION";
    private static final String   DEF_MAXPARTSIZE     = "" + (10 * 1024 * 1024); // 10 MB
    private HashMap<String, RequestParam[]> parameters = new HashMap<String, RequestParam[]>();
    private Logger                LOG                 = Logger.getLogger(this.getClass());
    private List<Exception>             multiPartExceptions = new ArrayList<Exception>();
    private String                servername;
    private String                querystring;
    private String                scheme;
    private String                uri;
    private HttpSession           session          = null;
    private int                   serverport;
    private HttpServletRequest    request;
    private long                  starttime        = 0;
    private PageAliasResolver     pageAliasResolver;
    private String                internalPageName;
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getCreationTimeStamp()
     */
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
    public PfixServletRequestImpl(HttpServletRequest req, Properties properties) {
    
        starttime   = System.currentTimeMillis();
        getRequestParams(req, properties);
        servername  = req.getServerName();
        querystring = req.getQueryString();
        scheme      = req.getScheme();
        uri         = req.getRequestURI();
        serverport  = req.getServerPort();
        request     = req;
        session     = req.getSession(false);

    }
    
    public PfixServletRequestImpl(HttpServletRequest req, Properties properties, PageAliasResolver pageAliasResolver) {
        this(req, properties);
        this.pageAliasResolver = pageAliasResolver;
    }

    //~ Methods ....................................................................................

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getLastException()
     */
    public Throwable getLastException() {
        return (Throwable) request.getAttribute(ATTR_LASTEXCEPTION);
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#setLastException(java.lang.Throwable)
     */
    public void setLastException(Throwable lastException) {
        request.setAttribute(ATTR_LASTEXCEPTION, lastException);
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getOriginalServerName()
     */
    public String getOriginalServerName() {
        return servername;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getOriginalQueryString()
     */
    public String getOriginalQueryString() {
        return querystring;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getOriginalScheme()
     */
    public String getOriginalScheme() {
        return scheme;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getOriginalRequestURI()
     */
    public String getOriginalRequestURI() {
        return uri;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getOriginalServerPort()
     */
    public int getOriginalServerPort() {
        return serverport;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#updateRequest(javax.servlet.http.HttpServletRequest)
     */
    public void updateRequest(HttpServletRequest req) {
        this.request = req;
        this.session = req.getSession(false);
        internalPageName = null;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRequest()
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#errorHappened()
     */
    public boolean errorHappened() {
        return ! multiPartExceptions.isEmpty();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getAllExceptions()
     */
    public List<Exception> getAllExceptions() {
        return multiPartExceptions;
    }

    // All these methods work on the currently set request, which may
    // be the original request used when the instance was created, or
    // any other request that has been set via updateRequest().
    // Most are just called as the corresponding methods in HttpServletRequest.

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return CookieUtils.getCookies(request);
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return request.getPathInfo();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getQueryString()
     */
    public String getQueryString() {
        return request.getQueryString();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        return SessionHelper.encodeURI(request);
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getContextPath()
     */
    public String getContextPath() {
        return request.getContextPath();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getServletPath()
     */
    public String getServletPath() {
        return request.getServletPath();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean create) {
        if (! create) {
            return session;
        } else {
            return request.getSession(true);
        }
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        String forward = request.getHeader("X-Forwarded-For");
        if (forward != null && !forward.equals("")) {
            return forward;
        } else {
            return request.getRemoteAddr();
        }
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getScheme()
     */
    public String getScheme() {
        return request.getScheme();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getServerPort()
     */
    public int getServerPort() {
        return request.getServerPort();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getServerName()
     */
    public String getServerName() {
        String forward = request.getHeader("X-Forwarded-Server");
        if (forward != null && !forward.equals("")) {
            return forward;
        } else {
            return request.getServerName();
        }
    }


    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getServletName()
     */
    public String getServletName() {
        return request.getServletPath();
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRequestParam(java.lang.String)
     */
    public RequestParam getRequestParam(String name) {
        RequestParam[] params = (RequestParam[]) parameters.get(name);
        if (params != null && params.length > 0) {
            return params[0];
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getAllRequestParams(java.lang.String)
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

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getRequestParamNames()
     */
    public String[] getRequestParamNames() {
        return (String[]) parameters.keySet().toArray(new String[]{});
    }


    private void getRequestParams(HttpServletRequest req, Properties properties) {
        String  type     = req.getContentType();
        HashSet<String> allnames = new HashSet<String>();
        if (type != null && type.toLowerCase().startsWith(MultipartHandler.MULTI_FORM_DATA)) {
            allnames.addAll(handleMulti(req, properties));
        }
        for (Enumeration<?> enm = req.getParameterNames(); enm.hasMoreElements();) {
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

    private HashSet<String> handleMulti(HttpServletRequest req, Properties properties) {
        String  tmpdir = properties.getProperty(PROP_TMPDIR);
        HashSet<String> names = new HashSet<String>();
        if (tmpdir == null || tmpdir.equals("")) {
            tmpdir = System.getProperty(AbstractPustefixXMLRequestHandler.DEF_PROP_TMPDIR);
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
        for (Enumeration<String> enm = multi.getParameterNames(); enm.hasMoreElements();) {
            String key = (String) enm.nextElement();
            names.add(key);
            List<PartData> values = multi.getAllParameter(key);
            PartData[] data = values.toArray(new PartData[]{});
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

    private void generateSynthetics(HttpServletRequest req, HashSet<String> allnames) {
        HashSet<String> prefixes = new HashSet<String>();
        for (Iterator<String> i = allnames.iterator(); i.hasNext();) {
            String name = i.next();
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
        for (Iterator<String> i = prefixes.iterator(); i.hasNext();) {
            String prefix = SYNT_PREFIX + i.next() + ":";
            for (Iterator<String> j = allnames.iterator(); j.hasNext();) {
                String name = j.next();
                if (name.startsWith(prefix) && (name.length() > prefix.length())) {
                    String         key    = name.substring(prefix.length());
                    RequestParam[] values = parameters.get(name);
                    LOG.debug("    * [EMB/" + name + "]  >> Key is " + key);
                    if (values != null && values.length > 0) {
                        RequestParam[] newvals = new RequestParam[values.length];
                        for (int k = 0; k < values.length ; k++) {
                            LOG.debug("         Adding value: " + values[k].getValue());
                            newvals[k] = new SimpleRequestParam(values[k].getValue());
                            newvals[k].setSynthetic(true);
                        }
                        // Eeeek, how to concatenate arrays in an elegant way?
                        RequestParam[] oldvals = parameters.get(key);
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
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.PfixServletRequest#getPageName()
     */
    public String getRequestedPageName() {
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
    
    public String getPageName() {
        if(internalPageName == null) {
            String pageName = getRequestedPageName();
            if(pageName != null) {
                if(pageAliasResolver != null) {
                    internalPageName = pageAliasResolver.getPageName(pageName, request);
                }
            }
        }
        return internalPageName;
    }
    
} // PfixServletRequest
