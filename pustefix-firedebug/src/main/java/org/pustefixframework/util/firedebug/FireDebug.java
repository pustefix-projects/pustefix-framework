package org.pustefixframework.util.firedebug;

import java.util.HashMap;

import de.schlund.pfixcore.workflow.ContextResource;

/**
 * FireDebug
 * 
 * FireDebug is a FireDebug wrapper for Pustefix.
 * It is implemented as a ContextResource, so you can call it from everywhere 
 * within Pustefix as long as you have access to the Context.
 * 
 * To set the needed response headers you have to use either the
 * FireDebugEnabledState or the FireDebugServletFilter or you can use both.
 * 
 * @author Holger Rüprich
 */

public interface FireDebug extends ContextResource {
    
    public void log(Object message);
    public void log(Object message, String label);
    public void info(Object message);
    public void info(Object message, String label);
    public void error(Object message);
    public void error(Object message, String label);
    public void warn(Object message);
    public void warn(Object message, String label);
    public void dump(Object variable, String label);
    public HashMap<String, String> getHeaders();
    public void reset();

}