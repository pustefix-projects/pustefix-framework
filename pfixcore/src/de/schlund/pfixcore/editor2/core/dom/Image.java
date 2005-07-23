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

/**
 * Represents an image that is used by one or several include parts.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Image {
    /**
     * Returns the path to the image file (relative to docroot)
     * 
     * @return Path to image file
     */
    String getPath();
    
    /**
     * Returns Enumeration containing all Page objects which are affected by
     * this Image. This list is allways recursive, which means all pages which
     * are directly or indirectly depending on this Image are returned.
     * 
     * @return All affected pages of this Target
     * @see Page
     */
    Collection getAffectedPages();
}
