/*
 * Created on 10.05.2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.exceptionprocessor.jms;
import java.util.Hashtable;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import de.schlund.jmsexceptionhandler.rmiobj.*;

/**
 * @author jh
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class JmsSender implements Runnable {
    private static Logger LOG = Logger.getLogger(JmsSender.class);
	private QueueSender queueSender = null;
	private QueueSession queueSession = null;
	private Cubbyhole cubbyhole;
	private static String JNDI1 = "UIL2ConnectionFactory";
	private static String JNDI2 = "queue/ErrorQueue";
	
	public JmsSender(Cubbyhole cubby) {
		cubbyhole = cubby;
		init();
		Thread th = new Thread(this);
		th.start();
	}
	
	private void send(ExceptionDataValue exdata) {
		LOG.debug("queueSender: " + queueSender);
		LOG.debug("queueSession: " + queueSession);
		try {
			ObjectMessage msg = queueSession.createObjectMessage();
			msg.setObject(exdata);
			queueSender.send(msg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	public void init()   {
		Hashtable env = new Hashtable();
		env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		env.put("java.naming.provider.url", "jnp://localhost:1099");
		env.put("java.naming.factory.url.pkgs", "org.jboss.naming.client:org.jnp.interfaces:org.jboss.naming");
		env.put("j2ee.clientName", "JmsQueueSenderExceptionProcessor");
		Context jndiContext = null;
		try {
			jndiContext = new InitialContext(env);
		} catch (NamingException e) {
			e.printStackTrace();
			return;
		}
		QueueConnectionFactory queueConnectionFactory = null;
		try {
			queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup(JNDI1);
		} catch (NamingException e2) {	
			e2.printStackTrace();
			return;
		} 
		
		LOG.debug(queueConnectionFactory);
		Queue queue = null;
		try {
			queue = (Queue) jndiContext.lookup(JNDI2);
		} catch (NamingException e1) {
			e1.printStackTrace();
			return;
		} 
		LOG.debug(queue);
		QueueConnection queueConnection = null;
		try {
			queueConnection = queueConnectionFactory.createQueueConnection();
		} catch (JMSException e5) {
			e5.printStackTrace();
			return;
		}
		LOG.debug(queueConnection);
		try {
			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e3) {
			e3.printStackTrace();
			return;
		}
		LOG.debug(queueSession);
		try {
			queueSender = queueSession.createSender(queue);
		} catch (JMSException e4) {
			e4.printStackTrace();
			return;
		}
		LOG.debug(queueSender);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ExceptionDataValue exdata = null;
		while(true) {
			try {
				exdata = (ExceptionDataValue) cubbyhole.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			send(exdata);
		}
		
	}
}
