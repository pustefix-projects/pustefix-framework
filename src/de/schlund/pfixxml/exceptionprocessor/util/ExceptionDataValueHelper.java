/*
 * Created on 03.06.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.exceptionprocessor.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;


import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;

/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExceptionDataValueHelper {
    
	/**
	 * @param exception
	 * @param pfixReq
	 * @return
	 */
	public static ExceptionDataValue createExceptionDataValue(Throwable exception, PfixServletRequest pfixReq) {
		ExceptionDataValue exdata = new ExceptionDataValue();
		exdata.setThrowable(exception);
		exdata.setScheme(pfixReq.getScheme());
		exdata.setServername(pfixReq.getServerName());
		exdata.setPort(pfixReq.getServerPort());
		exdata.setUri(pfixReq.getRequestURI());
		final HttpSession session = pfixReq.getSession(false);
		final String id = session.getId();
		exdata.setSessionid(id);
		exdata.setServlet(pfixReq.getServletName());
		exdata.setPage(new PageRequest(pfixReq).getName());
		exdata.setQuery(pfixReq.getQueryString());
		
		HashMap keysnvalues = new HashMap();
		String[] param_names = pfixReq.getRequestParamNames();
		for(int i=0; i<param_names.length; i++) {
			keysnvalues.put(param_names[i], pfixReq.getRequestParam(param_names[i]).getValue());
		}
		exdata.setRequestParams(keysnvalues);
		
		 
		SessionAdmin sessadmin = SessionAdmin.getInstance();
		SessionInfoStruct info = sessadmin.getInfo(id);
		ArrayList steps = new ArrayList();
	    if(info != null) {
	    	LinkedList trail = info.getTraillog();
	    	if (trail != null && trail.size() > 0) {
	    		for (Iterator j = trail.listIterator(); j.hasNext();) {
	    			SessionInfoStruct.TrailElement step = (SessionInfoStruct.TrailElement) j.next();
	        		steps.add("[" + step.getCounter() + "] " + step.getStylesheetname() + " [" + step.getServletname() + "]");
	        	}
	        }
	    }
	    exdata.setLastSteps(steps);
	    
	    
	    HashMap sessdata = new HashMap();
        Enumeration enm = session.getAttributeNames();
        while (enm.hasMoreElements()) {
            String key = (String) enm.nextElement();
            Object value = session.getAttribute(key);
            String strvalue = null;
            try {
                strvalue = value.toString();
            } catch(Exception e) {
                // Catch all exceptions here. If an exception occurs in context.toString
                // we definitly want the exception-info to be generated.
                strvalue = e.getMessage();
            }
            sessdata.put(key, strvalue);
        }
	    
        exdata.setSessionKeysAndValues(sessdata);
		return exdata;
	}


}
