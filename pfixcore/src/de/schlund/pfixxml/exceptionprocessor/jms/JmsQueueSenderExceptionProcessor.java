/*
 * Created on 26.04.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.exceptionprocessor.jms;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.jmsexceptionhandler.rmiobj.ExceptionDataValue;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionDataValueHelper;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;


/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 * 
 */
public class JmsQueueSenderExceptionProcessor implements ExceptionProcessor {
	private static Logger Log = Logger.getLogger(JmsQueueSenderExceptionProcessor.class);
	private Cubbyhole cubby;
	private JmsSender sender;
	
	
	public JmsQueueSenderExceptionProcessor()  {
		cubby = new Cubbyhole(100);
		sender = new JmsSender(cubby);
	}
	
	/* (non-Javadoc)
	 * @see de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor#processException(java.lang.Throwable, de.schlund.pfixxml.exceptionprocessor.ExceptionConfig, de.schlund.pfixxml.PfixServletRequest, javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void processException(Throwable exception, ExceptionConfig exConfig, PfixServletRequest pfixReq,
			ServletContext servletContext, HttpServletRequest req, HttpServletResponse res, Properties props) 
    throws IOException,
			ServletException {
		
		
		ExceptionDataValue exdata = ExceptionDataValueHelper.createExceptionDataValue(exception, pfixReq);
	    
	    try {
			cubby.put(exdata);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		}
		
		Log.info("throwable now in cubbyhole");
	}

	
	
}
