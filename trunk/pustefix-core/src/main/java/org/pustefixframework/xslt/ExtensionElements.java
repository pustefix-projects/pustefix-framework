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

package org.pustefixframework.xslt;

import java.util.HashMap;
import java.util.Map;

import com.icl.saxon.style.ExtensionElementFactory;
import com.icl.saxon.style.StyleElement;

/**
 * Factory creating XSL extension elements. XSL stylesheets using its extension
 * elements reference this class within an according XML namespace declaration.
 *
 */
public class ExtensionElements implements ExtensionElementFactory {

    private Map<String, Class<? extends StyleElement>> nameToClass = new HashMap<String, Class<? extends StyleElement>>();

    public ExtensionElements() {
        nameToClass.put("fail-safe", FailSafeExtensionElement.class);
        nameToClass.put("log", LogExtensionElement.class);
        nameToClass.put("debug", LogExtensionElement.class);
        nameToClass.put("info", LogExtensionElement.class);
        nameToClass.put("warn", LogExtensionElement.class);
        nameToClass.put("error", LogExtensionElement.class);
    }

    @Override
    public Class<?> getExtensionClass(String localName) {
        Class<?> clazz = nameToClass.get(localName);
        if(clazz == null) {
            throw new IllegalArgumentException("Extension element '" + localName + "' doesn't exist");
        }
        return clazz;
    }

}
