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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides access to files. This interface is similar to the
 * {@link java.io.File} object, but is more flexible. You usually
 * should not create your own classes implementing this interface
 * but use the {@link de.schlund.pfixxml.resources.ResourceUtil}
 * class to retrieve instances of this class.
 * Note that depending on the system state, some methods might not
 * be available, especially methods which modify a resource. These
 * methods will then behave like being used on a file that is not
 * writable. Note also the special behaviour of the 
 * {@link #lastModified()} method which is different from the behaviour
 * of the corresponding method in the <code>File</code> class.
 * 
 * @see de.schlund.pfixxml.resources.ResourceUtil
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface FileResource extends Resource {
    
    /**
     * Tests whether the application can modify the resource denoted by this
     * object.
     *
     * @return  <code>true</code> if and only if the resource denoted by this 
     *          object exists <em>and</em> the application is allowed to write 
     *          to the resource; <code>false</code> otherwise.
     */
    boolean canWrite();
    
    /**
     * Atomically creates a new, empty file resource identified by this object if
     * and only if a file with this name does not yet exist.  The check for the
     * existence of the file and the creation of the file if it does not exist
     * are a single operation that is atomic with respect to all other
     * filesystem activities that might affect the file.
     * <P>
     * Note: this method should <i>not</i> be used for file-locking, as
     * the resulting protocol cannot be made to work reliably. The 
     * {@link java.nio.channels.FileLock FileLock}
     * facility should be used instead. 
     *
     * @return  <code>true</code> if the named file does not exist and was
     *          successfully created; <code>false</code> if the named file
     *          already exists
     *
     * @throws  IOException
     *          If an I/O error occurred
     */
    boolean createNewFile() throws IOException;
    
    /**
     * Deletes the file or directory denoted by this file resource.  If
     * this resource denotes a directory, then the directory must be empty in
     * order to be deleted.
     *
     * @return  <code>true</code> if and only if the file or directory is
     *          successfully deleted; <code>false</code> otherwise
     */
    boolean delete();
    
    /**
     * Returns the name of the file or directory denoted by this file resource.
     * This is just the last name in the pathname's name sequence.  If the 
     * pathname's name sequence is empty, then the empty string is returned.
     *
     * @return  The name of the file or directory denoted by this file 
     *          resource, or the empty string if this resource's name sequence
     *          is empty
     */
    String getName();
    
    /**
     * Returns the file resource matching this file resource's parent,
     * or <code>null</code> if this file resource does not have a parent
     * directory.
     *
     * <p> The <em>parent</em> of an file resource consists of the
     * pathname's prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The file resource matching the parent directory named by this
     *          file resource, or <code>null</code> if this file resource
     *          does not name a parent
     *
     * @since 1.2
     */
    FileResource getParentAsFileResource();
    
    /**
     * Tests whether the resource denoted by this file resource is a
     * directory.
     *
     * @return <code>true</code> if and only if the resource denoted by this
     *          object exists <em>and</em> is a directory;
     *          <code>false</code> otherwise
     */
    boolean isDirectory();
    
    /**
     * Tests whether the file named by this file resource is a hidden
     * file.  The exact definition of <em>hidden</em> is system-dependent.  On
     * UNIX systems, a file is considered to be hidden if its name begins with
     * a period character (<code>'.'</code>).  On Microsoft Windows systems, a file is
     * considered to be hidden if it has been marked as such in the filesystem.
     *
     * @return  <code>true</code> if and only if the file denoted by this
     *          resource is hidden according to the conventions of the
     *          underlying platform
     */
    boolean isHidden();
    
    /**
     * Returns an array of strings naming the files and directories in the
     * directory denoted by this resource.
     *
     * <p> If this resource does not denote a directory, then this
     * method returns <code>null</code>.  Otherwise an array of strings is
     * returned, one for each file or directory in the directory.  Names
     * denoting the directory itself and the directory's parent directory are
     * not included in the result.  Each string is a file name rather than a
     * complete path.
     *
     * <p> There is no guarantee that the name strings in the resulting array
     * will appear in any specific order; they are not, in particular,
     * guaranteed to appear in alphabetical order.
     *
     * @return  An array of strings naming the files and directories in the
     *          directory denoted by this file resource.  The array will be
     *          empty if the directory is empty.  Returns <code>null</code> if
     *          this file resource does not denote a directory, or if an
     *          I/O error occurs.
     */
    String[] list();
    
    /**
     * Returns an array of file resource denoting the files in the
     * directory denoted by this file resource.
     *
     * <p> If this file resource does not denote a directory, then this
     * method returns <code>null</code>.  Otherwise an array of
     * <code>FileResource</code> objects is returned, one for each file or 
     * directory in the directory.  Resources denoting the directory itself and the
     * directory's parent directory are not included in the result.
     *
     * <p> There is no guarantee that the name strings in the resulting array
     * will appear in any specific order; they are not, in particular,
     * guaranteed to appear in alphabetical order.
     *
     * @return  An array of file resources denoting the files and
     *          directories in the directory denoted by this file resource.
     *          The array will be empty if the directory is empty.  Returns 
     *          <code>null</code> if this abstract pathname does not denote
     *          a directory, or if an I/O error occurs.
     */
    FileResource[] listFileResources();
    
    /**
     * Creates the directory named by this file resource.
     *
     * @return  <code>true</code> if and only if the directory was
     *          created; <code>false</code> otherwise
     */
    boolean mkdir();
    
    /**
     * Creates the directory named by this file resource, including any
     * necessary but nonexistent parent directories.  Note that if this
     * operation fails it may have succeeded in creating some of the necessary
     * parent directories.
     *
     * @return  <code>true</code> if and only if the directory was created,
     *          along with all necessary parent directories; <code>false</code>
     *          otherwise
     */
    boolean mkdirs();
    
    /**
     * Returns an URL which can be used in order to access the
     * file donoted by this object.
     * 
     * @return URL suitable to access the resource denoted by this object
     * @throws MalformedURLException if this object does not contain a valid path
     */
    URL toURL() throws MalformedURLException;
    
    /**
     * Returns a which can be used for writing to the resource
     * denoted by this object.
     * 
     * @return stream for writing to the resource
     * @throws FileNotFoundException if resource cannot be found or is readonly
     */
    OutputStream getOutputStream() throws FileNotFoundException;
    
    OutputStream getOutputStream(boolean append) throws FileNotFoundException;
    
}
