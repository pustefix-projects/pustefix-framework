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

package de.schlund.pfixxml.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.util.xsltimpl.XPathSaxon1;
import de.schlund.pfixxml.util.xsltimpl.XPathSaxon2;
import de.schlund.pfixxml.util.xsltimpl.XmlSaxon1;
import de.schlund.pfixxml.util.xsltimpl.XmlSaxon2;
import de.schlund.pfixxml.util.xsltimpl.XsltSaxon1;
import de.schlund.pfixxml.util.xsltimpl.XsltSaxon2;

/**
 * @author mleidig@schlund.de
 */
public class XsltProvider {
    
    static Logger LOG=Logger.getLogger(XsltProvider.class);

    static Map<XsltVersion,XmlSupport> xmlSupport=new HashMap<XsltVersion,XmlSupport>();
    static Map<XsltVersion,XPathSupport> xpathSupport=new HashMap<XsltVersion,XPathSupport>();
    static Map<XsltVersion,XsltSupport> xsltSupport=new HashMap<XsltVersion,XsltSupport>();
    
    static XsltVersion preferredXsltVersion=XsltVersion.XSLT1;
    
    static {
        try {
            xmlSupport.put(XsltVersion.XSLT1,new XmlSaxon1());
        } catch(Exception x) {
            LOG.warn("Can't initialize XmlSupport: "+XmlSaxon1.class.getName(),x);
        }
        try {
            xpathSupport.put(XsltVersion.XSLT1,new XPathSaxon1());
        } catch(Exception x) {
            LOG.warn("Can't initialize XPathSupport: "+XPathSaxon1.class.getName(),x);
        }
        try {
            xsltSupport.put(XsltVersion.XSLT1,new XsltSaxon1());
        } catch(Exception x) {
            LOG.warn("Can't initialize XsltSupport: "+XsltSaxon1.class.getName(),x);
        }
        try {
            xmlSupport.put(XsltVersion.XSLT2,new XmlSaxon2());
        } catch(Exception x) {
            LOG.warn("Can't initialize XmlSupport: "+XmlSaxon2.class.getName(),x);
        }
        try {
            xpathSupport.put(XsltVersion.XSLT2,new XPathSaxon2());
        } catch(Exception x) {
            LOG.warn("Can't initialize XPathSupport: "+XPathSaxon2.class.getName(),x);
        }
        try {
            xsltSupport.put(XsltVersion.XSLT2,new XsltSaxon2());
        } catch(Exception x) {
            LOG.warn("Can't initialize XsltSupport: "+XsltSaxon2.class.getName(),x);
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
