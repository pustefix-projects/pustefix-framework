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

package org.pustefixframework.editor.common.remote.transferobjects;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectTO implements Serializable {

    private static final long serialVersionUID = -7081358794051834368L;

    public String name;

    public String comment;

    public boolean includePartsEditableByDefault;

    public List<String> pages = new LinkedList<String>();

    public List<String> topPages = new LinkedList<String>();

    public List<IncludePartThemeVariantReferenceTO> includeParts = new LinkedList<IncludePartThemeVariantReferenceTO>();

    public List<String> images = new LinkedList<String>();

    public Map<String, String> prefixToNamespaceMappings = new LinkedHashMap<String, String>();

    public List<String> dynIncludeFiles = new LinkedList<String>();

}
