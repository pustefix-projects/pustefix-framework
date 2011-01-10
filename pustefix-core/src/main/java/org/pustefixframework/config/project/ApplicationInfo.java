package org.pustefixframework.config.project;

import org.pustefixframework.http.SessionTrackingStrategy;

public class ApplicationInfo {
    
    private SessionTrackingStrategy sessionTrackingStrategy;
    
    public SessionTrackingStrategy getSessionTrackingStrategy() {
        return sessionTrackingStrategy;
    }
    
    public void setSessionTrackingStrategy(SessionTrackingStrategy sessionTrackingStrategy) {
        this.sessionTrackingStrategy = sessionTrackingStrategy;
    }

}
