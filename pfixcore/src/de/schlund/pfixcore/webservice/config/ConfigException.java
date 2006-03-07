/*
 * de.schlund.pfixcore.webservice.config.ConfigException
 */
package de.schlund.pfixcore.webservice.config;

/**
 * ConfigException.java 
 * 
 * Created: 03.08.2004
 * 
 * @author mleidig
 */
public class ConfigException extends Exception {
    
    public final static int MISSING_PROPERTY=0;
    public final static int ILLEGAL_PROPERTY_VALUE=1;
    
    private int type;
    private String propName;
    private String propVal;
    private Throwable cause;
    
    public ConfigException(int type,String propName) {
        super();
        this.type=type;
        this.propName=propName;
    }
    
    public ConfigException(int type,String propName,String propVal) {
        super();
        this.type=type;
        this.propName=propName;
        this.propVal=propVal;
    }
    
    public ConfigException(int type,String propName,String propVal,Throwable cause) {
    	super();
    	this.type=type;
    	this.propName=propName;
    	this.propVal=propVal;
    	this.cause=cause;
    }
    
    public String getMessage() {
    	String msg="";
        if(type==MISSING_PROPERTY) {
            msg="Mandatory property '"+propName+"' is not set.";
        } else if(type==ILLEGAL_PROPERTY_VALUE) {
            msg="Property '"+propName+"' has illegal value: '"+propVal+"'.";
        } else msg="Unknown error";
        if(cause!=null) msg+="[Cause: "+cause.toString()+"]";
        return msg;
    }

}
