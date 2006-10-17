/*
 * Created on 13.06.2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice;

public class ServiceException extends Exception {

	public ServiceException(String msg) {
		super(msg);
	}
	
	public ServiceException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
