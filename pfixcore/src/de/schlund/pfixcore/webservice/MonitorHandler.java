package de.schlund.pfixcore.webservice;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;

import javax.xml.namespace.QName;

/**
 * This handler is used to monitor web service requests
 * SOAP monitor service.
 *
 * @author Brian Price (pricebe@us.ibm.com)
 */

public class MonitorHandler extends BasicHandler {

  private static long next_message_id = 1;
  private String wsdlURL = null;
  private QName  serviceQName = null;
  private QName  portQName = null;

  /**
   * Constructor
   */
  public MonitorHandler() {
    super();
  }

  /**
   * Process and SOAP message
   */
  public void invoke(MessageContext messageContext) throws AxisFault {
    String  target = messageContext.getTargetService();
    // Check for null target
    if (target == null) {
        target = new String();
    }
    // Get id, type and content
    Long    id;
    Integer type;
    Message message;
    if (!messageContext.getPastPivot()) {
      id = assignMessageId(messageContext);
      type = new Integer(MonitorConstants.MONITOR_REQUEST);
      message = messageContext.getRequestMessage();
    } else {
      id = getMessageId(messageContext);
      type = new Integer(MonitorConstants.MONITOR_RESPONSE);
      message = messageContext.getResponseMessage();
    }
    // Get the SOAP portion of the message
    String  soap = null;
    if (message != null) {
      soap = ((SOAPPart)message.getSOAPPart()).getAsString();
    }
    // If we have an id and a SOAP portion, then send the
    // message to the SOAP monitor service
    if ((id != null) && (soap != null)) {
      MonitorService.publishMessage(id,type,target,soap);
    }
  }

  /**
   * Assign a new message id
   */
  private Long assignMessageId(MessageContext messageContext) {
    Long id = null;
    synchronized(MonitorConstants.MONITOR_ID) {
      id = new Long(next_message_id);
      next_message_id++;
    }
    messageContext.setProperty(MonitorConstants.MONITOR_ID, id);
    return id;
  }

  /**
   * Get the already assigned message id
   */
  private Long getMessageId(MessageContext messageContext) {
    Long id = null;
    id = (Long) messageContext.getProperty(MonitorConstants.MONITOR_ID);
    return id;
  }
}
