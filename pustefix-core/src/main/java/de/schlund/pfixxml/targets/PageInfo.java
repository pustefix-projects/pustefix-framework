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

package de.schlund.pfixxml.targets;

/**
 * PageInfo.java
 *
 *
 * Created: Mon Jul 23 19:17:02 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class PageInfo implements Comparable<PageInfo> {
    private String          name;
    private String          variant;
    private TargetGenerator generator;
    
    protected PageInfo(TargetGenerator gen, String pagename, String pagevariant) {
        name      = pagename;
        variant   = pagevariant;
        generator = gen;
    }
        
    public String getName() {
        return name;
    }

    public String getVariant() {
        return variant;
    }

    public TargetGenerator getTargetGenerator() {
        return generator;
    }

    public int compareTo(PageInfo in) {
        if (getTargetGenerator().getName().compareTo(in.getTargetGenerator().getName()) != 0) {
            return getTargetGenerator().getName().compareTo(in.getTargetGenerator().getName());
        } else {
            if (!getName().equals(in.getName())) {
                return getName().compareTo(in.getName());
            } else {
                if (getVariant() == null && in.getVariant() == null) {
                    return 0;
                } else if (getVariant() == null && in.getVariant() != null) {
                    return 1;
                } else if (getVariant() != null && in.getVariant() == null) {
                    return -1;
                } else {
                    return getVariant().compareTo(in.getVariant());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "[PAGE: " + name + " " + generator.getName() + "/" + getVariant() + "]";
    }
    
}// PageInfo
