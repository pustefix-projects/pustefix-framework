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

package de.schlund.pfixcore.webservice.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;

import de.schlund.pfixcore.webservice.ServiceRuntime;

/**
 * @author mleidig@schlund.de
 */
public abstract class AbstractHandler extends BasicHandler {

    protected ServletContext getServletContext(MessageContext msgContext) {
        ServletContext context=null;
        HttpServlet servlet=(HttpServlet)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
        if(servlet!=null) context=servlet.getServletContext();
        return context;
    }

    protected ServiceRuntime getServiceRuntime(MessageContext msgContext) {
    	ServiceRuntime runtime=null;
        ServletContext context=getServletContext(msgContext);
        if(context!=null) runtime=(ServiceRuntime)context.getAttribute(ServiceRuntime.class.getName());
        return runtime;
    }
    
    protected HttpServletRequest getServletRequest(MessageContext msgContext) {
    	 HttpServletRequest req=(HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
         return req;
    }
    
    protected HttpServletResponse getServletResponse(MessageContext msgContext) {
        HttpServletResponse res=(HttpServletResponse)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        return res;
    }
    
    protected HttpSession getSession(MessageContext msgContext) {
        HttpSession session=null;
        HttpServletRequest req=getServletRequest(msgContext);
        if(req!=null) session=req.getSession(false);
        return session;
    }
    
}
