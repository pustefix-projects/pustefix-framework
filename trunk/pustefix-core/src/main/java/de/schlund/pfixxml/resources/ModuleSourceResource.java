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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ModuleSourceResource extends ModuleResource {
    
    private File file;
    
    public ModuleSourceResource(URI uri, File moduleDir, String moduleResourcePath) {
        super(uri);
        file = new File(moduleDir, moduleResourcePath + uri.getPath());
    }
    
    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }
    
    @Override
    public long length() {
        return file.length();
    }
    
    public File getFile() {
        return file;
    }
}
