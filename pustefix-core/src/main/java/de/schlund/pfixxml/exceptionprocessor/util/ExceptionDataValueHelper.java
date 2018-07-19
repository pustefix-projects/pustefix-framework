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

package de.schlund.pfixxml.exceptionprocessor.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

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
    private final static Logger LOG = LoggerFactory.getLogger(ExceptionDataValueHelper.class);
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
		
		HashMap<String, String> keysnvalues = new HashMap<String, String>();
		String[] param_names = pfixReq.getRequestParamNames();
		for(int i=0; i<param_names.length; i++) {
			keysnvalues.put(param_names[i], pfixReq.getRequestParam(param_names[i]).getValue());
		}
		exdata.setRequestParams(keysnvalues);

		SessionAdmin sessionAdmin = getSessionAdmin(pfixReq);
		if(sessionAdmin!=null) {
    		SessionInfoStruct info = sessionAdmin.getInfo(id);
    		ArrayList<String> steps = new ArrayList<String>();
    	    if(info != null) {
    	    	LinkedList<SessionInfoStruct.TrailElement> trail = info.getTraillog();
    	    	if (trail != null && trail.size() > 0) {
    	    		for (Iterator<SessionInfoStruct.TrailElement> j = trail.listIterator(); j.hasNext();) {
    	    			SessionInfoStruct.TrailElement step = j.next();
    	        		steps.add("[" + step.getCounter() + "] " + step.getStylesheetname() + " [" + step.getServletname() + "]");
    	        	}
    	        }
    	    }
    	    exdata.setLastSteps(steps);
		}
	    
	    HashMap<String, String> sessdata = new HashMap<String, String>();
	    try {
	        Enumeration<?> enm = session.getAttributeNames();
	        while (enm.hasMoreElements()) {
	            String key = (String) enm.nextElement();
	            Object value = session.getAttribute(key);
	            String strvalue = null;
	            if(value != null) {
	                if(ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
	                    try {
	                        strvalue = value.toString();
	                    } catch(Exception e) {
	                        strvalue = e.toString();
	                    }
	                } else {
	                    strvalue = value.getClass().getName() + '@' + Integer.toHexString(value.hashCode());
	                }
	                sessdata.put(key, strvalue);
	            }
	        }
	    } catch(IllegalStateException x) {
	    	//Session is already invalidated
	    }
	    
        exdata.setSessionKeysAndValues(sessdata);
		return exdata;
	}

    private static SessionAdmin getSessionAdmin(PfixServletRequest pfixReq) {
        HttpServletRequest req = pfixReq.getRequest();
        if(req == null) {
            req = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        }
        ApplicationContext appContext = RequestContextUtils.findWebApplicationContext(req);
        try {
            SessionAdmin sessionAdmin = (SessionAdmin)appContext.getBean(SessionAdmin.class.getName());
            return sessionAdmin;
        } catch(NoSuchBeanDefinitionException x) {
            LOG.error("Can't get SessionAdmin bean from ApplicationContext");
        }
        return null;
    }

}
