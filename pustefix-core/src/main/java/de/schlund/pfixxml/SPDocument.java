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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixxml.util.Xml;


/**
 *
 *
 */
public class SPDocument {

    //~ Instance/static variables ..................................................................

    private Document  document;
    private HashMap<String, Object> propertiesmap;
    private boolean   updateable     = true;
    // private boolean   nostore        = false;
    private String    pagename       = null;
    private Variant   variant        = null;
    private Tenant tenant;
    private String pageAlternativeKey;
    private String language;
    private String    xslkey         = null;
    private long      timestamp      = System.currentTimeMillis();
    private int       error          = 0;
    private String    errortext      = null;
    private String    contenttype    = null;
    private HashMap<String, String> header  = new HashMap<String, String>();
    private ArrayList<Cookie> cookies = new ArrayList<Cookie>();
    private String redirectURL = null;
    private boolean permanentRedirect = false;
    private boolean trailLogged;
    private long creationTime;

    //~ Methods ....................................................................................

    // Pagename is the preferred way to specify the target
    public void setPagename(String pagename) {
        this.pagename = pagename;
    }

    public String getPagename() {
        return pagename;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public Variant getVariant() {
        return variant;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Tenant getTenant() {
        return tenant;
    }
    
    public void setPageAlternative(String key) {
        pageAlternativeKey = key;
    }
    
    public String getPageAlternative() {
        return pageAlternativeKey;
    }

//    public void setNostore(boolean nostore) {
//        this.nostore = nostore;
//    }
//
//    public boolean getNostore() {
//        return nostore;
//    }
    
    public void setResponseContentType(String type) {
        contenttype = type;
    }

    public String getResponseContentType() {
        return contenttype;
    }

    public void setResponseErrorText(String err) {
        errortext = err;
    }

    public String getResponseErrorText() {
        return errortext;
    }

    public void setResponseError(int err) {
        error = err;
    }

    public int getResponseError() {
        if (redirectURL == null) {
            return error;
        } else {
            return permanentRedirect?HttpServletResponse.SC_MOVED_PERMANENTLY:HttpServletResponse.SC_MOVED_TEMPORARILY;
        }
    }

    public void addResponseHeader(String key, String val) {
        header.put(key, val);
    }

    public HashMap<String, String> getResponseHeader() {
        if (redirectURL == null) {
            return header;
        } else {
            HashMap<String, String> newheader = new HashMap<String, String>();
            newheader.put("Location", redirectURL);
            return newheader;
        }
    }

    public void storeFrameAnchors(Map<String, String> anchors) {
        if (document == null) {
            throw new RuntimeException("*** Can't store anchors into a null Document ***");
        }
        Element root = document.getDocumentElement();
        for (Iterator<String> i = anchors.keySet().iterator(); i.hasNext();) {
            String  frame  = i.next();
            String  anchor = anchors.get(frame);
            Element elem   = document.createElement("frameanchor");
            elem.setAttribute("frame", frame);
            elem.setAttribute("anchor", anchor);
            root.appendChild(elem);
        }
    }

    /**
     * Returns timestamp that was created on construction
     * of the document. Is <b>not</b> guaranteed to be unique.
     *
     * @return a <code>long</code> value
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public ArrayList<Cookie> getCookies() {
        return cookies;
    }

    public String getXSLKey() {
        return xslkey;
    }
    
    public Document getDocument() {
        return document;
    }

    public HashMap<String, Object> getProperties() {
        return propertiesmap;
    }

    public boolean docIsUpdateable() {
        return updateable;
    }

    public void setDocIsUpdateable(boolean upd) {
        updateable = upd;
    }
    
    public void setDocument(Document newDocument) {
        document = newDocument;
    }

    public void setProperties(HashMap<String, Object> newPropertiesmap) {
        propertiesmap = newPropertiesmap;
    }

    public void setProperty(String key, Object value) {
        if (propertiesmap == null) {
            propertiesmap = new HashMap<String, Object>();
        }
        propertiesmap.put(key, value);
    }

    /**
     * Describe <code>setXSLKey</code> method here.
     *
     * @param xslkey a <code>String</code> value
     */
    public void setXSLKey(String xslkey) {
        this.xslkey = xslkey;
    }
    
    /**
     * Sets an URL to use for redirection.
     * This will cause the {@link #getResponseError()} and
     * {@link #getResponseHeader()} methods to return special
     * values.
     * 
     * @param redirectURL Complete URL string
     */
    public void setRedirect(String redirectURL) {
        this.redirectURL = redirectURL;
    }
    
    public void setRedirect(String redirectURL, boolean permanent) {
        this.redirectURL = redirectURL;
        this.permanentRedirect = permanent;
    }

    public boolean isRedirect() {
        return redirectURL != null;
    }
    
    /**
     * Resets the redirect URL set via {@link #setRedirect(String)}.
     * Should be called after serving the document the first time
     * (which effectively means after doing the redirect).
     */
    public void resetRedirectURL() {
        this.redirectURL = null;
    }
    
    public boolean getTrailLogged() {
        return trailLogged;
    }
    
    public void setTrailLogged() {
        trailLogged=true;
    }
    
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Describe <code>toString</code> method here.
     *
     * @return a <code>String</code> value
     */
    @Override
    public String toString() {
        Document tmp = document;
        StringBuffer sw = new StringBuffer();
        sw.append("\n");
        if (tmp == null) {
            sw.append("null\n");
        } else {
            sw.append("[class: " + tmp.getClass().getName() + "]\n");
            sw.append(Xml.serialize(tmp, true, true));
        }
        return sw.toString();
    }
}
