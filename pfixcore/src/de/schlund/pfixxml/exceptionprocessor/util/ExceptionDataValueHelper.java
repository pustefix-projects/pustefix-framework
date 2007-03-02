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

package de.schlund.pfixxml.exceptionprocessor.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

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
    private static Logger LOG = Logger.getLogger(ExceptionDataValueHelper.class);
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
        String pagename = pfixReq.getPageName();
		if (pagename == null) {
            exdata.setPage("null");
        }
        else {
            exdata.setPage(pagename);
        }
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
                LOG.error("Exception while dumping session!", e);
                strvalue = e.getMessage() == null ? e.toString() : e.getMessage();
            }
            sessdata.put(key, strvalue);
        }
	    
        exdata.setSessionKeysAndValues(sessdata);
		return exdata;
	}


}
