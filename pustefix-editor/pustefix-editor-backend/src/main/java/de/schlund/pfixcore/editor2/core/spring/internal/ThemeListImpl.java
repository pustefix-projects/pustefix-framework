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
import java.util.List;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.AbstractThemeList;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.ThemeList;

import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixxml.targets.Themes;

/**
 * Implementation of ThemeList using de.schlund.pfixxml.targets.Themes internally.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixxml.targets.Themes
 */
public class ThemeListImpl extends AbstractThemeList implements ThemeList {
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


}
