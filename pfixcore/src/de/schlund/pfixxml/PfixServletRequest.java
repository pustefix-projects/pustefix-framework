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




import de.schlund.pfixxml.multipart.*;
import de.schlund.pfixxml.serverutil.SessionHelper;
import java.text.DecimalFormat;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;


/**
 * PfixServletRequest.java
 *
 *
 * Created: Tue May  7 23:55:50 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * <br/>
 * This class is an abstraction of a servlet request. Is provides wrapper functions
 * on a {@link javax.servlet.http.HttpServletRequest} but has also the possibility
 * to update the request and to retrieve both the current and orginal data. 
 * Classes in the Pustefix system should use this class and not a
 * <code>HttpServletRequest</code>.<br/>
 * Note: All method with a 'getOrginal' prefix work on the orginal request, 
 * all others methods work on the currently set request, which may
 * be the original request used when the instance was created, or
 * any other request that has been set via <code>updateRequest()</code>.
 */
public class PfixServletRequest {

    //~ Instance/static variables ..................................................................

    private static final Category PFXPERF          = Category.getInstance("PFXPERF");
    private static final String   INDENT           = "   ";
    private static final String   DATA_PREFIX      = "__DATA:";
    private static final String   PROP_TMPDIR      = "pfixservletrequest.tmpdir";
    private static final String   PROP_MAXPARTSIZE = "pfixservletrequest.maxpartsize";
    private static DecimalFormat  FORMAT           = new DecimalFormat("0000");
    private static String         DEF_TMPDIR       = "/tmp";
    private static String         DEF_MAXPARTSIZE  = "" + (10 * 1024 * 1024); // 10 MB
    private ArrayList             exceptions       = new ArrayList();
    private HashMap               parameters       = new HashMap();
    private Category              CAT              = Category.getInstance(this.getClass());
    private String                servername;
    private String                querystring;
    private String                scheme;
    private String                uri;
    private HttpSession           session          = null;
    private int                   serverport;
    private HttpServletRequest    request;
    private LinkedList            perflog          = null;
    private LinkedList            perfstack        = null;
    private long                  starttime        = 0;

    
    public void initPerfLog() {
        perflog   = new LinkedList();
        perfstack = new LinkedList();
        starttime = System.currentTimeMillis();
    }

    public void startLogEntry() {
        if (perflog != null) {
            long now   = System.currentTimeMillis();
            int  index = perflog.size(); // this is the index the new entry will get
            
            PerfEvent evn = new PerfEvent(now, ">>>", PerfEvent.START);
            perflog.add(evn);
            perfstack.add(evn); // this will be decreased by stop events
        }
    }

    public void endLogEntry(String info, long delay) {
        if (perflog != null) {
            long       now   = System.currentTimeMillis();
            PerfEvent start = (PerfEvent) perfstack.removeLast(); // the matching StartEvent
            if (start != null) {
                long stime = start.getTime();
                if (now - stime >= delay) {
                    String    msg = "<<<" + " " + info + " [" + (now - stime) + "ms]";
                    PerfEvent evn = new PerfEvent(now, msg, PerfEvent.END);
                    perflog.add(evn);
                } else {
                    perflog.remove(start);
                }
            } else {
                CAT.error("got end log event without a start log on the perfstack");
            }
        }
    }

    public synchronized void printLog() {
        if (session != null && perflog != null) {
            int depth  = 0;
            for (int i = 0; i < perflog.size(); i++) {
                StringBuffer indent = new StringBuffer("");
                PerfEvent    entry  = (PerfEvent) perflog.get(i);
                if (entry.getType() == PerfEvent.START) {
                    for (int j = 0; j < depth; j++) {
                        indent.append(INDENT);
                    }
                    depth++;
                } else {
                    depth--;
                    for (int j = 0; j < depth; j++) {
                        indent.append(INDENT);
                    }
                }
                PFXPERF.debug(session.getId() + "|" + FORMAT.format(entry.getTime() - starttime) + "|" +
                              indent.toString() + entry.getMessage());
            }
            PFXPERF.debug("--------------------------------------");
        }
    }

    private class PerfEvent {
        static final int START = 1;
        static final int END   = -1;
        long             time;
        int              type;
        String           msg;
        
        public PerfEvent (long time, String msg, int type) {
            this.time = time;
            this.msg  = msg;
            this.type = type;
        }

        public int getType() { return type; }
        public long getTime() { return time; }
        public String getMessage() { return msg; }
    }
    
    //~ Constructors ...............................................................................

    /**
     * Constructor for creating a PfixServletRequest
     * @param req the orginal servlet request 
     * @param properties 
     * @param cUtil
     */
    public PfixServletRequest(HttpServletRequest req, Properties properties) {
        getRequestParams(req, properties);
        servername  = req.getServerName();
        querystring = req.getQueryString();
        scheme      = req.getScheme();
        uri         = req.getRequestURI();
        serverport  = req.getServerPort();
        request     = req;
        session     = req.getSession(false);
    }

    //~ Methods ....................................................................................

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
        return ! exceptions.isEmpty();
    }

    /** 
     * Retrieve all happened exceptions
     * @return a list containing all exceptions
     */
    public List getAllExceptions() {
        return exceptions;
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
    public String getRequestURI(HttpServletResponse res) {
        return SessionHelper.encodeURI(request,res);
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

    // ---------------------------------- //

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

    /**
     * Retrieve all names of all request params
     * @return an array containing all names for all request params.
     */
    public String[] getRequestParamNames() {
        return (String[]) parameters.keySet().toArray(new String[]{});
    }

    // ------------------------------- //
    private void getRequestParams(HttpServletRequest req, Properties properties) {
        String  type     = req.getContentType();
        HashSet allnames = new HashSet();
        if (type != null && type.toLowerCase().startsWith(MultipartHandler.MULTI_FORM_DATA)) {
            allnames.addAll(handleMulti(req, properties));
        }
        for (Enumeration enum = req.getParameterNames(); enum.hasMoreElements();) {
            String key = (String) enum.nextElement();
            allnames.add(key);
            String[] data = req.getParameterValues(key);
            CAT.debug("* [NORMAL] Found parameters for key '" + key + "' count=" + data.length);
            RequestParam[] multiparams = (RequestParam[]) parameters.get(key);
            RequestParam[] params;
            if (multiparams == null) {
                params = new RequestParam[data.length];
            } else {
                params = new RequestParam[data.length + multiparams.length];
            }
            for (int i = 0; i < data.length; i++) {
                RequestParam param = new SimpleRequestParam(data[i]);
                CAT.debug("          " + i + ") Type: NORMAL Value: " + param.getValue());
                params[i] = param;
            }
            if (multiparams != null) {
                CAT.debug("          **** MULTI is not null...");
                for (int i = data.length; i < (data.length + multiparams.length); i++) {
                    PartData pdat = (PartData) multiparams[i - data.length];
                    CAT.debug("          " + i + ") Type: " + pdat.getType() + " Value: "
                              + pdat.getValue());
                    params[i] = multiparams[i - data.length];
                }
            }
            parameters.put(key, params);
        }
        HashSet processed=new HashSet();
        for (Iterator i = allnames.iterator(); i.hasNext();) {
            String paramname = (String) i.next();
            //avoid duplicate key/value pairs for input buttons with image type by
            //excluding the according duplicate parameter names from embedded data check
            if(paramname.endsWith(":.x")||paramname.endsWith(":.y")) {
                paramname=paramname.substring(0,paramname.length()-2);  
            }
            if(!processed.contains(paramname)) {
                checkParameterNameForEmbeddedData(paramname);
                processed.add(paramname);
            }
        }
    }

    private HashSet handleMulti(HttpServletRequest req, Properties properties) {
        String  tmpdir = properties.getProperty(PROP_TMPDIR);
        HashSet names = new HashSet();
        if (tmpdir == null || tmpdir.equals("")) {
            tmpdir = DEF_TMPDIR;
        }
        String maxsize = properties.getProperty(PROP_MAXPARTSIZE);
        if (maxsize == null || maxsize.equals("")) {
            maxsize = DEF_MAXPARTSIZE;
        }
        MultipartHandler multi = new MultipartHandler(req, tmpdir);
        multi.setMaxPartSize((new Long(maxsize)).longValue());
        try {
            multi.parseRequest();
            exceptions.addAll(multi.getExceptionList());
        } catch (Exception e) {
            exceptions.add(e);
        }
        for (Enumeration enum = multi.getParameterNames(); enum.hasMoreElements();) {
            String key = (String) enum.nextElement();
            names.add(key);
            List       values = multi.getAllParameter(key);
            PartData[] data = (PartData[]) values.toArray(new PartData[]{});
            CAT.debug("* [MULTI] Found parameters for key '" + key + "' count=" + data.length);
            for (int i = 0; i < data.length; i++) {
                PartData tmp = data[i];
                CAT.debug("          " + i + ") Type: " + tmp.getType() + " Value: "
                          + tmp.getValue());
            }
            parameters.put(key, data);
        }
        return names;
    }

    private void checkParameterNameForEmbeddedData(String name) {
        HashMap embpar = new HashMap();
        int     index = 0;
        while (name.indexOf(DATA_PREFIX, index) >= 0) {
            int keystart = name.indexOf(DATA_PREFIX, index) + DATA_PREFIX.length();
            int keyend = name.indexOf(":", keystart);
            if (keyend < 0) {
                CAT.warn("    * [EMB/" + name
                         + "] No trailing ':' was found after the key. Ignoring key.");
                break;
            }
            String key = name.substring(keystart, keyend);
            CAT.debug("    * [EMB/" + name + "]  >> Key is " + key);
            int valuestart = keyend + 1;
            int valueend = name.indexOf(":", valuestart);
            if (valueend < 0) {
                CAT.warn("    * [EMB/" + name
                         + "] No trailing ':' was found after the value. Ignoring key/value pair.");
                break;
            }
            String value = name.substring(valuestart, valueend);
            CAT.debug("    * [EMB/" + name + "]  >> Value is " + value);
            ArrayList list = (ArrayList) embpar.get(key);
            if (list == null) {
                list = new ArrayList();
                embpar.put(key, list);
            }
            list.add(value);
            index = valueend;
        }
        for (Iterator i = embpar.keySet().iterator(); i.hasNext();) {
            String    pkey  = (String) i.next();
            ArrayList pvals = (ArrayList) embpar.get(pkey);
            if (pvals != null) {
                RequestParam[] oldvals = (RequestParam[]) parameters.get(pkey);
                RequestParam[] newvals;
                if (oldvals == null) {
                    newvals = new RequestParam[pvals.size()];
                } else {
                    newvals = new RequestParam[pvals.size() + oldvals.length];
                }
                for (int j = 0; j < pvals.size(); j++) {
                    newvals[j] = new SimpleRequestParam((String) pvals.get(j));
                    newvals[j].setSynthetic(true);
                }
                if (oldvals != null) {
                    CAT.debug("          **** [EMB] already having parameter data for key '" + pkey
                              + "'...");
                    for (int k = pvals.size(); k < (pvals.size() + oldvals.length); k++) {
                        newvals[k] = oldvals[k - pvals.size()];
                    }
                }
                parameters.put(pkey, newvals);
            }
        }
    }
} // PfixServletRequest
