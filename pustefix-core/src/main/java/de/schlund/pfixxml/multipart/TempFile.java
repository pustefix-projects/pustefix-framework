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
 *
 */
package de.schlund.pfixxml.multipart;

import java.io.File;
import java.net.URI;

/**
 * Temporary file implementation, which ensures that the associated physical file is deleted,
 * when the object is no longer in use, i.e. when it gets finalized by the garbage collector. 
 * 
 * Created: 21.12.2004
 * 
 * @author mleidig
 */
public class TempFile extends File {

    /**
     * 
     */
    private static final long serialVersionUID = -5489856878726432493L;

    public TempFile(File parent,String child) {
        super(parent,child);
    }
    
    public TempFile(String pathname) {
        super(pathname);
    }
    
    public TempFile(String parent,String child) {
        super(parent,child);
    }
    
    public TempFile(URI uri) {
        super(uri);
    }
    
    @Override
    protected void finalize() throws Throwable {
        delete();
    }
    
}
