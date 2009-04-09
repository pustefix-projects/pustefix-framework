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
package org.pustefixframework.webservices.fault;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.util.email.EmailSender;
import de.schlund.pfixcore.util.email.EmailSenderException;

public class EmailNotifier extends Thread {
	
	Logger LOG=Logger.getLogger(getClass().getName());
	
	private static EmailNotifier instance=new EmailNotifier();
	
	private NotifierThread thread;
	ArrayList<Email> emailQueue;
	
	private EmailNotifier() {
		emailQueue=new ArrayList<Email>();
		thread=new NotifierThread();
		thread.start();
	}
	
	public static EmailNotifier getInstance() {
		return instance;
	}
	
	public void sendMail(String subject,String text,String[] recipients,String sender,String smtpHost) {
		Email email=new Email(subject,text,recipients,sender,smtpHost);
		synchronized(emailQueue) {
			emailQueue.add(email);
			emailQueue.notify();
		}
	}
	
	
	class Email {
		
		String subject;
		String text;
		String[] recipients;
		String sender;
		String smtpHost;
		
		Email(String subject,String text,String[] recipients,String sender,String smtpHost) {
			this.subject=subject;
			this.text=text;
			this.recipients=recipients;
			this.sender=sender;
			this.smtpHost=smtpHost;
		}
		
	}

	
	class NotifierThread extends Thread {
	
	    @Override
		public void run() {
			while(!isInterrupted()) {
				Email email=null;
				synchronized(emailQueue) {
					if(emailQueue.isEmpty()) {
						try {
							emailQueue.wait();
						} catch(InterruptedException x) {}
					} 
					email=(Email)emailQueue.remove(0);
				}
				try {     
					EmailSender.sendMail(email.subject,email.text,email.recipients,email.sender,email.smtpHost);
				} catch(EmailSenderException x) {
					LOG.error("Error while sending exception mail.",x);
				}
			}
		}
		
	}
	
}
