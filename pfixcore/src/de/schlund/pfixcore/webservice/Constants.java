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
    
    public static final String MSGCTX_PROP_CTXRESMAN="PFX_CTXRESMAN";
    public static final String MSGCTX_PROP_MONITORSTART="PFX_MONITORSTART";
    public static final String MSGCTX_PROP_MONITORENTRY="PFX_MONITORENTRY";
    
    public final static String MONITOR_SCOPE_SESSION="session";
    public final static String MONITOR_SCOPE_IP="ip";
    public final static String[] MONITOR_SCOPES={MONITOR_SCOPE_SESSION,MONITOR_SCOPE_IP};
    
    public final static String ENCODING_STYLE_RPC="rpc";
    public final static String ENCODING_STYLE_DOCUMENT="document";
    public final static String[] ENCODING_STYLES={ENCODING_STYLE_RPC,ENCODING_STYLE_DOCUMENT};
    public final static String ENCODING_USE_ENCODED="encoded";
    public final static String ENCODING_USE_LITERAL="literal";
    public final static String[] ENCODING_USES={ENCODING_USE_ENCODED,ENCODING_USE_LITERAL};
 
    public static final String HEADER_SOAP_ACTION="SOAPAction";
    public static final String PARAM_SOAP_MESSAGE="soapmessage";
    
}
