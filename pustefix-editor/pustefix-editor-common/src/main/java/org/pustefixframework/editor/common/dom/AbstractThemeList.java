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

package org.pustefixframework.editor.common.dom;

import java.util.Iterator;

/**
 * Provides functionality common to all classes implementing Theme
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractThemeList implements ThemeList {
    
    public boolean includesTheme(Theme theme) {
        return this.getThemes().contains(theme);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ThemeList)) {
            return false;
        }
        ThemeList list = (ThemeList) obj;
        Iterator<Theme> i1 = this.getThemes().iterator();
        Iterator<Theme> i2 = list.getThemes().iterator();
        while (i1.hasNext()) {
            if (!i2.hasNext()) {
                return false;
            }
            Theme theme1 = i1.next();
            Theme theme2 = i2.next();
            if (!theme1.equals(theme2)) {
                return false;
            }
        }
        return !i2.hasNext();
    }

    @Override
    public int hashCode() {
        return ("THEMELIST: " + this.toString()).hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Iterator<Theme> i = this.getThemes().iterator(); i.hasNext();) {
            Theme theme = i.next();
            buf.append(theme.getName());
            buf.append(" ");
        }
        return buf.substring(0, buf.length() - 1);
    }

    public boolean themeOverridesTheme(Theme t1, Theme t2) {
        if (t1.equals(t2)) {
            return false;
        }

        for (Iterator<Theme> i = this.getThemes().iterator(); i.hasNext();) {
            Theme t = i.next();
            if (t.equals(t1)) {
                return true;
            } else if (t.equals(t2)) {
                return false;
            }
        }

        return false;
    }
}
