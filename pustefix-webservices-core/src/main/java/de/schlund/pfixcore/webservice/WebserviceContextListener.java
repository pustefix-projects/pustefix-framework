/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package de.schlund.pfixcore.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This ServletContextListener implementation checks if a JAXWS endpoint configuration
 * can be found and, if so, creates a JAXWS ServletContextListener implementation and
 * delegates all calls to it.
 * 
 * @author mleidig@schlund.de
 */
public class WebserviceContextListener implements ServletContextAttributeListener, ServletContextListener {
    
    private ServletContextListener contextListener;
    private ServletContextAttributeListener attributeListener;
    
    private final static String JAXWS_CONFIG = "/WEB-INF/sun-jaxws.xml";
    private final static String JAXWS_LISTENER = "com.sun.xml.ws.transport.http.servlet.WSServletContextListener";
    
    public void attributeReplaced(ServletContextAttributeEvent event) {
        if(attributeListener!=null) attributeListener.attributeReplaced(event);
    }
    
    public void attributeAdded(ServletContextAttributeEvent event) {
        if(attributeListener!=null) attributeListener.attributeAdded(event);
    }
    
    public void attributeRemoved(ServletContextAttributeEvent event) {
        if(attributeListener!=null) attributeListener.attributeRemoved(event);
    }
    
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        URL configURL = null;
        try {
            configURL = context.getResource(JAXWS_CONFIG);
        } catch(MalformedURLException x) {
            throw new RuntimeException("Error initializing JAXWS runtime.",x);
        }
        if(configURL != null) {
            try {
                Class<?> clazz=Class.forName(JAXWS_LISTENER);
                Object listener=clazz.newInstance();
                //in JAXWS 2.1.3 the methods implementing this interface are empty,
                //so let's ignore if the interface isn't implmented in future versions
                if(ServletContextAttributeListener.class.isAssignableFrom(clazz)) 
                    attributeListener=(ServletContextAttributeListener)listener;
                if(ServletContextListener.class.isAssignableFrom(clazz))
                    contextListener=(ServletContextListener)listener;
                else throw new RuntimeException("Error intializing JAXWS runtime: ServletContextListener "
                        +"interface isn't implemented by class "+JAXWS_LISTENER);
                //make JAXWS less verbose (shouldn't be hard-coded here)
                Logger.getLogger("com.sun.xml.ws.server.sei.EndpointMethodHandler").setLevel(Level.OFF);
                Logger.getLogger("com.sun.xml.ws.transport.http.servlet").setLevel(Level.SEVERE);
                Logger.getLogger("javax.enterprise.resource.webservices.jaxws.server.http").setLevel(Level.SEVERE);
                Logger.getLogger("javax.enterprise.resource.webservices.jaxws.servlet.http").setLevel(Level.SEVERE);
            } catch(ClassNotFoundException x) {
                throw new RuntimeException("Error initializing JAXWS runtime: ServletContextListener "
                        +"class "+JAXWS_LISTENER+" not found",x);
            } catch(Exception x) {
                throw new RuntimeException("Error initializing JAXWS runtime: Creating ServletContextListener "
                        +JAXWS_LISTENER+" failed",x);
            }
        }
        if(contextListener!=null) contextListener.contextInitialized(event);
    }
    
    public void contextDestroyed(ServletContextEvent event) {
        if(contextListener!=null) contextListener.contextDestroyed(event);
    }

}
