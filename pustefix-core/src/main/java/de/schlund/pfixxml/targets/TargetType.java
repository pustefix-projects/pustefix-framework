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
 *
 */

package de.schlund.pfixxml.targets;

/**
 * TargetType.java
 *
 *
 * Created: Thu Jul 19 19:48:27 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public final class TargetType {

    public static final TargetType XSL_LEAF   = new TargetType("xsl_leaf", "de.schlund.pfixxml.targets.XSLLeafTarget");
    public static final TargetType XML_LEAF   = new TargetType("xml_leaf", "de.schlund.pfixxml.targets.XMLLeafTarget");
    public static final TargetType XSL_VIRTUAL = new TargetType("xsl",     "de.schlund.pfixxml.targets.XSLVirtualTarget");
    public static final TargetType XML_VIRTUAL = new TargetType("xml",     "de.schlund.pfixxml.targets.XMLVirtualTarget");
    
    private static final TargetType[] typearray = {XSL_LEAF, XML_LEAF, XSL_VIRTUAL, XML_VIRTUAL}; 
    
    private String tag;
    private Class<? extends TargetRW>  theclass;
    
    private TargetType(String tag, String theclass) {
        try {
            this.tag      = tag;
            this.theclass = Class.forName(theclass).asSubclass(TargetRW.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public String toString() {
        return "[" + getTag() + "]";
    }
    
    public String getTag() {
        return tag;
    }
    
    public Class<? extends TargetRW> getTargetClass() {
        return theclass;
    }

    public static TargetType getByTag(String type) {
        for (int i = 0; i < typearray.length; i++) {
            TargetType tmp = typearray[i];
            if (type.equals(tmp.getTag())) {
                return tmp;
            }
        }
        throw new RuntimeException("Target with unknown type '" + type + "'");
    }
  
}// TargetType
