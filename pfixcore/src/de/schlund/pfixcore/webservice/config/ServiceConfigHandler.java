/*
 * de.schlund.pfixcore.webservice.config.ServiceConfigHandler
 */
package de.schlund.pfixcore.webservice.config;

import de.schlund.pfixcore.webservice.util.*;

/**
 * ServiceConfigHandler.java 
 * 
 * Created: 01.10.2004
 * 
 * @author mleidig
 */
public class ServiceConfigHandler extends XMLHandler {

    ServiceConfig obj;
    
    public ServiceConfigHandler() {
        obj=new ServiceConfig();
    }
    
    public void handleStartElement(String uri,String localName,String qName,XMLAttributes attributes) throws XMLProcessingException {
        String val=null;
        System.out.println("SCH: "+getContextPath());
        if(getContextPath().equals("/")) {
            if(qName.equals("webservice")) {
                val=attributes.getValue("name");
                if(val!=null) obj.setName(val);
            }
        }
        if(getContextPath().equals("/webservice")) {
            if(qName.equals("interface")) {
                val=attributes.getValue("name");
                if(val!=null) obj.setInterfaceName(val);
            } else if(qName.equals("implementation")) {
                val=attributes.getValue("name");
                if(val!=null) obj.setImplementationName(val);
            } else if(qName.equals("session")) {
                val=attributes.getValue("type");
                if(val!=null) obj.setSessionType(val);
            } else if(qName.equals("context")) {
                val=attributes.getValue("name");
                if(val!=null) obj.setContextName(val);
            }
        }
    }
    
    public void handleEndElement(String uri,String localName,String qName) throws XMLProcessingException {
    }
    
    public ServiceConfig getServiceConfig() {
        return obj;
    }
    
}
