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

package de.schlund.pfixxml.resources;



/**
 * Creates docroot resources using environment dependend methods.
 * A class implementing this interface provides Pustefix docroot 
 * resources. This task can be performed by using files in a directory
 * on the filesystem, files from a JAR file or any other mechanism
 * that can provide file like resources. 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class DocrootResourceProvider implements ResourceProvider {
    
    private static String PFIXROOT_SCHEME = "pfixroot";
    private static String[] supportedSchemes = {PFIXROOT_SCHEME};
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
}
