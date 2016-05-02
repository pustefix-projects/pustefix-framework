package org.pustefixframework.config.project;

public class SessionTimeoutInfo {

    private int initialTimeout;
    private int requestLimit;
    
    public void setInitialTimeout(int initialTimeout) {
        this.initialTimeout = initialTimeout;
    }
    
    public int getInitialTimeout() {
        return initialTimeout;
    }
    
    public void setRequestLimit(int requestLimit) {
        this.requestLimit = requestLimit;
    }
    
    public int getRequestLimit() {
        return requestLimit;
    }
    
}
