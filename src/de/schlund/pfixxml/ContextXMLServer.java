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

package de.schlund.pfixxml;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;
import org.w3c.dom.*;

/**
 * @author jtl
 *
 *
 */


public class ContextXMLServer extends AbstractXMLServer {
    private              Category CAT            = Category.getInstance(ContextXMLServer.class.getName());
    public  final static String   CONTEXT_SUFFIX = "__CONTEXT__";
    public  final static String   CONTEXT_CLASS  = "context.class";
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected boolean needsSession() {
        return true;
    }
   
    protected boolean allowSessionCreate() {
        return true;
    }

    protected boolean tryReloadProperties(PfixServletRequest preq) throws ServletException {
        if (super.tryReloadProperties(preq)) {
        	PropertyObjectManager.getInstance().resetPropertyObjects(getProperties());
            try {
                getContext(preq, true);
            } catch (Exception e) {
                throw new ServletException("When reloading for Context: " + e);
            }
            return true;
        } else {
            return false;
        }
    }

    
    public SPDocument getDom(PfixServletRequest preq) throws Exception {
        AppContext context = getContext(preq, false);
        SPDocument spdoc   = context.handleRequest(preq);
        return spdoc;
    }

    private AppContext getContext(PfixServletRequest preq, boolean reset) throws Exception {
        String        contextname = servletname + CONTEXT_SUFFIX;
        HttpSession   session     = preq.getSession(false);
        ContainerUtil conutil     = getContainerUtil();
        if (session == null) {
            throw new XMLException("No valid session found! Aborting...");
        }
        AppContext context = (AppContext) conutil.getSessionValue(session, contextname);
        if (context == null) {
            context = createContext();
            conutil.setSessionValue(session, contextname, context);
        } else if (reset) {
            AppContext tmp = createContext();
            if (tmp.getClass().getName().equals(context.getClass().getName())) {
                context.reset();
            } else {
                conutil.setSessionValue(session, contextname, null);
                return null;
            }
        }
        return context;
    }
    

    private AppContext createContext() throws Exception {
        String contextclass = getProperties().getProperty(CONTEXT_CLASS);
        if (contextclass == null) {
            throw (new XMLException("Need name for context class from context.class property"));
        }
        AppContext context = (AppContext) Class.forName(contextclass).newInstance();
        context.init(getProperties(), getContainerUtil());
        return context;
    }
}
