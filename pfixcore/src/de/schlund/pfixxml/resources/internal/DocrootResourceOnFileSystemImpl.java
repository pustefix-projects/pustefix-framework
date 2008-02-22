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

package de.schlund.pfixxml.resources.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import de.schlund.pfixxml.resources.AbstractDocrootResourceImpl;
import de.schlund.pfixxml.resources.DocrootResource;

/**
 * Actual implementation of the {@link DocrootResource}. This class should never
 * be exposed to any other packages as only the interface should be used to access
 * the class.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
class DocrootResourceOnFileSystemImpl extends AbstractDocrootResourceImpl {
    private File file;
    
    DocrootResourceOnFileSystemImpl(URI uri, String docroot) {
        super(uri);
        
        this.file = new File(docroot, path.substring(1));
        
        if (this.trailingSlash && this.exists() && !this.isDirectory()) {
            throw new IllegalArgumentException("URI \"" + uri.toString() + "\" points to a non-existent directory");
        }
    }
    
    public boolean canRead() {
        return file.canRead();
    }

    public boolean canWrite() {
        return file.canWrite();
    }

    public boolean createNewFile() throws IOException {
        return file.createNewFile();
    }

    public boolean delete() {
        return file.delete();
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isHidden() {
        return file.isHidden();
    }

    public long lastModified() {
        return file.lastModified();
    }

    public String[] list() {
        return file.list();
    }
    
    public boolean mkdir() {
        return file.mkdir();
    }

    public boolean mkdirs() {
        return file.mkdirs();
    }

    public URL toURL() throws MalformedURLException {
        return file.toURI().toURL();
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }
    
    public OutputStream getOutputStream(boolean append) throws FileNotFoundException {
        return new FileOutputStream(file, append);
    }

}
