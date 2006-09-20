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

package de.schlund.pfixxml.config.includes;

import java.io.File;
import java.util.EventObject;

public class FileIncludeEvent extends EventObject {
    private static final long serialVersionUID = -3702684169219524940L;
    private File includedFile;

    protected FileIncludeEvent(Object source, File file) {
        super(source);
        this.includedFile = file;
    }
    
    public File getIncludedFile() {
        return includedFile;
    }
}
