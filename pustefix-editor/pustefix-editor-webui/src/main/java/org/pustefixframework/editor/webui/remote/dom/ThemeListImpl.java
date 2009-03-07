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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.pustefixframework.editor.common.dom.AbstractThemeList;
import org.pustefixframework.editor.common.dom.Theme;



public class ThemeListImpl extends AbstractThemeList {
    List<Theme> themes;
    
    public ThemeListImpl(Collection<Theme> themes) {
        this.themes = Collections.unmodifiableList(new LinkedList<Theme>(themes));
    }
    
    public List<Theme> getThemes() {
        return this.themes; 
    }
    
}
