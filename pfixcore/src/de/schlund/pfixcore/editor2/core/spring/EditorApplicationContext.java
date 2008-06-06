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
 */

package de.schlund.pfixcore.editor2.core.spring;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Project specific implementation of Spring ApplicationContext.
 * 
 * This implementation uses an XML configuration file to configure all beans.
 * The docroot is used to resolve ResourcePathes to filesystem objects.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorApplicationContext extends AbstractXmlApplicationContext {
    /**
     * Stores locations of config files
     */
    private String configLocations[];
    /**
     * Stores path to docroot (usually Pustefix docroot)
     */
    private String docroot;
    
    /**
     * Creates an ApplicationContext using a XML configuration
     * 
     * @param configLocation Path to the XML configuration file
     * @param docroot Path to the Pustefix docroot
     */
    public EditorApplicationContext(String configLocation, String docroot) {
        this(new String[] {configLocation}, docroot);
    }

    /**
     * Creates an ApplicationContext using an arbitrary number of XML configurations
     * 
     * @param configLocations Array containing pathes to the XML configuration file
     * @param docroot Path to the Pustefix docroot
     */
    public EditorApplicationContext(String configLocations[], String docroot) {
        this.configLocations = configLocations;
        this.docroot = docroot;
        this.refresh();
    }
    
    /**
     * Returns location of configurations specified when this object has been constructed
     * 
     * @return Array containing configuration locations
     * @see org.springframework.context.support.AbstractXmlApplicationContext#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return this.configLocations;
    }

    /**
     * Resolves a resource path using the configured docroot
     * 
     * @return Resource representing the filesystem object
     * @see org.springframework.core.io.DefaultResourceLoader#getResourceByPath(java.lang.String)
     */
    protected Resource getResourceByPath(String path) {
        String rpath = path;
        if (rpath.startsWith("/")) {
            rpath = rpath.substring(1);
        }
        return new FileSystemResource(this.docroot + "/" + rpath);
    }
}
