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
package org.pustefixframework.http.internal;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.util.JarFileCache;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * This Servlet is just there to have it's init method called on startup of the
 * VM. It starts all VM-global factories by calling their 'init' method from the
 * {@link FactoryInit} interface. These factories are located by analyzing the
 * "servlet.propfile" parameter which points to a file where all factories are
 * listed.
 */
public class PustefixInit {

    public final static String SERVLET_CONTEXT_ATTRIBUTE_NAME = "___PUSTEFIX_INIT___";

    public PustefixInit(ServletContext servletContext) throws PustefixCoreException {
        this(servletContext, null);
    }
    
    public PustefixInit(ServletContext servletContext, String docrootstr) throws PustefixCoreException {

    	try {
    	    final File cacheDir = PustefixTempDirs.getInstance(servletContext).createTempDir("pustefix-jar-cache-");
    	    JarFileCache.setCacheDir(cacheDir);
    	} catch(IOException x) {
    	    throw new RuntimeException("Error creating temporary directory for JAR caching", x);
    	}

        initEnvironmentProperties(servletContext);

    	if(docrootstr == null) {
    	    docrootstr = servletContext.getRealPath("/");
    	    if (docrootstr == null) {
    	        GlobalConfigurator.setServletContext(servletContext);
    	    } else {
    	        if (!docrootstr.equals(GlobalConfig.getDocroot())) {
                    GlobalConfigurator.setDocroot(docrootstr);
    	        }
    	    }
    	} else {
    	    GlobalConfigurator.setDocroot(docrootstr);
    	}
    }

    public static void initEnvironmentProperties(ServletContext servletContext) {
        //override environment properties by according context init parameters
        Enumeration<?> names = servletContext.getInitParameterNames();
        while(names.hasMoreElements()) {
            String name = (String)names.nextElement();
            String value = servletContext.getInitParameter(name);
            if(value != null && !value.equals("")) {
                EnvironmentProperties.getProperties().put(name, value);
            }
        }
    }

}
