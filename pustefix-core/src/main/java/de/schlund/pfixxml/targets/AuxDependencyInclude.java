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

package de.schlund.pfixxml.targets;

import de.schlund.pfixxml.resources.Resource;

/**
 * Stores information about an include part used by a target.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixxml.targets.DependencyType#TEXT
 */
public class AuxDependencyInclude extends AuxDependencyFile {
    private String part;

    private String theme;

    public AuxDependencyInclude(Resource path, String part, String theme) {
        super(path);
        this.type = DependencyType.TEXT;
        this.part = part;
        this.theme = theme;

        String key = type.getTag() + "@" + path.toString() + "@" + part
                + "@" + theme;
        this.hashCode = key.hashCode();
    }

    /**
     * Return name of the part that is included
     * 
     * @return part name
     */
    public String getPart() {
        return part;
    }

    /**
     * Returns name of the theme variant being used
     * 
     * @return theme name
     */
    public String getTheme() {
        return theme;
    }

    @Override
    public int compareTo(AuxDependency o) {
        int comp;

        comp = super.compareTo(o);
        if (comp != 0) {
            return comp;
        }

        AuxDependencyInclude a = (AuxDependencyInclude) o;

        comp = part.compareTo(a.part);
        if (comp != 0) {
            return comp;
        }

        return theme.compareTo(a.theme);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuxDependencyInclude) {
            AuxDependencyInclude a = (AuxDependencyInclude) obj;
            return (this.compareTo(a) == 0);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "[AUX/" + getType() + " " + getPath().toURI().toString() + "@"
                + getPart() + "@" + getTheme() + "]";
    }

}
