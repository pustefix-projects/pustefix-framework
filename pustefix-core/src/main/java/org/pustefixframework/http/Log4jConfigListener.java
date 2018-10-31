package org.pustefixframework.http;

import java.lang.reflect.Method;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.pustefixframework.http.internal.PustefixInit;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;

/**
 * Configures Log4j at early application startup, i.e. first initializes essential Pustefix stuff,
 * then the Log4j configuration from WEB-INF/pfixlog.xml is applied.
 * 
 * Without using this listener, everything is done at the beginning of the ApplicationContext creation.
 *
 * @deprecated Will be removed in Pustefix 1.0, because obsolete after upgrading to Log4J 2 or Logback.
 */
@Deprecated
public class Log4jConfigListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		try {
			PustefixInit pustefixInit = new PustefixInit(sce.getServletContext());
			sce.getServletContext().setAttribute(PustefixInit.SERVLET_CONTEXT_ATTRIBUTE_NAME, pustefixInit);
		} catch(PustefixCoreException x) {
			throw new PustefixRuntimeException("Error initializing Pustefix", x);
		}
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
		sce.getServletContext().removeAttribute(PustefixInit.SERVLET_CONTEXT_ATTRIBUTE_NAME);
        try {
            Class<?> clazz = Class.forName("org.apache.log4j.LogManager");
            Method meth = clazz.getMethod("shutdown");
            meth.invoke(null);
        } catch(Exception x) {
            System.err.println("[ERROR] Error shutting down Log4j.");
            x.printStackTrace(System.err);
        }
    };

}
