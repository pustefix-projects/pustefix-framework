package org.pustefixframework.config.project;

import org.pustefixframework.http.SessionTrackingStrategy;

public class SessionTrackingStrategyInfo {
    
    private Class<? extends SessionTrackingStrategy> sessionTrackingStrategy;
    
    public void setSessionTrackingStrategy(Class<? extends SessionTrackingStrategy> sessionTrackingStrategy) {
        this.sessionTrackingStrategy = sessionTrackingStrategy;
    }
    
    public SessionTrackingStrategy getSessionTrackingStrategyInstance() {
        try {
            return sessionTrackingStrategy.newInstance();
        } catch(Exception x) {
            throw new RuntimeException("Can't instantiate session tracking strategy", x);
        }
    }

}
