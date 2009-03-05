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

package de.schlund.pfixcore.beans.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mleidig@schlund.de
 */
public class Bean {
    
    String className;
    Map<String,Property> properties;
    boolean excludeByDefault;
    
    public Bean(String className) {
        this.className=className;
        properties=new HashMap<String,Property>();
    }
    
    public String getClassName() {
        return className;
    }

    public Property getProperty(String name) {
        return properties.get(name);
    }
    
    public void setProperty(Property property) {
        properties.put(property.getName(),property);
    }
    
    public boolean isExcludedByDefault() {
        return excludeByDefault;
    }
    
    public void excludeByDefault() {
        excludeByDefault=true;
    }
    
    public void excludeProperty(String name) {
        Property prop=getProperty(name);
        if(prop==null) {
            prop=new Property(name);
            setProperty(prop);
        } 
        prop.exclude();
    }
    
    public void includeProperty(String name) {
        Property prop=getProperty(name);
        if(prop==null) {
            prop=new Property(name);
            setProperty(prop);
        } 
        prop.include();
    }
    
    public void setPropertyAlias(String name,String alias) {
        Property prop=getProperty(name);
        if(prop==null) {
            prop=new Property(name);
            setProperty(prop);
        } 
        prop.setAlias(alias);
    }
    
}
