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

import java.util.List;

/**
 * List of Theme objects. This list always keeps it order, thus allowing a "priorization" of themes.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.core.dom.Theme
 */
public interface ThemeList {
    /**
     * Returns ordered list of themes contained by this ThemeList
     * 
     * @return All themes in this list
     */
    List<Theme> getThemes();
    
    /**
     * Checks wheter the specified theme is included in this list
     * 
     * @param theme Theme object to check
     * @return true if specified theme is included in list, false otherwise
     */
    boolean includesTheme(Theme theme);

    /**
     * Compares the preference of two themes within this list.
     * This method is used to see, whether t1 would be preferred
     * over t2.
     * 
     * @param t1 the potentially preferred theme
     * @param t2 the potentially overridden theme
     * @return <code>true</code> if t1 is found in this list before
     *         t2. This includes the case when t1 is included in the list
     *         but t2 is not. <code>false</code> is returned if t1 is not
     *         in the list or t2 is found before t1.
     */
    boolean themeOverridesTheme(Theme t1, Theme t2);
}
