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

package de.schlund.pfixxml.resources.internal;

import java.io.File;
import java.net.URI;

import de.schlund.pfixxml.resources.DocrootResourceProvider;
import de.schlund.pfixxml.resources.Resource;

/**
 * Provider using a path on the local file system to resolve docroot resources.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootResourceOnFileSystemProvider extends DocrootResourceProvider {
    
    private String docroot;
    private String fallbackDocroot;
    
    public DocrootResourceOnFileSystemProvider(String docroot) {
        this.docroot = docroot;
        // Support for running webapps from source with 'mvn tomcat:run'
        // Set the target artifact directory as alternative docroot if docroot is source location 
        if(docroot.endsWith("src/main/webapp")) {
            File dir = guessFallbackDocroot();
            if(dir != null) {
                fallbackDocroot = dir.getAbsolutePath();
            }
        }
    }
    
    public Resource getResource(URI uri) {
    	String path = uri.getPath();
    	// Support for running webapps from source with 'mvn tomcat:run'
    	// Use the alternative docroot for paths which aren't located in source dir when running from source
    	if( fallbackDocroot != null && 
    			( path.startsWith("/core/") || 
    			  path.startsWith("/modules/") ||
    			  path.startsWith("/.cache/") ||
    			  path.startsWith("/wsscript/") ||
    			  path.startsWith("/wsdl/") ||
    			  path.equals("/WEB-INF/buildtime.prop") ) ) {
            return new DocrootResourceOnFileSystemImpl(uri, fallbackDocroot);
        }
        return new DocrootResourceOnFileSystemImpl(uri, docroot);
    }

    
    private static File guessFallbackDocroot() {
        File target = new File("target");
        if(target.exists() && target.isDirectory()) {
            for(File file:target.listFiles()) {
                if(file.isDirectory()) {
                    File webInfDir = new File(file, "WEB-INF");
                    if(webInfDir.exists()) return file;
                }
            }
        }
        return null;
    }
    
}
