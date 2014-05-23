/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.pustefixframework.container.spring.beans.PustefixWebApplicationContext;
import org.pustefixframework.http.internal.PustefixInit;
import org.pustefixframework.http.internal.PustefixTempDirs;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * ContextLoader strategy implementation for PustefixWebApplicationContext loading.
 * This class can be used to set up the ApplicationContext using Spring's TestContext Framework
 * or creating it programmatically.
 * 
 * <p>
 * Usage example:<br/>
 * <pre>
 * @ContextConfiguration(loader=PustefixWebApplicationContextLoader.class,
 *                       locations={"docroot:/WEB-INF/project.xml", 
 *                                  "docroot:/WEB-INF/spring.xml", 
 *                                  "(mode=test)"})
 * </pre>
 * </p>
 */
public class PustefixWebApplicationContextLoader implements ContextLoader {
    
    private File docroot;
    private ServletContext servletContext;
    private Properties locationProperties = new Properties();
    
    public PustefixWebApplicationContextLoader() {
        
    }
    
    public PustefixWebApplicationContextLoader(File docroot) {
        this.docroot = docroot;
    }
    
    public PustefixWebApplicationContextLoader(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public PustefixWebApplicationContextLoader(File docroot, ServletContext servletContext) {
        this.docroot = docroot;
        this.servletContext = servletContext;
    }
    
    /**
     * Returns a PustefixWebApplicationContext configured using the passed location URIs.
     * 
     * You have to pass the locations of the project configuration file (project.xml) and
     * one or more Spring configuration files (spring.xml).
     * Location URIs using the 'docroot:' scheme will be resolved to according file URIs.
     * Calling this method also creates a mock ServletContext and initializes factories.
     */
    public ApplicationContext loadContext(String... locations) {

        //Mock ServletContext
        if(docroot==null) docroot = GlobalConfig.guessDocroot();
        
        if(servletContext == null) {
            servletContext = new MockServletContext(docroot.toURI().toString());
            try {
                File tmpDir = PustefixTempDirs.getInstance(servletContext).createTempDir("pustefix-logs-");
                ((MockServletContext)servletContext).addInitParameter("logroot", tmpDir.getCanonicalPath());
                String mode = locationProperties.getProperty("mode");
                if(mode != null) {
                    ((MockServletContext)servletContext).addInitParameter("mode", mode);
                }
            } catch(IOException x) {
                throw new RuntimeException("Error creating temporary log directory", x);
            }
            
        }
        
        PustefixInit pustefixInit;
        try {
            pustefixInit = new PustefixInit(servletContext, docroot.getAbsolutePath());
        } catch(PustefixCoreException x) {
            throw new RuntimeException("Pustfix initialization error", x);
        }
        
        //Resolve "docroot:" URIs
        for(int i=0;i<locations.length;i++) {
            if(locations[i].startsWith("docroot")) {
                FileResource fileRes = ResourceUtil.getFileResource(locations[i]);
                try {
                    locations[i] = fileRes.toURL().toString();
                } catch(MalformedURLException x) {
                    throw new IllegalArgumentException("Illegal location", x);
                }
            }
        }
        
        //Set up PustefixWebApplicationContext
        PustefixWebApplicationContext appContext = new PustefixWebApplicationContext(pustefixInit);
        appContext.registerShutdownHook();
        appContext.setServletContext(servletContext);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);
        appContext.setConfigLocations(locations);
        appContext.refresh();
        
        return appContext;
    }
    
    public String[] processLocations(Class<?> clazz, String... locations) {
        List<String> newLocations = new ArrayList<String>();
        for(String location: locations) {
            if(location.startsWith("(") && location.endsWith(")")) {
                String[] tokens = location.substring(1, location.length() - 1).split("=");
                if(tokens.length == 2) {
                    locationProperties.setProperty(tokens[0], tokens[1]);
                }
            } else {
                newLocations.add(location);
            }
        }
        return newLocations.toArray(new String[newLocations.size()]);
    }
    
}
