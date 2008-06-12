/*
 * Created on 13.06.2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.pustefixframework.webservices;

public class ServiceProcessingException extends ServiceException {

	/**
     * 
     */
    private static final long serialVersionUID = -5587167087434915343L;

    public ServiceProcessingException(String msg) {
		super(msg);
	}
	
	public ServiceProcessingException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
