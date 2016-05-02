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

import com.icl.saxon.Context;
import com.icl.saxon.NodeHandler;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.trace.TraceListener;

/**
 * TraceListener storing current/context node of XSL transformation
 * to make it available for error reporting.
 */
public class Saxon1LocationTraceListener implements TraceListener {

    private NodeInfo contextNodeInfo;
    private NodeInfo currentNodeInfo;
    
    public NodeInfo getContextNodeInfo() {
        return contextNodeInfo;
    }
    
    public NodeInfo getCurrentNodeInfo() {
        return currentNodeInfo;
    }
    
    @Override
    public void enter(NodeInfo node, Context context) {}
    
    @Override
    public void leave(NodeInfo node, Context context) {}
    
    @Override
    public void enterSource(NodeHandler handler, Context context) {
        contextNodeInfo = context.getContextNodeInfo();
        currentNodeInfo= context.getCurrentNodeInfo();
    }
    
    @Override
    public void leaveSource(NodeHandler handler, Context context) {
        contextNodeInfo = null;
        currentNodeInfo = null;
    }
    
    @Override
    public void toplevel(NodeInfo node) {}
    
    @Override
    public void open() {}
    
    @Override
    public void close() {}
    
}
