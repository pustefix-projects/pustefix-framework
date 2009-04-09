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
 *
 */

package de.schlund.pfixxml.targets;

/**
 * DependencyType.java
 *
 *
 * Created: Thu Jul 19 19:48:27 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public final class DependencyType {
    public static final DependencyType TEXT   = new DependencyType("text", true);
    public static final DependencyType IMAGE  = new DependencyType("image", true);
    public static final DependencyType TARGET = new DependencyType("target", true);
    public static final DependencyType FILE   = new DependencyType("file", false);
    public static final DependencyType ROOT   = new DependencyType("root", true);

    private static final DependencyType[] typearray = {TEXT, IMAGE, TARGET, FILE, ROOT}; 
    
    private final String tag;
    private final boolean isDynamic;
    
    private DependencyType(String tag, boolean isDynamic) {
        this.tag = tag;
        this.isDynamic = isDynamic;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public String toString() {
        return "[" + getTag() + "]";
    }

    public String getTag() {
        return tag;
    }
    
    public static DependencyType getByTag(String type) {
        for (int i = 0; i < typearray.length; i++) {
            DependencyType tmp = typearray[i];
            if (type.equals(tmp.getTag())) {
                return tmp;
            }
        }
        throw new RuntimeException("AuxDep with unknow type '" + type + "'");
    }
        
}// DependencyType
