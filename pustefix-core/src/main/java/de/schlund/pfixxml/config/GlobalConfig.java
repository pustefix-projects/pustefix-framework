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

package de.schlund.pfixxml.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import de.schlund.pfixxml.resources.ResourceProviderRegistry;
import de.schlund.pfixxml.resources.internal.DocrootResourceByServletContextProvider;
import de.schlund.pfixxml.resources.internal.DocrootResourceOnFileSystemProvider;

/**
 * Provides access to global (shared between all application within an environment)
 * Pustefix settings. Settings can be changed through 
 * {@link de.schlund.pfixxml.config.GlobalConfigurator}.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class GlobalConfig {
    
    private static String docroot = null;
    private static URL docrootURL = null;
    private static ServletContext servletContext = null;
    
    /**
     * Returns the absolute to the Pustefix docroot (usually the "projects" directory)
     * 
     * @return Absolute path to Pustefix docroot or <code>null</code> if Pustefix
     *         docroot is not available (e.g. in packed WAR mode)
     */
    public static String getDocroot() {
        return docroot;
    }

    public static void reset() {
        docroot = null;
        docrootURL = null;
        ResourceProviderRegistry.reset();
    }

    static void setDocroot(String path) {
        if (docroot != null || servletContext != null) {
            if(path.equals(docroot)) return;
            throw new IllegalStateException("Docroot or servlet context may only be set once!");
        }
        docroot = path;
        ResourceProviderRegistry.register(new DocrootResourceOnFileSystemProvider(docroot));
        try {
            docrootURL =  new URL("file", null, -1, docroot);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot create URL for docroot: " + docroot, e);
        }

    }

    public static File guessDocroot() {
        File target = new File("target");
        File root;
        
        if (target.isDirectory()) {
            for (File dir : target.listFiles()) {
                if (dir.isDirectory()) {
                    root = new File(dir, "WEB-INF/web.xml");
                    if (root.exists()) {
                        return dir;
                    }
                }
            }
        }
        throw new IllegalStateException("cannot guess docroot");
    }
    

    /**
     * Returns an URL that points to the Pustefix docroot.
     * This can be used to retrieve elements relative to the
     * docroot.
     * 
     * @return URL denoting the Pustefix docroot
     */
    public static URL getDocrootAsURL() {
        if (docrootURL != null) {
            return docrootURL;
         } else {
            throw new IllegalStateException("getDocrootAsURL() can only be used if either docroot or servlet context are set!");
        }
    }
    
    /**
     * Returns the servlet context
     * 
     * @return The servlet context or <code>null</code> if not available
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }
    
    static void setServletContext(ServletContext context) {
        if (docroot != null || servletContext != null) {
            throw new IllegalStateException("Docroot or servlet context may only be set once!");
        }
        servletContext = context;
        ResourceProviderRegistry.register(new DocrootResourceByServletContextProvider(servletContext));
        try {
            docrootURL = servletContext.getResource(".");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unexpected error while creating URL for docroot!", e);            }
        
    }
    
}
