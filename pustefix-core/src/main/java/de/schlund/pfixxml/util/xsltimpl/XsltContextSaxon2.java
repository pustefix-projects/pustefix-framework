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

package de.schlund.pfixxml.util.xsltimpl;

import javax.xml.transform.URIResolver;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * @author mleidig@schlund.de
 */
public class XsltContextSaxon2 implements XsltContext {

    XPathContext context;
    
    public XsltContextSaxon2(XPathContext context) {
        this.context=context;
    }
    
    public String getSystemId() {
        NodeInfo info=(NodeInfo)context.getContextItem();
        return info.getSystemId();
    }
    
    public String getStylesheetSystemId() {
        return null;
    }
    
    public String getDocumentElementName() {
        NodeInfo info=(NodeInfo)context.getContextItem();
        return info.getDocumentRoot().getRoot().getLocalPart();
    }
    
    public XsltVersion getXsltVersion() {
        return XsltVersion.XSLT2;
    }
    
    public URIResolver getURIResolver() {
        return context.getController().getURIResolver();
    }
    
}
