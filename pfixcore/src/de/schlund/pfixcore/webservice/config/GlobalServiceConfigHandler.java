/*
 * de.schlund.pfixcore.webservice.config.GlobalServiceConfigHandler
 */
package de.schlund.pfixcore.webservice.config;

import java.io.FileInputStream;

import de.schlund.pfixcore.webservice.util.*;

/**
 * GlobalServiceConfigHandler.java 
 * 
 * Created: 01.10.2004
 * 
 * @author mleidig
 */
public class GlobalServiceConfigHandler extends XMLHandler {

    GlobalServiceConfig obj;
    
    public GlobalServiceConfigHandler() {
        obj=new GlobalServiceConfig();
    }
    
    public void handleStartElement(String uri,String localName,String qName,XMLAttributes attributes) throws XMLProcessingException {
        String val=null;
        System.out.println("GSCH: "+getContextPath());
        if(getContextPath().equals("/webservice-global")) {
            if(qName.equals("server")) {
                val=attributes.getValue("name");
                if(val!=null) obj.setServer(val);
            } else if(qName.equals("wsdlsupport")) {
                val=attributes.getValue("enabled");
                if(val!=null) obj.setWSDLSupportEnabled(XMLTypeConverter.toBoolean(val));
                val=attributes.getValue("repository");
                if(val!=null) obj.setWSDLRepository(val);
            } else if(qName.equals("stubgeneration")) {
                val=attributes.getValue("enabled");
                if(val!=null) obj.setStubGenerationEnabled(XMLTypeConverter.toBoolean(val));
                val=attributes.getValue("repository");
                if(val!=null) obj.setStubRepository(val);
            } else if(qName.equals("encoding")) {
                val=attributes.getValue("style");
                if(val!=null) obj.setEncodingStyle(val);
                val=attributes.getValue("use");
                if(val!=null) obj.setEncodingUse(val);
            } else if(qName.equals("session")) {
                val=attributes.getValue("type");
                if(val!=null) obj.setSessionType(val);
            } else if(qName.equals("admin")) {
                val=attributes.getValue("enabled");
                if(val!=null) obj.setAdminEnabled(XMLTypeConverter.toBoolean(val));
            } else if(qName.equals("monitoring")) {
                val=attributes.getValue("enabled");
                if(val!=null) obj.setMonitoringEnabled(XMLTypeConverter.toBoolean(val));
                val=attributes.getValue("scope");
                if(val!=null) obj.setMonitoringScope(val);
                val=attributes.getValue("historysize");
                if(val!=null) obj.setMonitoringHistorySize(XMLTypeConverter.toInteger(val));
            } else if(qName.equals("logging")) {
                val=attributes.getValue("logging");
                if(val!=null) obj.setLoggingEnabled(XMLTypeConverter.toBoolean(val));
            }
        }
    }
    
    public void handleEndElement(String uri,String localName,String qName) throws XMLProcessingException {
        String val=null;
        if(qName.equals("requestpath")) {
            val=getContent();
            if(val!=null) obj.setRequestPath(val);    
        }
    }
    
    public GlobalServiceConfig getGlobalServiceConfig() {
        return obj;
    }
    
  
}
