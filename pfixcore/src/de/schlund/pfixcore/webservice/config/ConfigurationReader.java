/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.webservice.config;

import java.io.CharArrayWriter;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.fault.FaultHandler;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.resources.FileResource;

/**
 * @author mleidig@schlund.de
 */
public class ConfigurationReader extends DefaultHandler {

	FileResource configFile;
	Configuration config;
	Stack<Object> contextStack=new Stack<Object>();
	Object context;
	CharArrayWriter content=new CharArrayWriter();

	public static Configuration read(FileResource file) throws Exception {
		ConfigurationReader reader=new ConfigurationReader(file);
		reader.read();
		return reader.getConfiguration();
	}
	
	public Configuration getConfiguration() {
		return config;
	}
	
	public ConfigurationReader(FileResource configFile) {
		this.configFile=configFile;
	}
	
	public void read() throws Exception {
		CustomizationHandler cushandler=new CustomizationHandler(this,Constants.WS_CONF_NS,Constants.CUS_NS);
        SAXParserFactory spf=SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser parser=spf.newSAXParser();
        parser.parse(configFile.getInputStream(),cushandler);
	}
	
	private void setContext(Object obj) {
		contextStack.add(obj);
		context=obj;
	}
	
	private void resetContext() {
		contextStack.pop();
		if(contextStack.empty()) context=null;
		else context=contextStack.peek();
	}
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		content.reset();
		if(context==null) {
			if(localName.equals("webservice-config")) {
				config=new Configuration();
				setContext(config);
			}
		} else if(context instanceof Configuration) {
			if(localName.equals("webservice-global")) {
				GlobalServiceConfig globSrvConf=new GlobalServiceConfig();
				config.setGlobalServiceConfig(globSrvConf);
				setContext(globSrvConf);
			} else if(localName.equals("webservice")) {
				ServiceConfig srvConf=new ServiceConfig();
				String name=getStringAttribute(atts,"name",true);
				srvConf.setName(name);
				config.addServiceConfig(srvConf);
				setContext(srvConf);
			}
		} else if(context instanceof GlobalServiceConfig) {
			GlobalServiceConfig globSrvConf=(GlobalServiceConfig)context;
			if(localName.equals("wsdlsupport")) {
				boolean wsdlSupport=getBooleanAttribute(atts,"enabled",true,false);
				globSrvConf.setWSDLSupportEnabled(wsdlSupport);
				if(wsdlSupport) {
					String wsdlRepo=getStringAttribute(atts,"repository",true);
					globSrvConf.setWSDLRepository(wsdlRepo);
				}
			} else if(localName.equals("stubgeneration")) {
				 boolean stubGeneration=getBooleanAttribute(atts,"enabled",true,false);
				 globSrvConf.setStubGenerationEnabled(stubGeneration);
				 if(stubGeneration) {
					 String stubRepo=getStringAttribute(atts,"repository",true);
					 globSrvConf.setStubRepository(stubRepo);
				 }
			} else if(localName.equals("protocol")) {
				String proto=getStringAttribute(atts,"type",Constants.PROTOCOL_TYPES,Constants.PROTOCOL_TYPE_ANY);
				globSrvConf.setProtocolType(proto);
			} else if(localName.equals("encoding")) {
				String encStyle=getStringAttribute(atts,"style",Constants.ENCODING_STYLES,true);
				globSrvConf.setEncodingStyle(encStyle);
		        String encUse=getStringAttribute(atts,"use",Constants.ENCODING_USES,true);
		        globSrvConf.setEncodingUse(encUse);
			} else if(localName.equals("session")) {
				String sessType=getStringAttribute(atts,"type",Constants.SESSION_TYPES,GlobalServiceConfig.DEFAULT_SESSTYPE);
				globSrvConf.setSessionType(sessType);
			} else if(localName.equals("scope")) {
				String scopeType=getStringAttribute(atts,"type",Constants.SERVICE_SCOPES,GlobalServiceConfig.DEFAULT_SCOPETYPE);
				globSrvConf.setScopeType(scopeType);
			} else if(localName.equals("admin")) {
				boolean admin=getBooleanAttribute(atts,"enabled",true,false);
				globSrvConf.setAdminEnabled(admin);
			} else if(localName.equals("monitoring")) {
				boolean monitoring=getBooleanAttribute(atts,"enabled",true,false);
				globSrvConf.setMonitoringEnabled(monitoring);
		        if(monitoring) {
		            String monitorScope=getStringAttribute(atts,"scope",Constants.MONITOR_SCOPES,true);
		            globSrvConf.setMonitoringScope(monitorScope);
		            int monitorSize=getIntegerAttribute(atts,"historysize",true,0);
		            globSrvConf.setMonitoringHistorySize(monitorSize);
		        }
			} else if(localName.equals("logging")) {
				boolean logging=getBooleanAttribute(atts,"enabled",true,false);
				globSrvConf.setLoggingEnabled(logging);
			} else if(localName.equals("faulthandler")) {
				 FaultHandler faultHandler=(FaultHandler)getObjectAttribute(atts,"class",FaultHandler.class,false);
				 globSrvConf.setFaultHandler(faultHandler);
				 setContext(faultHandler);
			}
		} else if(context instanceof ServiceConfig) {
			ServiceConfig srvConf=(ServiceConfig)context;
			if(localName.equals("interface")) {
				String name=getStringAttribute(atts,"name",true);
				srvConf.setInterfaceName(name);
			} else if(localName.equals("implementation")) {
				String name=getStringAttribute(atts,"name",true);
				srvConf.setImplementationName(name);
			} else if(localName.equals("protocol")) {
				String proto=getStringAttribute(atts,"type",Constants.PROTOCOL_TYPES,Constants.PROTOCOL_TYPE_ANY);
				srvConf.setProtocolType(proto);
			} else if(localName.equals("encoding")) {
				String encStyle=getStringAttribute(atts,"style",Constants.ENCODING_STYLES,true);
				srvConf.setEncodingStyle(encStyle);
		        String encUse=getStringAttribute(atts,"use",Constants.ENCODING_USES,true);
		        srvConf.setEncodingUse(encUse);
			} else if(localName.equals("session")) {
				String sessType=getStringAttribute(atts,"type",Constants.SESSION_TYPES,GlobalServiceConfig.DEFAULT_SESSTYPE);
				srvConf.setSessionType(sessType);
			} else if(localName.equals("scope")) {
				String scopeType=getStringAttribute(atts,"type",Constants.SERVICE_SCOPES,GlobalServiceConfig.DEFAULT_SCOPETYPE);
				srvConf.setScopeType(scopeType);
			} else if(localName.equals("ssl")) {
				boolean sslForce=getBooleanAttribute(atts,"force",true,true);
				srvConf.setSSLForce(sslForce);
			} else if(localName.equals("context")) {
				String ctxName=getStringAttribute(atts,"name",true);
				srvConf.setContextName(ctxName);
			} else if(localName.equals("faulthandler")) {
				 FaultHandler faultHandler=(FaultHandler)getObjectAttribute(atts,"class",FaultHandler.class,false);
				 srvConf.setFaultHandler(faultHandler);
				 setContext(faultHandler);
			}
		} else if(context instanceof FaultHandler) {
			FaultHandler faultHandler=(FaultHandler)context;
			if(localName.equals("param")) {
				String name=getStringAttribute(atts,"name",true);
				String value=getStringAttribute(atts,"value",true);
				faultHandler.addParam(name,value);
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(context instanceof Configuration) {
			if(localName.equals("webservice-config")) {
				resetContext();
			}
		} else if(context instanceof GlobalServiceConfig) {
			GlobalServiceConfig globSrvConf=(GlobalServiceConfig)context;
			if(localName.equals("webservice-global")) {
				resetContext();
			} else if(localName.equals("requestpath")) {
				globSrvConf.setRequestPath(getContent());
			}
		} else if(context instanceof ServiceConfig) {
			if(localName.equals("webservice")) {
				resetContext();
			} 
		} else if(context instanceof FaultHandler) {
			if(localName.equals("faulthandler")) {
                FaultHandler faultHandler=(FaultHandler)context;
                faultHandler.init();
				resetContext();
			}
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		content.write(ch,start,length);
	}
	
	/**
	public void startDocument() throws SAXException {}
	public void endDocument() throws SAXException {}
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
	public void processingInstruction(String target, String data) throws SAXException {}
	public void setDocumentLocator(Locator locator) {}
	public void skippedEntity(String name) throws SAXException {}
	public void startPrefixMapping(String prefix, String uri) throws SAXException {}
	public void endPrefixMapping(String prefix) throws SAXException {}
	*/
	
    public String getContent() {
    	return content.toString();
    }
	
    public String getStringAttribute(Attributes attributes,String attrName,boolean mandatory) throws ConfigException {
        String val=attributes.getValue(attrName);
        if(val==null && mandatory) throw new ConfigException(ConfigException.MISSING_ATTRIBUTE,attrName);
        return val.trim();
    }
    
    public String getStringAttribute(Attributes attributes,String attrName,String[] allowedValues,boolean mandatory) throws ConfigException {
        String val=attributes.getValue(attrName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_ATTRIBUTE,attrName);
            else return null;
        }
        for(int i=0;i<allowedValues.length;i++) {
            if(val.equals(allowedValues[i])) return val;
        }
        throw new ConfigException(ConfigException.ILLEGAL_ATTRIBUTE_VALUE,attrName,val);
    }
	
    public String getStringAttribute(Attributes attributes,String attrName,String[] allowedValues,String defaultValue) throws ConfigException {
        String val=attributes.getValue(attrName);
        if(val==null) return defaultValue;
        else {
        	for(int i=0;i<allowedValues.length;i++) {
        		if(val.equals(allowedValues[i])) return val;
        	}
        	throw new ConfigException(ConfigException.ILLEGAL_ATTRIBUTE_VALUE,attrName,val);
        }
    }
    
    public boolean getBooleanAttribute(Attributes attributes,String attrName,boolean mandatory,boolean defaultVal) throws ConfigException {
        String val=attributes.getValue(attrName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_ATTRIBUTE,attrName);
            else return defaultVal;
        }
        if(val.equalsIgnoreCase("true")) return true;
        if(val.equalsIgnoreCase("false")) return false;
        throw new ConfigException(ConfigException.ILLEGAL_ATTRIBUTE_VALUE,attrName,val);
    }
    
    public int getIntegerAttribute(Attributes attributes,String attrName,boolean mandatory,int defaultVal) throws ConfigException {
        String val=attributes.getValue(attrName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_ATTRIBUTE,attrName);
            else return defaultVal;
        }
        try {
        	int intVal=Integer.parseInt(val);
            return intVal;
        } catch(NumberFormatException x) {
        	throw new ConfigException(ConfigException.ILLEGAL_ATTRIBUTE_VALUE,attrName,val);
        }
    }
    
    public Object getObjectAttribute(Attributes attributes,String attrName,Class superClazz,boolean mandatory) throws ConfigException {
    	String val=attributes.getValue(attrName);
    	if(val==null) {
    		if(mandatory) throw new ConfigException(ConfigException.MISSING_ATTRIBUTE,attrName);
    		else return null;
    	}
    	try {
    		Class clazz=Class.forName(val);
    		Object obj=clazz.newInstance();
    		if(!superClazz.isInstance(obj)) throw new ClassCastException("Class '"+val+"' can't be casted to '"+superClazz.getName()+"'.");
    		return obj;
    	} catch(Exception x) {
    		throw new ConfigException(ConfigException.ILLEGAL_ATTRIBUTE_VALUE,attrName,val,x);
    	}
    }
    	
}
