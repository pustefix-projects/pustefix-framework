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
import java.util.Collection;
import java.util.Iterator;

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
    private ArrayList themes;

   /**
    * Creates a ThemeList object
    * 
    * @param themes Themes object as used by the Pustefix generator
    */
    public ThemeListImpl(ThemeFactoryService themefactory, Themes themes) {
        this.themes = new ArrayList();
        String[] array = themes.getThemesArr();
        for (int i = 0; i < array.length; i++) {
            Theme theme = themefactory.getTheme(array[i]);
            this.themes.add(theme);
        }
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.ThemeList#getThemes()
     */
    public Collection getThemes() {
       return new ArrayList(this.themes);
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
        Iterator i1 = this.getThemes().iterator();
        Iterator i2 = list.getThemes().iterator();
        while (i1.hasNext()) {
            if (!i2.hasNext()) {
                return false;
            }
            Theme theme1 = (Theme) i1.next();
            Theme theme2 = (Theme) i2.next();
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
        for (Iterator i = this.themes.iterator(); i.hasNext();) {
            Theme theme = (Theme) i.next();
            buf.append(theme.getName());
            buf.append(" ");
        }
        return buf.substring(0, buf.length()-1);
    }
}
