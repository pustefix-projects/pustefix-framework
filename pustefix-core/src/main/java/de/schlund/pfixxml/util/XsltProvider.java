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

package de.schlund.pfixxml.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author mleidig@schlund.de
 */
public class XsltProvider {
    
    final static Logger LOG=Logger.getLogger(XsltProvider.class);

    final static String DETECT_SAXON1="com.icl.saxon.TransformerFactoryImpl";
    final static String DETECT_SAXON2="net.sf.saxon.TransformerFactoryImpl";
    
    final static String XPATH_SAXON1="de.schlund.pfixxml.util.xsltimpl.XPathSaxon1";
    final static String XPATH_SAXON2="de.schlund.pfixxml.util.xsltimpl.XPathSaxon2";
    final static String XML_SAXON1="de.schlund.pfixxml.util.xsltimpl.XmlSaxon1";
    final static String XML_SAXON2="de.schlund.pfixxml.util.xsltimpl.XmlSaxon2";
    final static String XSLT_SAXON1="de.schlund.pfixxml.util.xsltimpl.XsltSaxon1";
    final static String XSLT_SAXON2="de.schlund.pfixxml.util.xsltimpl.XsltSaxon2";
    
    static Map<XsltVersion,XmlSupport> xmlSupport=new HashMap<XsltVersion,XmlSupport>();
    static Map<XsltVersion,XPathSupport> xpathSupport=new HashMap<XsltVersion,XPathSupport>();
    static Map<XsltVersion,XsltSupport> xsltSupport=new HashMap<XsltVersion,XsltSupport>();
    
    static XsltVersion preferredXsltVersion=XsltVersion.XSLT1;
    
    static {
        boolean saxon1Available=false;
        try {
            Class.forName(DETECT_SAXON1);
            saxon1Available=true;    
        } catch(Exception x) {
            LOG.warn("No Saxon XSLT1 implementation found!");
        }
        boolean saxon2Available=false;
        try {
            Class.forName(DETECT_SAXON2);
            saxon2Available=true;    
        } catch(Exception x) {
            LOG.warn("No Saxon XSLT2 implementation found!");
        }
        if(saxon1Available) {
            try {
                Class<? extends XmlSupport> clazz=Class.forName(XML_SAXON1).asSubclass(XmlSupport.class);
                xmlSupport.put(XsltVersion.XSLT1,clazz.newInstance());
            } catch(Exception x) {
                LOG.error("Can't initialize XmlSupport: "+XML_SAXON1,x);
            }
            try {
                Class<? extends XPathSupport> clazz=Class.forName(XPATH_SAXON1).asSubclass(XPathSupport.class);
                xpathSupport.put(XsltVersion.XSLT1,clazz.newInstance());
            } catch(Exception x) {
                LOG.error("Can't initialize XPathSupport: "+XPATH_SAXON1,x);
            }
            try {
                Class<? extends XsltSupport> clazz=Class.forName(XSLT_SAXON1).asSubclass(XsltSupport.class);
                xsltSupport.put(XsltVersion.XSLT1,clazz.newInstance());
            } catch(Exception x) {
                LOG.error("Can't initialize XsltSupport: "+XSLT_SAXON1,x);
            }
        }
        if(saxon2Available) {
            try {
                Class<? extends XmlSupport> clazz=Class.forName(XML_SAXON2).asSubclass(XmlSupport.class);
                xmlSupport.put(XsltVersion.XSLT2,clazz.newInstance());
            } catch(Exception x) {
                LOG.error("Can't initialize XmlSupport: "+XML_SAXON2,x);
            }
            try {
                Class<? extends XPathSupport> clazz=Class.forName(XPATH_SAXON2).asSubclass(XPathSupport.class);
                xpathSupport.put(XsltVersion.XSLT2,clazz.newInstance());
            } catch(Exception x) {
                LOG.error("Can't initialize XPathSupport: "+XPATH_SAXON2,x);
            }
            try {
                Class<? extends XsltSupport> clazz=Class.forName(XSLT_SAXON2).asSubclass(XsltSupport.class);
                xsltSupport.put(XsltVersion.XSLT2,clazz.newInstance());
            } catch(Exception x) {
                LOG.error("Can't initialize XsltSupport: "+XSLT_SAXON2,x);
            }
        }
    }
    
    public static Map<XsltVersion,XmlSupport> getXmlSupport() {
        return xmlSupport;
    }
    
    public static XmlSupport getXmlSupport(XsltVersion version) {
        return xmlSupport.get(version);
    }
    
    public static Map<XsltVersion,XPathSupport> getXpathSupport() {
        return xpathSupport;
    }
    
    public static XPathSupport getXPathSupport(XsltVersion version) {
        return xpathSupport.get(version);
    }
    
    public static Map<XsltVersion,XsltSupport> getXsltSupport() {
        return xsltSupport;
    }
    
    public static XsltSupport getXsltSupport(XsltVersion version) {
        return xsltSupport.get(version);
    }
    
    public static XsltVersion getPreferredXsltVersion() {
        if(getXmlSupport().containsKey(XsltVersion.XSLT1)) return XsltVersion.XSLT1;
        return XsltVersion.XSLT2;
    }
    
}
