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

import javax.servlet.http.HttpSession;

import org.pustefixframework.http.ErrorFilter;
import org.pustefixframework.http.ErrorFilter.TraceBackEntry;
import org.pustefixframework.http.ErrorFilter.TraceBackList;
import org.springframework.util.ClassUtils;

import de.schlund.pfixxml.PfixServletRequest;


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
		if(session != null) {
		    exdata.setSessionid(session.getId());
		}
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

        ArrayList<String> lastSteps = new ArrayList<>();
        TraceBackList traceList = ErrorFilter.getTraceBackList(pfixReq.getRequest());
        if(traceList != null) {
            for(TraceBackEntry entry:traceList.getEntries()) {
                lastSteps.add("[" + entry.count + "] " + entry.method + " " + entry.requestURI
                        + " (" + entry.status + ")");
            }
        }
        exdata.setLastSteps(lastSteps);

	    HashMap<String, String> sessdata = new HashMap<String, String>();
	    try {
            if(session != null) {
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
            }
        } catch(IllegalStateException x) {
            //Session is already invalidated
        }

        exdata.setSessionKeysAndValues(sessdata);
        return exdata;
    }

}
