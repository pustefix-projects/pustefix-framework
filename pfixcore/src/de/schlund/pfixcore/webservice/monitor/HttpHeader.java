/*
 * Created on 31.07.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HttpHeader {

    private String name;
    private String value;
    
    public HttpHeader(String name,String value) {
        this.name=name;
        this.value=value;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getValue() {
    	return value;
    }
    
    public String toString() {
    	return name+": "+value;
    }
    
}
