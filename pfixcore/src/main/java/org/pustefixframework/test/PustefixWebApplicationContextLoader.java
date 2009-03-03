package org.pustefixframework.test;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import org.pustefixframework.container.spring.beans.PustefixWebApplicationContext;
import org.pustefixframework.http.internal.FactoryInitWorker;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * ContextLoader strategy implementation for PustefixWebApplicationContext loading.
 * This class can be used to set up the ApplicationContext using Spring's TestContext Framework
 * or creating it programmatically.
 * 
 * @author mleidig@schlund.de
 *
 */
public class PustefixWebApplicationContextLoader implements ContextLoader {
    
    private static File DEFAULT_DOCROOT = new File("projects");
    
    private File docroot;
    
    public PustefixWebApplicationContextLoader() {
        
    }
    
    public PustefixWebApplicationContextLoader(File docroot) {
        this.docroot = docroot;
    }
    
    /**
     * Returns a PustefixWebApplicationContext configured using the passed location URIs.
     * 
     * You have to pass the locations of the project configuration file (project.xml) and
     * one or more Spring configuration files (spring.xml).
     * Location URIs using the 'pfixroot:' scheme will be resolved to according file URIs.
     * Calling this method also creates a mock ServletContext and initializes factories.
     */
    public ApplicationContext loadContext(String... locations) {
        
        //Mock ServletContext
        MockServletContext servletContext = new MockServletContext();
        if(docroot==null) docroot = DEFAULT_DOCROOT;
        servletContext.addInitParameter("pustefix.docroot", docroot.getAbsolutePath());
        
        //Initialize factories
        try {
            FactoryInitWorker.init(servletContext);
        } catch(ServletException x) {
            throw new RuntimeException("Factory initialization error", x);
        }
        
        //Resolve "pfixroot:" URIs
        for(int i=0;i<locations.length;i++) {
            if(locations[i].startsWith("pfixroot")) {
                FileResource fileRes = ResourceUtil.getFileResource(locations[i]);
                try {
                    locations[i] = fileRes.toURL().toString();
                } catch(MalformedURLException x) {
                    throw new IllegalArgumentException("Illegal location", x);
                }
            }
        }
        
        //Set up PustefixWebApplicationContext
        PustefixWebApplicationContext appContext = new PustefixWebApplicationContext();
        appContext.setServletContext(servletContext);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);
        appContext.setConfigLocations(locations);
        appContext.refresh();
       
        return appContext;
    }
    
    public String[] processLocations(Class<?> clazz, String... locations) {
        return locations;
    }
    
}
