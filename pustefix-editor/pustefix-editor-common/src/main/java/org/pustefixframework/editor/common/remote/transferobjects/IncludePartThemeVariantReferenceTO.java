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

package org.pustefixframework.editor.common.remote.transferobjects;

import java.io.Serializable;


public class IncludePartThemeVariantReferenceTO implements Serializable {
    
    private static final long serialVersionUID = -7081358794051834368L;
    
    public String path;
    public String part;
    public String theme;
    
    @Override
    public boolean equals(Object obj) {
        if ( obj == null || !(obj instanceof IncludePartThemeVariantReferenceTO)) {
            return false;
        }
        IncludePartThemeVariantReferenceTO ref = (IncludePartThemeVariantReferenceTO) obj;
        if ((path == null && ref.path != null) || (path != null && ref.path == null)) {
            return false;
        }
        if ((part == null && ref.part != null) || (part != null && ref.part == null)) {
            return false;
        }
        if ((theme == null && ref.theme != null) || (theme != null && ref.theme == null)) {
            return false;
        }
        return (path == ref.path || path.equals(ref.path)) && (part == ref.part || part.equals(ref.part)) && (theme == ref.theme || theme.equals(ref.theme));
    }
    
    @Override
    public int hashCode() {
        return (((path == null) ? "" : path) + ((part == null) ? "" : part) + ((theme == null) ? "" : theme)).hashCode(); 
    }
    
}
