package de.schlund.pfixcore.webservice;

import java.util.Enumeration;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import de.schlund.pfixxml.loader.AppLoader;

import javax.xml.soap.*;

import org.apache.log4j.Category;
import java.io.*; 

public class LoggingHandler extends BasicHandler {

    private Category  CAT  = Category.getInstance(this.getClass().getName());
    
  private static long next_message_id = 1;
  private String wsdlURL = null;
  private QName  serviceQName = null;
  private QName  portQName = null;

  /**
   * Constructor
   */
  public LoggingHandler() {
    super();
  }

  /**
   * Process and SOAP message
   */
  public void invoke(MessageContext messageContext) throws AxisFault {
    String  target = messageContext.getTargetService();
    if (target == null) {
        target = new String();
    }
    Message message=null;
    String  soap = "";
    String kind="";
    long time=0;
    if (!messageContext.getPastPivot()) {
      messageContext.setProperty(MonitorConstants.MONITOR_START_TIME,new Long(System.currentTimeMillis()));
      message = messageContext.getRequestMessage();
      kind="Request";
    } else {
      Long startTime=(Long)messageContext.getProperty(MonitorConstants.MONITOR_START_TIME);
      time=System.currentTimeMillis()-startTime.longValue();
      message = messageContext.getResponseMessage();
      kind="Response";
      try { 
          /**
          Message msg=messageContext.getCurrentMessage();
          if(msg!=null) {
              System.out.println("addheader");
              SOAPEnvelope env=msg.getSOAPEnvelope();
              SOAPHeaderElement header=new SOAPHeaderElement("http://www.schlund.de/pustefix","session","testid");
              env.addHeader(header);
              messageContext.setCurrentMessage(new Message(env));
          }
          */
      } catch(Exception x) {
          x.printStackTrace();
      }
    }
    
    if (message != null) {
        soap = ((SOAPPart)message.getSOAPPart()).getAsString();
      }
    
    StringBuffer sb=new StringBuffer();
    sb.append("\n----------------------------------------------\n\n");
    sb.append("Service: "+target+"\n");
    if(kind.equals("Response")) sb.append("Time: "+time+"ms"+"\n");
    sb.append("\nHeader: "+"\n");
    sb.append(logHttpHeaders(messageContext));
    sb.append("\n"+kind+":"+"\n");
    sb.append(soap+"\n");
    try {
    FileWriter fw=new FileWriter("webservice.log",true);
    BufferedWriter bw=new BufferedWriter(fw);
    bw.write(sb.toString());
    bw.close();
    fw.close();
    } catch(Exception x) {
        x.printStackTrace();
    }
    
    //logSession(messageContext);
  }
  
  protected void logSession(MessageContext msgCtx) {
      HttpServletRequest req=(HttpServletRequest)msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
     
  }
  
  protected String logHttpHeaders(MessageContext msgCtx) {
      StringBuffer sb=new StringBuffer();
      HttpServletRequest req=(HttpServletRequest)msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
      Enumeration enum=req.getHeaderNames();
      while(enum.hasMoreElements()) {
          String name=(String)enum.nextElement();
          String val=req.getHeader(name);
          sb.append(name+": "+val+"\n");
      }
      return sb.toString();
  }
  

}
