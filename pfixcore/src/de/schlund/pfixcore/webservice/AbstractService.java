/*
 * de.schlund.pfixcore.webservice.AbstractService
 */
package de.schlund.pfixcore.webservice;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;

/**
 * AbstractService.java 
 * 
 * Created: 29.06.2004
 * 
 * @author mleidig
 */
public abstract class AbstractService {

    protected ContextResourceManager getContextResourceManager() {
        MessageContext msgContext=MessageContext.getCurrentContext();
        ContextResourceManager crm=(ContextResourceManager)msgContext.getProperty(Constants.MSGCTX_PROP_CTXRESMAN);
        return crm;
    }
    /**
    protected ContextResource getContextResource(String name) {
        ContextResourceManager crm=getContextResourceManager();
        if(crm!=null) return crm.getResource(name);
        return null;
    }
    */
    private ContextResource getContextResource(HttpSession session,String name) {
        Enumeration enum=session.getAttributeNames();
        while(enum.hasMoreElements()) {
            String attName=(String)enum.nextElement();
            if(attName.endsWith("__CONTEXT__")) {
                Context context=(Context)session.getAttribute(attName);
                ContextResource res=context.getContextResourceManager().getResource(name);
                return res;
            }
        }
        return null;
    }
    
    protected ContextResource getContextResource(String name) throws Exception {
        MessageContext msgContext=MessageContext.getCurrentContext();
        HttpServletRequest req=(HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession session=req.getSession(false);
        if(session==null) throw new Exception("Valid pustefix session needed.");
        return getContextResource(session,name);
    }
    
    protected ContextResource getContextResource(String sid,String name) throws Exception {
        SessionAdmin admin=SessionAdmin.getInstance();
        HttpSession session=null;
        SessionInfoStruct sis=admin.getInfo(sid);
        if(sis!=null) session=sis.getSession();
        if(session==null) throw new Exception("Valid pustefix session needed.");
        return getContextResource(session,name);
    }
    
    /**
    private String extractSid(String sidStr) {
        if(sidStr.startsWith(Constants.SESSION_PREFIX)) {
                return sidStr.substring(Constants.SESSION_PREFIX.length());
        }
        return sidStr;
    }
    */
    
}


