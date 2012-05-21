package org.pustefixframework.http;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.servlet.http.HttpSession;


public class SessionUtils {
	
	public final static String SESSION_ATTR_LOCK = "__PFX_SESSION_LOCK__";
	
	public static void invalidate(HttpSession session) {
		ReadWriteLock lock = (ReadWriteLock)session.getAttribute(SessionUtils.SESSION_ATTR_LOCK);
		if(lock != null) {
			Lock writeLock = lock.writeLock();
			writeLock.lock();
			try {
				session.invalidate();
			} finally {
				writeLock.unlock();
			}
		} else {
			session.invalidate();
		}
	}

}
