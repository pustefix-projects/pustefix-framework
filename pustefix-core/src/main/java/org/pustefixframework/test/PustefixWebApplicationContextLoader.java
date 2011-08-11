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
import java.net.MalformedURLException;

import org.pustefixframework.container.spring.beans.PustefixWebApplicationContext;
import org.pustefixframework.http.internal.PustefixInit;
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
 * @author mleidig@schlund.de
 *
 */
public class PustefixWebApplicationContextLoader implements ContextLoader {
    
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
     * Location URIs using the 'docroot:' scheme will be resolved to according file URIs.
     * Calling this method also creates a mock ServletContext and initializes factories.
     */
    public ApplicationContext loadContext(String... locations) {
        
        if(docroot==null) docroot = GlobalConfig.guessDocroot();
        //Mock ServletContext
        MockServletContext servletContext = new MockServletContext(docroot.toURI().toString());
        servletContext.addInitParameter("pustefix.docroot", docroot.getAbsolutePath());
        
        //Initialize factories
        try {
            PustefixInit.init(servletContext);
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
