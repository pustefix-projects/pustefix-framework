package de.schlund.pfixcore.webservice;

public class Constants {
    
    public final static String SESSION_PREFIX=";jsessionid=";
    
    public final static String PROP_COMMON_FILE="servlet.commonpropfile";
    public final static String PROP_SERVLET_FILE="servlet.propfile";
    
    final public static String OLD_CLASSLOADER_PROPERTY="org.jboss.net.axis.OLD_CLASSLOADER_PROPERTY";
    final public static String CONFIGURATION_CONTEXT="org.jboss.net.axis.CONFIGURATION_CONTEXT";
    final static String FLASH_ENABLED_CLIENT ="org.jboss.net.axis.FLASH_ENABLED_CLIENT";

    public final static String SESSION_TYPE_NONE="none";
    public final static String SESSION_TYPE_SERVLET="servlet";
    public final static String SESSION_TYPE_SOAPHEADER="soapheader";
    public final static String[] SESSION_TYPES={SESSION_TYPE_NONE,SESSION_TYPE_SERVLET,SESSION_TYPE_SOAPHEADER};
     
    public static final String SERVICE_SCOPE_APPLICATION="application";
    public static final String SERVICE_SCOPE_SESSION="session";
    public static final String SERVICE_SCOPE_REQUEST="request";
    public static final String[] SERVICE_SCOPES={SERVICE_SCOPE_APPLICATION,SERVICE_SCOPE_SESSION,SERVICE_SCOPE_REQUEST};
    
    public final static String MONITOR_SCOPE_SESSION="session";
    public final static String MONITOR_SCOPE_IP="ip";
    public final static String[] MONITOR_SCOPES={MONITOR_SCOPE_SESSION,MONITOR_SCOPE_IP};
    
    public final static String PROTOCOL_TYPE_SOAP="SOAP";
    public final static String PROTOCOL_TYPE_JSONWS="JSONWS";
    public final static String PROTOCOL_TYPE_ANY="ANY";
    public final static String[] PROTOCOL_TYPES={PROTOCOL_TYPE_SOAP,PROTOCOL_TYPE_JSONWS,PROTOCOL_TYPE_ANY};
    
    public final static String ENCODING_STYLE_RPC="rpc";
    public final static String ENCODING_STYLE_DOCUMENT="document";
    public final static String ENCODING_STYLE_WRAPPED="wrapped";
    public final static String[] ENCODING_STYLES={ENCODING_STYLE_RPC,ENCODING_STYLE_DOCUMENT,ENCODING_STYLE_WRAPPED};
    public final static String ENCODING_USE_ENCODED="encoded";
    public final static String ENCODING_USE_LITERAL="literal";
    public final static String[] ENCODING_USES={ENCODING_USE_ENCODED,ENCODING_USE_LITERAL};
 
    public static final String HEADER_SOAP_ACTION="SOAPAction";
    public static final String HEADER_REQUEST_ID="Request-Id";
    public static final String PARAM_SOAP_MESSAGE="soapmessage";
    public static final String PARAM_REQUEST_ID="PFX_Request_ID";
    
    public static final String CONTENT_TYPE_URLENCODED="application/x-www-form-urlencoded";
    
    public static final String XMLNS_XSD="http://www.w3.org/2001/XMLSchema";
    public static final String XMLNS_SOAPENC="http://schemas.xmlsoap.org/soap/encoding/";
    public static final String XMLNS_WSDL="http://schemas.xmlsoap.org/wsdl/";
    public static final String XMLNS_APACHESOAP="http://xml.apache.org/xml-soap";
    
    public static final String SOAPBINDING_TRANSPORT_HTTP="http://schemas.xmlsoap.org/soap/http";
    
    public final static String WS_CONF_NS = "http://pustefix.sourceforge.net/wsconfig200401";
    public final static String CUS_NS = "http://www.schlund.de/pustefix/customize";
    
    public final static String HEADER_WSTYPE="wstype";
    public final static String PARAM_WSTYPE="wstype";
    
}
