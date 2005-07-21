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

package de.schlund.pfixcore.editor2.core.dom;

import java.util.Collection;

import org.w3c.dom.Document;

/**
 * Stores several IncludeParts
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface IncludeFile {
    /**
     * Returns path and filename of this IncludeFile (relative to docroot)
     * 
     * @return Path to this IncludeFile
     */
    String getPath();

    /**
     * Returns IncludePart stored in this IncludeFile identified by name
     * 
     * @param name
     *            Name of the IncludePart to return
     * @return IncludePart object corresponding to <code>name</code> or
     *         <code>null</code> if no IncludePart for this name can be found
     *         within this IncludeFile
     */
    IncludePart getPart(String name);

    /**
     * Creates a new IncludePart. The part is not actually created in the file,
     * but only in memory. Changes on the filesystem are first made, when
     * content is assigned to the part. If there is already a part with the
     * specified name, the existing part is returned.
     * 
     * @param name
     *            Name of the part
     * @return IncludePart object representing the new IncludePart
     */
    IncludePart createPart(String name);
    
    /**
     * Returns true if there is a part with the specified name,
     * whereas this does not necessarily mean, that this part is also already stored on filesystem.
     * 
     * @param name Name of the part to look for
     * @return <code>true</code> true if the part is existing, <code>false</code> otherwise.
     */
    boolean hasPart(String name);
    
    /**
     * Returns all parts which are stored in this IncludeFile.
     * 
     * @return Iterator iterating over all parts stored in this file
     */
    Collection getParts();
    
    /**
     * Returns a DOM Document representing the content of this IncludeFile. If
     * this IncludeFile is not yet existing on filesystem, <code>null</code>
     * is returned.
     * 
     * @return DOM Document with content of this IncludeFile or
     *         <code>null</code> if file is not yet existing.
     */
    Document getContentXML();
}
