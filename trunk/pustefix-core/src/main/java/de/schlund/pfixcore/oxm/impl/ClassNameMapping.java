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
package de.schlund.pfixcore.oxm.impl;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.schlund.pfixcore.oxm.impl.annotation.ClassNameAlias;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class ClassNameMapping {

    Map<Class<?>, String> mappings;

    public ClassNameMapping() {
        mappings = new HashMap<Class<?>, String>();
        mappings.put(Integer.class, "int");
        mappings.put(Character.class, "char");
        mappings.put(GregorianCalendar.class, "calendar");
        mappings.put(Date.class, "date");
        mappings.put(Properties.class, "properties");
    }

    public String mapClassName(Class<?> clazz) {
        String name = null;
        synchronized (mappings) {
            name = mappings.get(clazz);
        }
        if (name == null) {
            ClassNameAlias elementName = clazz.getAnnotation(ClassNameAlias.class);
            if (elementName != null) {
                name = elementName.value();
            } else {
                if (Collection.class.isAssignableFrom(clazz)) {
                    if (List.class.isAssignableFrom(clazz)) {
                        name = "list";
                    } else if (Set.class.isAssignableFrom(clazz)) {
                        name = "set";
                    }
                    name = "collection";
                } else if (Map.class.isAssignableFrom(clazz)) {
                    name = "map";
                } else if (clazz.isArray()) {
                    name = "array";
                }
            }
            if (name == null) name = getElementName(clazz);
            synchronized (mappings) {
                mappings.put(clazz, name);
            }
        }
        return name;
    }

    private String getElementName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

}
