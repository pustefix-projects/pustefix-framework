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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import net.sf.cglib.proxy.Enhancer;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;


/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class StateUtil {
  
    private final static Logger LOG = Logger.getLogger(StateUtil.class);
    
    private static final String MIMETYPE    = "mimetype";
    private static final String HEADER      = "responseheader";
    private static final String DEFAULTMIME = "text/html";

    public static ResultDocument createDefaultResultDocument(Context context) throws Exception {
        ResultDocument  resdoc = new ResultDocument();
        renderContextResources(context, resdoc);
        addResponseHeadersAndType(context, resdoc);
        return resdoc;
    }
    
    
    @SuppressWarnings("deprecation")
    public static void renderContextResources(Context context, ResultDocument resdoc) throws Exception {
        if (context.getConfigForCurrentPageRequest() == null) {
            // This page is not defined explicitly...
            return;
        }
        ContextResourceManager crm = context.getContextResourceManager();
        Map<String, Class<?>> crs = context.getConfigForCurrentPageRequest().getContextResources();
        
        for (Iterator<String> i = crs.keySet().iterator(); i.hasNext();) {
            String nodename = i.next();
            String classname = crs.get(nodename).getName();
            if (LOG.isDebugEnabled()) {
                LOG.debug("*** Auto appending status for " + classname + " at node " + nodename);
            }
            Object cr = crm.getResource(classname);
            if (cr == null) {
                throw new XMLException("Resource not found: " + classname);
            }
            Class<?> clazz = cr.getClass();
            if(Enhancer.isEnhanced(clazz)) {
                clazz = clazz.getSuperclass();
            }
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXTRESOURCE_INSERTSTATUS, classname);
            pe.start();
            if (cr instanceof de.schlund.pfixcore.workflow.ContextResource) {
                LOG.debug("***** Resource implements ContextResource => calling insertStatus(...) of " + clazz.getName());
                ((de.schlund.pfixcore.workflow.ContextResource) cr).insertStatus(resdoc, resdoc.createNode(nodename));
            } else {
                boolean found_annotation = false;
                for (Method m : clazz.getMethods()) {
                    if (m.isAnnotationPresent(InsertStatus.class)) {
                        Class<?>[] params = m.getParameterTypes();
                        Class<?>  rettype = m.getReturnType();
                        if (params.length == 0 && rettype != null) {
                            LOG.debug("***** Found @InsertStatus for Object:" + m.getName() + "() of " + clazz.getName());
                            ResultDocument.addObject(resdoc.createNode(nodename), m.invoke(cr, new Object[] {}));
                        } else if (params.length == 1 && params[0].isAssignableFrom(Element.class)) {
                            LOG.debug("***** Found @InsertStatus for " + m.getName() + "(Element) of " + clazz.getName());
                            m.invoke(cr, resdoc.createNode(nodename));
                        } else if (params.length == 2 && params[0].isAssignableFrom(ResultDocument.class) && params[1].isAssignableFrom(Element.class)) {
                            LOG.debug("***** Found @InsertStatus for " + m.getName() + "(ResultDocument, Element) of " + clazz.getName());
                            m.invoke(cr, resdoc, resdoc.createNode(nodename));
                        } else {
                            throw new PustefixApplicationException("Exception when trying to call annotated method '@InsertStatus' " +
                                    "of " + clazz.getName() + ": Need either a signature of either " + 
                            "method(Element) or method(ResultDocument, Element)");
                        }
                        found_annotation = true;
                        break;
                    }
                }
                if (!found_annotation) {
                    LOG.debug("***** Serializing the complete resource " + clazz.getName());
                    ResultDocument.addObject(resdoc.createNode(nodename), cr);
                }
            }
            pe.save();
        }
    }

    
    public static void addResponseHeadersAndType(Context context, ResultDocument resdoc) {
        boolean have_config = true;
        Properties contextprops = context.getProperties();
        SPDocument doc = resdoc.getSPDocument();
        
        if (context.getConfigForCurrentPageRequest() == null) {
            // This page is not defined explicitly...
            have_config = false;
        }
        
        Properties props = null;
        String mime = null;
        if (have_config) {
            props = context.getPropertiesForCurrentPageRequest();
            mime = props.getProperty(MIMETYPE);
        }

        if (mime != null) {
            doc.setResponseContentType(mime);
        } else {
            doc.setResponseContentType(DEFAULTMIME);
        }

        // Set global headers first
        HashMap<String, String> headers = PropertiesUtils.selectProperties(contextprops, HEADER);
        if (headers != null && !headers.isEmpty()) {
            for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext();) {
                String key = iter.next();
                String val = headers.get(key);
                LOG.debug("* Adding response header: " + key + " => " + val);
                doc.addResponseHeader(key, val);
            }
        }

        // then set page specific headers
        if (have_config) {
            headers = PropertiesUtils.selectProperties(props, HEADER);
            if (headers != null && !headers.isEmpty()) {
                for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext();) {
                    String key = iter.next();
                    String val = headers.get(key);
                    LOG.debug("* Adding response header: " + key + " => " + val);
                    doc.addResponseHeader(key, val);
                }
            }
        }
    }

    
    public static boolean isDirectTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(State.SENDDATA);
        return (!isPageFlowRunning(context) && 
                
        		(context.getCurrentStatus() == PageRequestStatus.JUMP  || sdreq == null || !sdreq.isTrue()));
    }
    
    public static boolean isPageFlowRunning(Context context) {
    	return (context.getCurrentStatus() == PageRequestStatus.WORKFLOW);
    }
    
    public static boolean isSubmitTrigger(Context context, PfixServletRequest preq) {
        return isSubmitTriggerHelper(context, preq.getRequestParam(State.SENDDATA));
    }
    
    
    public static boolean isSubmitAuthTrigger(Context context, PfixServletRequest preq) {
        return isSubmitTriggerHelper(context, preq.getRequestParam(State.SENDAUTHDATA));
    }
    
    
    // ============ private Helper methods ============
    
    private static boolean isSubmitTriggerHelper(Context context, RequestParam sdreq) {
        return (!isPageFlowRunning(context) &&
                !(context.getCurrentStatus() == PageRequestStatus.JUMP) && sdreq != null && sdreq.isTrue());
    }  
}
