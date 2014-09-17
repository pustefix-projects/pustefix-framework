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

import javax.xml.transform.TransformerException;

import org.pustefixframework.xslt.XMLSourceLocator;
import org.pustefixframework.xslt.XSLSourceLocator;

import com.icl.saxon.om.NodeInfo;

/**
 * ErrorListener adding XML source location from TraceListener
 * to the TransformerException's SourceLocator
 *
 */
public class Saxon1ErrorListener extends ErrorListenerBase {

    private Saxon1LocationTraceListener traceListener;
    
    public Saxon1ErrorListener(Saxon1LocationTraceListener traceListener) {
        this.traceListener = traceListener;
    }

    public void fatalError(TransformerException e) throws TransformerException {
        if(traceListener != null) {
            NodeInfo nodeInfo = traceListener.getContextNodeInfo();
            nodeInfo = traceListener.getCurrentNodeInfo();
            if(nodeInfo != null && nodeInfo.getSystemId() != null && !nodeInfo.getSystemId().isEmpty()) {
                XMLSourceLocator xmlLocator = new XMLSourceLocator(nodeInfo.getSystemId(), nodeInfo.getLineNumber());
                XSLSourceLocator xslLocator = new XSLSourceLocator(e.getLocator(), xmlLocator);
                e.setLocator(xslLocator);
            }
        }
        super.fatalError(e);
    }
    
}
