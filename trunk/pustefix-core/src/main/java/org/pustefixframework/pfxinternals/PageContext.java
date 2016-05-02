package org.pustefixframework.pfxinternals;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;


public interface PageContext {
    
    public enum MessageLevel { INFO, WARN, ERROR };
    
    public ServletContext getServletContext();
    public ApplicationContext getApplicationContext();
    public void addMessage(MessageLevel level, String message);

}
