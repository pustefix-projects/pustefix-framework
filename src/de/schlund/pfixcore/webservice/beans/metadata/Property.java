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

package de.schlund.pfixcore.webservice.beans.metadata;

/**
 * @author mleidig@schlund.de
 */
public class Property {

    String name;
    String alias;
    boolean exclude;
    boolean include;
    
    public Property(String name) {
        this.name=name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setAlias(String alias) {
        this.alias=alias;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public boolean isExcluded() {
        return exclude;
    }
    
    public void exclude() {
        exclude=true;
    }
    
    public boolean isIncluded() {
        return include;
    }
    
    public void include() {
        include=true;
    }
    
}
