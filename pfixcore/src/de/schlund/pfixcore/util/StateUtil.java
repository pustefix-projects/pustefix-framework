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

package de.schlund.pfixcore.util;

import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;


/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class StateUtil {
  
    private static Logger LOG = Logger.getLogger(StateImpl.class);
    
    private static final String MIMETYPE      = "mimetype";
    private static final String HEADER        = "responseheader";
    private static final String def_mime      = "text/html";

    /**
     * 
     */
    public static ResultDocument createDefaultResultDocument(Context context) throws Exception {
        ResultDocument  resdoc = new ResultDocument();
        renderContextResources(context, resdoc);
        addResponseHeadersAndType(context, resdoc);
        return resdoc;
    }
    
  
    /**
     * 
     */
    public static void renderContextResources(Context context, ResultDocument resdoc) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        Map<String, Class> crs = context.getConfigForCurrentPageRequest().getContextResources();
        
        for (Iterator<String> i = crs.keySet().iterator(); i.hasNext();) {
            String nodename = i.next();
            String classname = crs.get(nodename).getName();
            if (LOG.isDebugEnabled()) {
                LOG.debug("*** Auto appending status for " + classname + " at node " + nodename);
            }
            ContextResource cr = (ContextResource) crm.getResource(classname);
            if (cr == null) {
                throw new XMLException("ContextResource not found: " + classname);
            }
           
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXTRESOURCE_INSERTSTATUS, classname);
            pe.start();
            cr.insertStatus(resdoc, resdoc.createNode(nodename));
            pe.save();
        }
    }

    
    /**
     * 
     */
    public static void addResponseHeadersAndType(Context context, ResultDocument resdoc) {
        Properties props = context.getPropertiesForCurrentPageRequest();
        Properties contextprops = context.getProperties();
        
        String mime = props.getProperty(MIMETYPE);
        SPDocument doc = resdoc.getSPDocument();

        if (mime != null) {
            doc.setResponseContentType(mime);
        } else {
            doc.setResponseContentType(def_mime);
        }

        // Set global headers first
        HashMap headers = PropertiesUtils.selectProperties(contextprops, HEADER);
        if (headers != null && !headers.isEmpty()) {
            for (Iterator iter = headers.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String val = (String) headers.get(key);
                LOG.debug("* Adding response header: " + key + " => " + val);
                doc.addResponseHeader(key, val);
            }
        }

        // then set page specific headers
        headers = PropertiesUtils.selectProperties(props, HEADER);
        if (headers != null && !headers.isEmpty()) {
            for (Iterator iter = headers.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String val = (String) headers.get(key);
                LOG.debug("* Adding response header: " + key + " => " + val);
                doc.addResponseHeader(key, val);
            }
        }
    }

    
    /**
     * 
     */
    public static boolean isDirectTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(State.SENDDATA);
        return (!context.flowIsRunning() && (context.jumpToPageIsRunning() || sdreq == null || !sdreq.isTrue()));
    }
    
    
    /**
     * 
     */
    public static boolean isSubmitTrigger(Context context, PfixServletRequest preq) {
        return isSubmitTriggerHelper(context, preq.getRequestParam(State.SENDDATA));
    }
    
    
    /**
     * 
     */
    public static boolean isSubmitAuthTrigger(Context context, PfixServletRequest preq) {
        return isSubmitTriggerHelper(context, preq.getRequestParam(State.SENDAUTHDATA));
    }
    
    
    // ============ private Helper methods ============
    
    /**
     * 
     */
    private static boolean isSubmitTriggerHelper(Context context, RequestParam sdreq) {
        return (!context.flowIsRunning() && !context.finalPageIsRunning() &&
                !context.jumpToPageIsRunning() && sdreq != null && sdreq.isTrue());
    }  
}
