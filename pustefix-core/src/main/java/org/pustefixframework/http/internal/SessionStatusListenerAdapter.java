package org.pustefixframework.http.internal;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.workflow.SessionStatusEvent;
import de.schlund.pfixcore.workflow.SessionStatusListener;

public class SessionStatusListenerAdapter implements HttpSessionListener {

    private Logger LOG = Logger.getLogger(SessionStatusListenerAdapter.class);
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        
        HttpSession session = se.getSession();
        if(session != null) {
            SessionStatusListener[] statusListeners = (SessionStatusListener[])session.getAttribute(SessionStatusListener.class.getName());
            if(statusListeners != null && statusListeners.length > 0) {
                SessionStatusEvent event = new SessionStatusEvent(SessionStatusEvent.Type.SESSION_DESTROYED);
                try {
                    for (SessionStatusListener statusListener : statusListeners) {
                        statusListener.sessionStatusChanged(event);
                    }
                } catch(Throwable t) {
                    LOG.error("Error calling SessionStatusListener at end of session", t);
                }
            }
        }
    }
    
}
