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

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixxml.targets.Themes;

/**
 * Implementation of ThemeList using de.schlund.pfixxml.targets.Themes internally.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixxml.targets.Themes
 */
public class ThemeListImpl implements ThemeList {
    private ArrayList<Theme> themes;

    /**
     * Creates a ThemeList object
     * 
     * @param themes Themes object as used by the Pustefix generator
     */
    public ThemeListImpl(ThemeFactoryService themefactory, Themes themes) {
        if (themes.getThemesArr().length == 0) {
            String msg = "Themes array should not be empty!";
            Logger.getLogger(this.getClass()).warn(msg);
        }
        this.themes = new ArrayList<Theme>();
        String[] array = themes.getThemesArr();
        for (int i = 0; i < array.length; i++) {
            Theme theme = themefactory.getTheme(array[i]);
            this.themes.add(theme);
        }
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.ThemeList#getThemes()
     */
    public List<Theme> getThemes() {
        return new ArrayList<Theme>(this.themes);
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.ThemeList#includesTheme(de.schlund.pfixcore.editor2.core.dom.Theme)
     */
    public boolean includesTheme(Theme theme) {
        return this.themes.contains(theme);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return ("THEMELIST: " + this.toString()).hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Iterator<Theme> i = this.themes.iterator(); i.hasNext();) {
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
