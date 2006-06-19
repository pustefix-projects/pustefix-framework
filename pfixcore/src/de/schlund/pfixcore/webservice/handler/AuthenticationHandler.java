package de.schlund.pfixcore.webservice.handler;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.WebServiceContext;
import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.contextxmlserver.ContextWrapper;
import de.schlund.pfixxml.contextxmlserver.ServerContextImpl;
import de.schlund.pfixxml.contextxmlserver.SessionContextImpl;
import de.schlund.pfixxml.serverutil.SessionAdmin;

public class AuthenticationHandler extends AbstractHandler {

  public AuthenticationHandler() {
    super();
  }
  
  public void invoke(MessageContext messageContext) throws AxisFault {
    String  target = messageContext.getTargetService();
    if (target == null) {
        target = new String();
    }
   
    if (!messageContext.getPastPivot()) {
        //handle request
        WebServiceContext context=getWebServiceContext(messageContext);
       
        Configuration config=context.getConfiguration();
        ServiceConfig srvConf=config.getServiceConfig(target);
        if(srvConf==null) throw AxisFault.makeFault(new Exception("Target service doesn't exist"));
        if(srvConf.getContextName()!=null) {
            if(srvConf.getSessionType().equals(Constants.SESSION_TYPE_SERVLET)) {
                HttpSession session=getSession(messageContext);
                if(session==null) throw AxisFault.makeFault(new Exception("Authentication failed: No valid session."));
                HttpServletRequest req=this.getServletRequest(messageContext);
                if(srvConf.getSSLForce() && !req.getScheme().equals("https")) throw AxisFault.makeFault(new Exception("Authentication failed: SSL connection required"));
                if(req.getScheme().equals("https")) {
                    Boolean secure=(Boolean)session.getAttribute(SessionAdmin.SESSION_IS_SECURE);
                    if(secure==null || !secure.booleanValue()) throw AxisFault.makeFault(new Exception("Authentication failed: No secure session"));
                }
                String contextname = srvConf.getContextName() + "__CONTEXT__";
                ServerContextImpl srvcontext = (ServerContextImpl) getServletContext(messageContext).getAttribute(contextname);
                SessionContextImpl sesscontext = (SessionContextImpl) session.getAttribute(contextname);
                State authstate = srvcontext.getAuthState();
                Context wcontext = new ContextWrapper(srvcontext, sesscontext, null);
                if (authstate != null) {
                    PfixServletRequest preq = new PfixServletRequest(getServletRequest(messageContext), new Properties());
                    try {
                        if (!authstate.isAccessible(wcontext, preq)) {
                            throw new XMLException("State of authpage is not accessible!");                        }
                        if (authstate.needsData(wcontext, preq)) {
                            throw new Exception("Authorization failed");
                        }
                    } catch (Exception e) {
                        throw AxisFault.makeFault(e);
                    }

                }
                ContextResourceManager crm = sesscontext.getContextResourceManager();
                messageContext.setProperty(Constants.MSGCTX_PROP_CTX,wcontext);
                messageContext.setProperty(Constants.MSGCTX_PROP_CTXRESMAN,crm);
            }
        }
    } else {
        //handle response
    }
  }

}
