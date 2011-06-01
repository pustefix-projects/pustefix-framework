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
package de.schlund.pfixxml.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface Resource extends Comparable<Resource>, org.springframework.core.io.Resource {

    /**
     * Tests whether this resource exists.
     *
     * @return  <code>true</code> if and only if the file, directory, etc. denoted
     *          by this resource exists; <code>false</code> otherwise
     */
    boolean exists();
    
    /**
     * Tests whether the application can read the resource denoted by this
     * object.
     *
     * @return  <code>true</code> if and only if the resource specified by this
     *          object exists <em>and</em> can be read by the application; 
     *          <code>false</code> otherwise
     */
    boolean canRead();
    
    /**
     * Returns the time that the resource was
     * last modified. Might return "-1" if the last modified time is
     * not available.
     *
     * @return  A <code>long</code> value representing the time the file was
     *          last modified, measured in milliseconds since the epoch
     *          (00:00:00 GMT, January 1, 1970), or <code>0L</code> if the
     *          file does not exist or if an I/O error occurs
     */
    long lastModified();
    
    long length();
    
    /**
     * Tests whether the resource denoted by this object is a normal
     * file.  A file is <em>normal</em> if it is not a directory and, in
     * addition, satisfies other system-dependent criteria.  Any non-directory
     * file created by a Java application is guaranteed to be a normal file.
     *
     * @return  <code>true</code> if and only if the file denoted by this
     *          file resource exists <em>and</em> is a normal file;
     *          <code>false</code> otherwise
     */
    boolean isFile();
    
    
    /**
     * Returns a normalized version of the URI which was used
     * in order to create this Resource 
     * 
     * @return URI representing this object
     */
    public URI toURI();
    
    /**
     * Get the originating URI which was used to retrieve this
     * resource using the ResourceProvider mechanism.
     * 
     * @return URI originating URI used to retrieve this resource
     */
    public URI getOriginatingURI();
    
    public void setOriginatingURI(URI uri);
    
    /**
     * Returns a stream which can be used for reading the content of
     * the resource denoted by this object.
     * 
     * @return stream for reading the resource
     * @throws IOException if resource cannot be found or read
     */
    InputStream getInputStream() throws IOException;
    
}
