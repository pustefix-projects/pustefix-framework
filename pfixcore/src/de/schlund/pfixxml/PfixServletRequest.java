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
import de.schlund.pfixxml.serverutil.ContainerUtil;
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
 *
 *
 */

public class PfixServletRequest {
    private static final String DATA_PREFIX      = "__DATA:";
    public  static final String PROP_TMPDIR      = "pfixservletrequest.tmpdir";
    public  static final String PROP_MAXPARTSIZE = "pfixservletrequest.maxpartsize";
    public  static       String DEF_TMPDIR       = "/tmp";
    public  static       String DEF_MAXPARTSIZE  = "" + (10 * 1024 * 1024); // 10 MB
    private ArrayList          exceptions = new ArrayList();
    private HashMap            parameters = new HashMap();
    private Category           CAT        = Category.getInstance(this.getClass());
    private String             servername;
    private String             querystring;
    private String             scheme;
    private String             uri;
    private HttpSession        session = null; 
    private int                serverport;
    private HttpServletRequest request;
    private ContainerUtil      conUtil;
    
    public PfixServletRequest(HttpServletRequest req, Properties properties, ContainerUtil cUtil) {
        getRequestParams(req, properties);
        
        servername  = req.getServerName();
        querystring = req.getQueryString();
        scheme      = req.getScheme();
        uri         = req.getRequestURI();
        serverport  = req.getServerPort();
        
        request     = req;
        session     = req.getSession(false);
        
        conUtil     = cUtil;
    }
    
    public String getOriginalServerName() {
        return servername;
    }
    
    public String getOriginalQueryString() {
        return querystring;
    }
    
    public String getOriginalScheme() {
        return scheme;
    }
    
    public String getOriginalRequestURI() {
        return uri;
    }

    public int getOriginalServerPort() {
        return serverport;
    }

    public void updateRequest(HttpServletRequest req) {
        this.request = req;
        this.session = req.getSession(false);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public boolean errorHappened() {
        return !exceptions.isEmpty();
    }
    
    public List getAllExceptions() {
        return exceptions;
    }


    // All these methods work on the currently set request, which may
    // be the original request used when the instance was created, or
    // any other request that has been set via updateRequest().
    // Most are just called as the corresponding methods in HttpServletRequest.
    
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    public String getPathInfo() {
        return request.getPathInfo();
    }

    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    public String getRequestURI(HttpServletResponse res) {
        if (conUtil != null) {
            return conUtil.encodeURI(request, res);
        } else {
            return request.getRequestURI();
        }
    }

    public String getContextPath() {
        return conUtil.getContextPath(request);
    }
    
    public String getServletPath() {
        return request.getServletPath();
    }
    
    public HttpSession getSession(boolean create) {
        if (!create) {
            return session;
        } else {
            return request.getSession(true);
        }
    }
    
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    public String getScheme() {
        return request.getScheme();
    }

    public int getServerPort() {
        return request.getServerPort();
    }

    public String getServerName() {
        return request.getServerName();
    }
    
    // ---------------------------------- //
    
    public RequestParam getRequestParam(String name) {
        RequestParam[] params = (RequestParam[]) parameters.get(name);
        if (params != null && params.length > 0) {
            return params[0];
        } else {
            return null;
        }
    }

    public RequestParam[] getAllRequestParams(String name) {
        RequestParam[] params = (RequestParam[]) parameters.get(name);
        if (params != null) {
            return params;
        } else {
            return null;
        }
    }

    public String[] getRequestParamNames() {
        return (String[]) parameters.keySet().toArray(new String[] {});
    }

    // ------------------------------- //
    
    private void getRequestParams(HttpServletRequest req, Properties properties) {
        String  type     = req.getContentType();
        HashSet allnames = new HashSet();
        if (type != null && type.toLowerCase().startsWith(MultipartHandler.MULTI_FORM_DATA)) {
            allnames.addAll(handleMulti(req, properties));
        }

        for (Enumeration enum = req.getParameterNames(); enum.hasMoreElements(); ) {
            String   key  = (String) enum.nextElement();
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
                CAT.debug("          **** MULTI is not null..." );
                for (int i = data.length; i < (data.length + multiparams.length); i++) {
                    PartData pdat = (PartData) multiparams[i - data.length];
                    CAT.debug("          " + i + ") Type: " + pdat.getType() + " Value: " + pdat.getValue());
                    params[i] = multiparams[i - data.length];
                }
            }
            parameters.put(key, params);
        }

        for (Iterator i = allnames.iterator(); i.hasNext();) {
            String paramname = (String) i.next();
            checkParameterNameForEmbeddedData(paramname);
        }
        
    }

    private HashSet handleMulti(HttpServletRequest req, Properties properties) {
        String  tmpdir = properties.getProperty(PROP_TMPDIR);
        HashSet names  = new HashSet();
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
        
        for (Enumeration enum = multi.getParameterNames(); enum.hasMoreElements(); ) {
            String     key    = (String) enum.nextElement();
            names.add(key);
            List       values = multi.getAllParameter(key);
            PartData[] data   = (PartData[])values.toArray(new PartData[] {});
            CAT.debug("* [MULTI] Found parameters for key '" + key + "' count=" + data.length);
            for (int i = 0; i < data.length; i++) {
                PartData tmp = data[i];
                CAT.debug("          " + i + ") Type: " + tmp.getType() + " Value: " + tmp.getValue());
            }
            parameters.put(key, data);
        }
        return names;
    }


    private void checkParameterNameForEmbeddedData(String name) {
        HashMap embpar = new HashMap();
        int     index  = 0;
        while (name.indexOf(DATA_PREFIX, index) >= 0) {
            int keystart = name.indexOf(DATA_PREFIX, index) + DATA_PREFIX.length();
            int keyend   = name.indexOf(":", keystart);
            if (keyend < 0) {
                CAT.warn("    * [EMB/" + name + "] No trailing ':' was found after the key. Ignoring key.");
                break;
            }
            String key = name.substring(keystart, keyend);
            CAT.debug("    * [EMB/" + name + "]  >> Key is " + key);

            int valuestart = keyend +1 ;
            int valueend   = name.indexOf(":", valuestart);
            if (valueend < 0) {
                CAT.warn("    * [EMB/" + name + "] No trailing ':' was found after the value. Ignoring key/value pair.");
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
                }
                if (oldvals != null) {
                    CAT.debug("          **** [EMB] already having parameter data for key '" + pkey + "'..." );
                    for (int k = pvals.size(); k < (pvals.size() + oldvals.length); k++) {
                        newvals[k] = oldvals[k - pvals.size()];
                    }
                }
                parameters.put(pkey, newvals);
            }
        }
        
    }

    
    
}// PfixServletRequest
