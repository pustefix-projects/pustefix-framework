package de.schlund.pfixcore.webservice;

public class Constants {
    
    public final static String SESSION_PREFIX=";jsessionid=";
    
    public final static String PROP_COMMON_FILE="servlet.commonpropfile";
    public final static String PROP_SERVLET_FILE="servlet.propfile";
    
	final public static String OLD_CLASSLOADER_PROPERTY="org.jboss.net.axis.OLD_CLASSLOADER_PROPERTY";
	final public static String CONFIGURATION_CONTEXT="org.jboss.net.axis.CONFIGURATION_CONTEXT";
	final static String FLASH_ENABLED_CLIENT ="org.jboss.net.axis.FLASH_ENABLED_CLIENT";

    public final static int SESSION_TYPE_NONE=0;
    public final static int SESSION_TYPE_SERVLET=1;
    public final static int SESSION_TYPE_SOAPHEADER=1;
    
    public final static int MONITOR_SCOPE_SESSION=0;
    public final static int MONITOR_SCOPE_IP=1;
    
    public static final String MSGCTX_PROP_CTXRESMAN="PFX_CTXRESMAN";
    public static final String MSGCTX_PROP_MONITORSTART="PFX_MONITORSTART";
    public static final String MSGCTX_PROP_MONITORENTRY="PFX_MONITORENTRY";
    
}
