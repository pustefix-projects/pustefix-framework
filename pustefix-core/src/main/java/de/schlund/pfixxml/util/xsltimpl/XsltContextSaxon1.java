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

import org.w3c.dom.Document;

import com.icl.saxon.Context;
import com.icl.saxon.expr.StringValue;
import com.icl.saxon.om.NodeInfo;

import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * @author mleidig@schlund.de
 */
public class XsltContextSaxon1 implements XsltContext {

    Context context;
    
    public XsltContextSaxon1(Context context) {
        this.context=context;
    }
    
    public String getSystemId() {
        NodeInfo info=context.getContextNodeInfo();
        return info.getSystemId();
    }
    
    public String getStylesheetSystemId() {
        StringValue value = ((StringValue)context.getController().getParameter("__stylesheet"));
        if(value != null) {
            return value.asString();
        }
        return null;
    }
    
    public String getDocumentElementName() {
        NodeInfo info=context.getContextNodeInfo();
        return ((Document)info.getDocumentRoot()).getDocumentElement().getNodeName();
    }
    
    public XsltVersion getXsltVersion() {
        return XsltVersion.XSLT1;
    }
    
    public URIResolver getURIResolver() {
        return context.getController().getURIResolver();
    }
        
}
