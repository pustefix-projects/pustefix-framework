/*
 * de.schlund.pfixcore.webservice.config.ConfigurationHandler
 */
package de.schlund.pfixcore.webservice.config;

import java.io.FileInputStream;

import de.schlund.pfixcore.webservice.util.*;

/**
 * ConfigurationHandler.java 
 * 
 * Created: 01.10.2004
 * 
 * @author mleidig
 */
public class ConfigurationHandler extends XMLHandler {

    Configuration obj;
    XMLHandler childHandler;
    
    public ConfigurationHandler() {
        obj=new Configuration();
    }
    
    public void handleStartElement(String uri,String localName,String qName,XMLAttributes attributes) throws XMLProcessingException {
        if(childHandler!=null) {
            childHandler.handleStartElement(uri,localName,qName,attributes);
            return;
        }
        System.out.println("CH: "+getContextPath());
        String val=null;
        if(getContextPath().equals("/webservice-config")) {
            if(qName.equals("webservice-global")) {
                childHandler=new GlobalServiceConfigHandler();
                childHandler.handleStartElement(uri,localName,qName,attributes);
            } else if(qName.equals("webservice")) {
                childHandler=new ServiceConfigHandler();
                childHandler.handleStartElement(uri,localName,qName,attributes);
            }
        }
    }
    
    public void handleEndElement(String uri,String localName,String qName) throws XMLProcessingException {
        if(getContextPath().equals("/webservice-config")) {
            if(qName.equals("webservice-global")) {
                obj.setGlobalServiceConfig(((GlobalServiceConfigHandler)childHandler).getGlobalServiceConfig());
                childHandler=null;
            } else if(qName.equals("webservice")) {
                obj.addServiceConfig(((ServiceConfigHandler)childHandler).getServiceConfig());
                childHandler=null;
            }
        }
        if(childHandler!=null) {
            childHandler.handleEndElement(uri,localName,qName);
            return;
        }
    }
    
    public Configuration getConfiguration() {
        return obj;
    }
    
    public static void main(String args[]) throws Exception {
        XMLHandler handler=new ConfigurationHandler();
        XMLResourceReader reader=new XMLResourceReader(new FileInputStream("/home/mleidig/workspace/pfixcore_ws/example/webservice/conf/webservice.xml"),"test",handler,false);
        reader.read();
    }
    
}
