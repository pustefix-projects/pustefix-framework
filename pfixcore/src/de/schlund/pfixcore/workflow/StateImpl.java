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

package de.schlund.pfixcore.workflow;

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.*;

/**
 * @author jtl
 *
 *
 */

public abstract class StateImpl implements State {
    protected Category          CAT           = Category.getInstance(this.getClass().getName());
    public  static final String PROP_INSERTCR = "insertcr";
    private static final String MIMETYPE      = "mimetype";
    private static final String HEADER        = "responseheader";
    private static final String def_mime      = "text/html";

    public final boolean isDirectTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(SENDDATA);
        return (!context.flowIsRunning() && (context.jumpToPageIsRunning() || !requestParamSaysTrue(sdreq)));
    }
    
    public final boolean isSubmitTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(SENDDATA);
        return (isSubmitTriggerAny(context, sdreq));
    }
    
    public final boolean isSubmitAuthTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(SENDAUTHDATA);
        return (isSubmitTriggerAny(context, sdreq));
    }

    // private
    protected static boolean requestParamSaysTrue(RequestParam sdreq) {
        if (sdreq != null && sdreq.getValue() != null) {
            String sd = sdreq.getValue();
            return (sd.equals("true") || sd.equals("1") || sd.equals("yes"));
        }
        return false;
    }
    
    
    private boolean isSubmitTriggerAny(Context context, RequestParam sdreq) {
        return (!context.flowIsRunning() && !context.finalPageIsRunning() &&
                !context.jumpToPageIsRunning() && requestParamSaysTrue(sdreq));
    }

    // You may want to overwrite this 
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }

    // You may want to overwrite this 
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }
    
    // You need to implement the state logic in this method.
    public abstract ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception;


    // Helper methods:
    
    protected ResultDocument createDefaultResultDocument(Context context) throws Exception {
        ResultDocument  resdoc = new ResultDocument();
        renderContextResources(context, resdoc);
        addResponseHeadersAndType(context, resdoc);
        return resdoc;
    }
    
    protected void renderContextResources(Context context, ResultDocument resdoc) throws Exception {
        Properties props  = context.getPropertiesForCurrentPageRequest();
        if (props != null) {
            ContextResourceManager crm = context.getContextResourceManager();
            HashMap                crs = PropertiesUtils.selectProperties(props, PROP_INSERTCR);
            if (crs != null) {
                for (Iterator i = crs.keySet().iterator(); i.hasNext();) {
                    String nodename  = (String) i.next();
                    String classname = (String) crs.get(nodename);
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("*** Auto appending status for " + classname + " at node " + nodename);
                    }
                    ContextResource cr = crm.getResource(classname);
                    if (cr == null) {
                        throw new XMLException("ContextResource not found: " + classname);
                    }
                    context.startLogEntry();
                    cr.insertStatus(resdoc, resdoc.createNode(nodename));
                    PerfEventType et = PerfEventType.CONTEXTRESOURCE_INSERTSTATUS;
                    et.setClass(classname);
                    context.endLogEntry(et);
                }
            }
        }
    }

    protected void addResponseHeadersAndType(Context context, ResultDocument resdoc) {
        Properties      props  = context.getPropertiesForCurrentPageRequest();
        String          mime   = props.getProperty(MIMETYPE);
        SPDocument      doc    = resdoc.getSPDocument();
        if (mime != null) {
            doc.setResponseContentType(mime);
        } else {
            doc.setResponseContentType(def_mime);
        }
        HashMap headers = PropertiesUtils.selectProperties(props, HEADER);
        if (headers != null && !headers.isEmpty()) {
            for (Iterator iter = headers.keySet().iterator(); iter.hasNext(); ) {
                String key = (String) iter.next();
                String val = (String) headers.get(key);
                CAT.debug("* Adding response header: " + key + " => " + val);
                doc.addResponseHeader(key, val);
            }
        }
    }
}
