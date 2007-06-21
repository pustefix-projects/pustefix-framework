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

import de.schlund.pfixxml.util.Xml;
import java.util.*;

import javax.servlet.http.*;

import org.w3c.dom.*;


/**
 *
 *
 */
public class SPDocument {

    //~ Instance/static variables ..................................................................

    private Document   document;
    private Properties properties;
    private boolean    updateable  = true;
    private boolean    nostore     = false;
    private String     pagename    = null;
    private Variant    variant     = null;
    private String     xslkey      = null;
    private long       timestamp   = System.currentTimeMillis();
    private int        error       = 0;
    private String     errortext   = null;
    private String     contenttype = null;
    private HashMap    header      = new HashMap();
    private ArrayList  cookies     = new ArrayList();
    private String     sslRedirectURL = null;
    private boolean trailLogged;

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

    public void setNostore(boolean nostore) {
        this.nostore = nostore;
    }

    public boolean getNostore() {
        return nostore;
    }
    
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
        if (sslRedirectURL == null) {
            return error;
        } else {
            return HttpServletResponse.SC_MOVED_TEMPORARILY;
        }
    }

    public void addResponseHeader(String key, String val) {
        header.put(key, val);
    }

    public HashMap getResponseHeader() {
        if (sslRedirectURL == null) {
            return header;
        } else {
            HashMap newheader = new HashMap();
            newheader.put("Location", sslRedirectURL);
            return newheader;
        }
    }

    public void storeFrameAnchors(Map anchors) {
        if (document == null) {
            throw new RuntimeException("*** Can't store anchors into a null Document ***");
        }
        Element root = document.getDocumentElement();
        for (Iterator i = anchors.keySet().iterator(); i.hasNext();) {
            String  frame  = (String) i.next();
            String  anchor = (String) anchors.get(frame);
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

    public ArrayList getCookies() {
        return cookies;
    }

    public String getXSLKey() {
        return xslkey;
    }
    
    public Document getDocument() {
        return document;
    }

    public Properties getProperties() {
        return properties;
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

    public void setProperties(Properties newProperties) {
        properties = newProperties;
    }

    public void setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.setProperty(key, value);
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
    public void setSSLRedirect(String redirectURL) {
        this.sslRedirectURL = redirectURL;
    }
    
    /**
     * Resets the redirect URL set via {@link #setSSLRedirect(String)}.
     * Should be called after serving the document the first time
     * (which effectively means after doing the redirect).
     */
    public void resetSSLRedirectURL() {
        this.sslRedirectURL = null;
    }

    public boolean getTrailLogged() {
        return trailLogged;
    }
    
    public void setTrailLogged() {
        trailLogged=true;
    }
    
    /**
     * Describe <code>toString</code> method here.
     *
     * @return a <code>String</code> value
     */
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
