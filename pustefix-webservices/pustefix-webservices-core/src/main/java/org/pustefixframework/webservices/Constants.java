/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.webservices;

public class Constants {
    
    public final static String SESSION_PREFIX=";jsessionid=";
    
    public final static String PROP_COMMON_FILE="servlet.commonpropfile";
    public final static String PROP_SERVLET_FILE="servlet.propfile";
    public final static String PROP_CONTEXT_NAME="servlet.contextname";
    
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
    public final static String PROTOCOL_TYPE_JSONQX="JSONQX";
    public final static String PROTOCOL_TYPE_TEST="TEST";
    public final static String PROTOCOL_TYPE_ANY="ANY";
    public final static String[] PROTOCOL_TYPES={PROTOCOL_TYPE_SOAP,PROTOCOL_TYPE_JSONWS,PROTOCOL_TYPE_JSONQX,PROTOCOL_TYPE_TEST,PROTOCOL_TYPE_ANY};
    
    public final static String ENCODING_STYLE_RPC="rpc";
    public final static String ENCODING_STYLE_DOCUMENT="document";
    public final static String ENCODING_STYLE_WRAPPED="wrapped";
    public final static String[] ENCODING_STYLES={ENCODING_STYLE_RPC,ENCODING_STYLE_DOCUMENT,ENCODING_STYLE_WRAPPED};
    public final static String ENCODING_USE_ENCODED="encoded";
    public final static String ENCODING_USE_LITERAL="literal";
    public final static String[] ENCODING_USES={ENCODING_USE_ENCODED,ENCODING_USE_LITERAL};
    
    public final static String STUBGEN_JSNAMESPACE_COMPAT="COMPAT";
    public final static String STUBGEN_JSNAMESPACE_COMPATUNIQUE="COMPAT_UNIQUE";
    public final static String STUBGEN_JSNAMESPACE_JAVANAME="JAVA_NAME";
    public final static String[] STUBGEN_JSNAMESPACES={STUBGEN_JSNAMESPACE_COMPAT,STUBGEN_JSNAMESPACE_COMPATUNIQUE,STUBGEN_JSNAMESPACE_JAVANAME};
    
    public final static String STUBGEN_DEFAULT_JSNAMESPACE="WS_";
    public final static String STUBGEN_JSONWS_JSNAMESPACE="JWS_";
    
    public static final String HEADER_SOAP_ACTION="SOAPAction";
    public static final String HEADER_REQUEST_ID="Request-Id";
    public static final String PARAM_SOAP_MESSAGE="soapmessage";
    public static final String PARAM_REQUEST_ID="PFX_Request_ID";
    
    public static final String CONTENT_TYPE_URLENCODED="application/x-www-form-urlencoded";
    
    public static final String XMLNS_XSD="http://www.w3.org/2001/XMLSchema";
    public static final String XMLNS_WSDL="http://schemas.xmlsoap.org/wsdl/";
    public static final String XMLNS_APACHESOAP="http://xml.apache.org/xml-soap";
    
    public static final String SOAPBINDING_TRANSPORT_HTTP="http://schemas.xmlsoap.org/soap/http";
    
    public final static String WS_CONF_NS = "http://www.pustefix-framework.org/2008/namespace/webservice-config";
    public final static String CUS_NS = "http://www.schlund.de/pustefix/customize";
    
    public final static String HEADER_WSTYPE="wstype";
    public final static String PARAM_WSTYPE="wstype";
    
}
