package de.schlund.pfixcore.webservice.handler;

import java.util.Enumeration;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;

import org.apache.log4j.Category;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

import de.schlund.pfixcore.webservice.*;
import de.schlund.pfixcore.webservice.config.*;

public class AuthenticationHandler extends AbstractHandler {

  private static long next_message_id = 1;
  private String wsdlURL = null;
  private QName  serviceQName = null;
  private QName  portQName = null;

  public AuthenticationHandler() {
    super();
  }
  
  public void invoke(MessageContext messageContext) throws AxisFault {
    String  target = messageContext.getTargetService();
    if (target == null) {
        target = new String();
    }
    long time=0;
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
                Context pfxContext=(Context)session.getAttribute(srvConf.getContextName()+"__CONTEXT__");
                try {
                    if(pfxContext.checkAuthorization()!=null) throw AxisFault.makeFault(new Exception("Authorization failed"));
                } catch(Exception x) {
                    throw AxisFault.makeFault(new Exception("Authorization failed"));
                }
                ContextResourceManager crm=pfxContext.getContextResourceManager();
                messageContext.setProperty(Constants.MSGCTX_PROP_CTXRESMAN,crm);
            }
        }
    } else {
        //handle response
    }
  }

}
