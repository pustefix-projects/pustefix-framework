package de.schlund.pfixcore.webservice;

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
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.workflow.ContextResourceManager;

import de.schlund.pfixcore.webservice.config.*;

public class AuthenticationHandler extends BasicHandler {

    
  private static long next_message_id = 1;
  private String wsdlURL = null;
  private QName  serviceQName = null;
  private QName  portQName = null;

  /**
   * Constructor
   */
  public AuthenticationHandler() {
    super();
  }

  private HttpSession getSession(MessageContext msgContext) {
      HttpServletRequest req=(HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
      HttpSession session=req.getSession(false);
      return session;
  }
  
  private ServletContext getServletContext(MessageContext msgContext) {
      HttpServlet srv = (HttpServlet)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
      ServletContext context = srv.getServletContext();
      return context;
  }
  
  private WebServiceContext getWebServiceContext(MessageContext msgContext) {
      return (WebServiceContext)getServletContext(msgContext).getAttribute(Constants.WEBSERVICE_CONTEXT);
  }
  
  private void getContextResourceManager(HttpSession session) {
      Enumeration enum=session.getAttributeNames();
      while(enum.hasMoreElements()) {
          String attName=(String)enum.nextElement();
          /**
          if(attName.endsWith("__CONTEXT__")) {
              Context context=(Context)session.getAttribute(attName);
              ContextResource res=context.getContextResourceManager().getResource(name);
              return res;
          }*/
        
      }
  }
  
  
  /**
   * Process and SOAP message
   */
  public void invoke(MessageContext messageContext) throws AxisFault {
    String  target = messageContext.getTargetService();
    if (target == null) {
        target = new String();
    }
    long time=0;
    if (!messageContext.getPastPivot()) {
        //handle request
        WebServiceContext context=getWebServiceContext(messageContext);
       
        ServiceConfiguration config=context.getServiceConfiguration();
        ServiceConfig srvConf=config.getServiceConfig(target);
        if(srvConf.getContextName()!=null) {
            if(srvConf.getSessionType()==Constants.SESSION_TYPE_SERVLET) {
                HttpSession session=getSession(messageContext);
                if(session==null) throw AxisFault.makeFault(new Exception("Authentication failed: No valid session."));
               
                getContextResourceManager(session);
                Context pfxContext=(Context)session.getAttribute(srvConf.getContextName()+"__CONTEXT__");
                ContextResourceManager crm=pfxContext.getContextResourceManager();
                messageContext.setProperty(Constants.MSGCTX_PROP_CTXRESMAN,crm);
            }
        }
    } else {
        //handle response
    }
  }

}
