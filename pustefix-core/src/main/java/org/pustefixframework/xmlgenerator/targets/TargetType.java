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

package org.pustefixframework.xmlgenerator.targets;

import java.lang.reflect.Constructor;

import org.pustefixframework.resource.FileResource;
import org.pustefixframework.resource.Resource;

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

    public static final TargetType XSL_LEAF   = new TargetType("xsl_leaf", "org.pustefixframework.xmlgenerator.targets.XSLLeafTarget", false);
    public static final TargetType XML_LEAF   = new TargetType("xml_leaf", "org.pustefixframework.xmlgenerator.targets.XMLLeafTarget", false);
    public static final TargetType XSL_VIRTUAL = new TargetType("xsl",     "org.pustefixframework.xmlgenerator.targets.XSLVirtualTarget", true);
    public static final TargetType XML_VIRTUAL = new TargetType("xml",     "org.pustefixframework.xmlgenerator.targets.XMLVirtualTarget", true);
    
    private static final TargetType[] typearray = {XSL_LEAF, XML_LEAF, XSL_VIRTUAL, XML_VIRTUAL}; 
    
    private boolean virtual;
    private String tag;
    private Class<? extends TargetRW>  theclass;
    
    private TargetType(String tag, String theclass, boolean virtual) {
        try {
            this.virtual = virtual;
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
    
    public Constructor<? extends TargetRW> getTargetClassConstructor() throws SecurityException, NoSuchMethodException {
        if (virtual) {
            return theclass.getConstructor(new Class[]{TargetType.class, TargetGenerator.class, FileResource.class, FileResource.class, String.class, Themes.class});
        } else {
            return theclass.getConstructor(new Class[]{TargetType.class, TargetGenerator.class, Resource.class, FileResource.class, String.class, Themes.class});
        }
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
