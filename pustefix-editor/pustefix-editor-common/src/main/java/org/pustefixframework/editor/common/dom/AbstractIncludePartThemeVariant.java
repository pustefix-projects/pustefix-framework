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

package org.pustefixframework.editor.common.dom;

/**
 * Provides functionality common to all classes implementing
 * IncludePartThemeVariant
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractIncludePartThemeVariant implements
        IncludePartThemeVariant {

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IncludePartThemeVariant variant) {
        int ret;
        ret = this.getIncludePart().getIncludeFile().compareTo(variant.getIncludePart().getIncludeFile());
        if (ret != 0) {
            return ret;
        }
        ret = this.getIncludePart().compareTo(variant.getIncludePart());
        if (ret != 0) {
            return ret;
        }
        ret = this.getTheme().compareTo(variant.getTheme());
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof IncludePartThemeVariant)) {
            return false;
        }
        IncludePartThemeVariant incVariant = (IncludePartThemeVariant) obj;
        return this.getTheme().equals(incVariant.getTheme())
                && this.getIncludePart().equals(incVariant.getIncludePart());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return ("INCLUDEPARTTHEMEVARIANT: " + this.toString()).hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getIncludePart().toString() + " ("
                + this.getTheme().getName() + ")";
    }
}
