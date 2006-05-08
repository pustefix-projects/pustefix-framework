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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Provides access to files. This interface is similar to the
 * {@link java.io.File} object, but is more flexible. You usually
 * should not create your own classes implementing this interface
 * but use the {@link de.schlund.pfixxml.resources.ResourceUtil}
 * class to retrieve instances of this class.
 * 
 * @see de.schlund.pfixxml.resources.ResourceUtil
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface FileResource extends Comparable<FileResource> {
    boolean canRead();
    boolean canWrite();
    boolean createNewFile() throws IOException;
    boolean delete();
    boolean exists();
    String getName();
    FileResource getParentAsFileResource();
    boolean isDirectory();
    boolean isFile();
    boolean isHidden();
    long lastModified();
    String[] list();
    FileResource[] listFileResources();
    boolean mkdir();
    boolean mkdirs();
    
    /**
     * Returns a normalized version of the URI which was used
     * in order to create this FileResource 
     * 
     * @return URI representing this object
     */
    URI toURI();
    
    /**
     * Returns an URL which can be used in order to access the
     * file donoted by this object.
     * 
     * @return URL suitable to access the resource denoted by this object
     * @throws MalformedURLException if this object does not contain a valid path
     */
    URL toURL() throws MalformedURLException;
    
    /**
     * Returns a stream which can be used for reading the content of
     * the resource denoted by this object.
     * 
     * @return stream for reading the resource
     * @throws FileNotFoundException if resource cannot be found or read
     */
    InputStream getInputStream() throws FileNotFoundException;
    
    /**
     * Returns a which can be used for writing to the resource
     * denoted by this object.
     * 
     * @return stream for writing to the resource
     * @throws FileNotFoundException if resource cannot be found or is readonly
     */
    OutputStream getOutputStream() throws FileNotFoundException;
}
