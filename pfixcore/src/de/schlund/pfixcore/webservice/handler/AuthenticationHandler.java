package de.schlund.pfixcore.webservice.handler;

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
                Context pfxContext=(Context)session.getAttribute(srvConf.getContextName()+"__CONTEXT__");
                try {
                    if(pfxContext.checkAuthorization(false)!=null) throw AxisFault.makeFault(new Exception("Authorization failed"));
                } catch(Exception x) {
                    throw AxisFault.makeFault(new Exception("Authorization failed"));
                }
                ContextResourceManager crm=pfxContext.getContextResourceManager();
                messageContext.setProperty(Constants.MSGCTX_PROP_CTX,pfxContext);
                messageContext.setProperty(Constants.MSGCTX_PROP_CTXRESMAN,crm);
            }
        }
    } else {
        //handle response
    }
  }

}
