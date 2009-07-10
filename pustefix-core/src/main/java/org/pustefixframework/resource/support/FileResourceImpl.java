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

package org.pustefixframework.resource.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.pustefixframework.resource.AbstractResource;
import org.pustefixframework.resource.FileResource;

/**
 * Resource implementation using a {@link File} to access the resource.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class FileResourceImpl extends AbstractResource implements FileResource {

    private URI uri;
    private URI originalURI;
    private File file;

    /**
     * Creates a new File based resource.
     * 
     * @param uri is returned by {@link #getOriginalURI()}
     * @param originallyRequestedURI is returned by {@link #getURI()} 
     *  if not <code>null</code>, otherwise <code>uri</code> is returned
     * @param file is used to actually access the resource
     */
    public FileResourceImpl(URI uri, URI originallyRequestedURI, File file) {
        this.originalURI = uri;
        if (originallyRequestedURI != null) {
            this.uri = originallyRequestedURI;
        } else {
            this.originalURI = uri;
        }
        this.file = file;
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    public URI getOriginalURI() {
        return originalURI;
    }

    public URI[] getSupplementaryURIs() {
        URI fileURI = file.toURI();
        if (fileURI.equals(uri) || fileURI.equals(originalURI)) {
            return null;
        }
        return new URI[] { fileURI };
    }

    public URI getURI() {
        return uri;
    }

    public URL getURL() {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("File path \"" + file.getPath() + "\" could not be converted to a URL", e);
        }
    }

    public File getFile() {
        return file;
    }

}
